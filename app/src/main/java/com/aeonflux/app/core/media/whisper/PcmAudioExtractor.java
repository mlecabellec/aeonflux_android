/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-004 / TSK-20260809-004.6 - Native PCM 16kHz Audio Extractor.
 */
package com.aeonflux.app.core.media.whisper;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260809-004.6] Native Android MediaCodec & MediaExtractor PCM Audio Stream Extractor.
 * Decodes remote HTTP audio URLs or local files (.m4a, .mp3, .aac, .wav) into raw 16kHz 16-bit
 * mono PCM float/short audio samples required for OpenAI Whisper (whisper.cpp) STT decoding.
 *
 * <p>The primary API is {@link #extractChunkPcm(String, long, long)} which performs seek-and-decode
 * on a bounded time window, enabling chunked streaming without any fixed memory cap.
 * The legacy {@link #extractPcmAudio(String, long)} method is retained for compatibility but
 * now internally caps at {@link #MAX_FULL_EXTRACT_DURATION_MS} to guard against OOM on very
 * long files when called without a time window.
 */
public class PcmAudioExtractor {

    private static final Logger LOGGER = Logger.getLogger(PcmAudioExtractor.class.getName());

    /** Output sample rate required by whisper.cpp. */
    public static final int TARGET_SAMPLE_RATE = 16000;

    /**
     * Hard ceiling on any single chunk decode: 30 seconds.
     * Enforced inside {@link #extractChunkPcm} — no caller (loop, VAD, or legacy path)
     * can bypass this limit. A warning is logged when clamping occurs.
     */
    public static final long MAX_CHUNK_DURATION_MS = 30_000L;

    /**
     * Safety ceiling for the legacy full-extract path: same as one chunk maximum.
     * Prefer {@link #extractChunkPcm} for any streaming use.
     */
    private static final long MAX_FULL_EXTRACT_DURATION_MS = MAX_CHUNK_DURATION_MS;

    /** MediaCodec dequeue timeout in microseconds. */
    private static final long CODEC_TIMEOUT_US = 10_000L;

    // -----------------------------------------------------------------------------------------
    // Logging helpers
    // -----------------------------------------------------------------------------------------

    private static void logDebug(String tag, String msg) {
        try {
            android.util.Log.d(tag, msg);
        } catch (Throwable ignored) {
            LOGGER.fine(msg);
        }
    }

    private static void logWarn(String tag, String msg) {
        try {
            android.util.Log.w(tag, msg);
        } catch (Throwable ignored) {
            LOGGER.warning(msg);
        }
    }

    private static void logError(String tag, String msg, Throwable t) {
        try {
            android.util.Log.e(tag, msg, t);
        } catch (Throwable ignored) {
            LOGGER.log(Level.WARNING, msg, t);
        }
    }

    // -----------------------------------------------------------------------------------------
    // PcmAudioBuffer
    // -----------------------------------------------------------------------------------------

    /**
     * Container holding raw 16kHz mono PCM float audio samples and metadata.
     */
    public static class PcmAudioBuffer {
        public final float[] samples;
        public final int sampleRate;
        public final long durationMs;

        public PcmAudioBuffer(float[] samples, int sampleRate, long durationMs) {
            this.samples = samples;
            this.sampleRate = sampleRate;
            this.durationMs = durationMs;
        }

        public int getSampleCount() {
            return samples.length;
        }

        @NonNull
        @Override
        public String toString() {
            return String.format("PcmAudioBuffer[Samples: %d, SampleRate: %dHz, Duration: %dms]",
                    samples.length, sampleRate, durationMs);
        }
    }

    // -----------------------------------------------------------------------------------------
    // Public API — chunked streaming (primary path)
    // -----------------------------------------------------------------------------------------

    /**
     * [PRIMARY API] Seek-and-decode a bounded time window from an audio stream.
     *
     * <p>Uses {@link MediaExtractor#seekTo} to jump directly to {@code startMs} before decoding,
     * then stops as soon as the encoded sample timestamp exceeds {@code endMs}.
     * Only the audio data for the requested chunk is decoded — completely eliminating the
     * 16MB fixed-buffer problem and enabling transcription of arbitrarily long audio files.
     *
     * @param audioUrl  Remote or local audio stream URI.
     * @param startMs   Start of the time window in milliseconds (inclusive).
     * @param endMs     End of the time window in milliseconds (exclusive).
     * @return {@link PcmAudioBuffer} containing 16kHz mono float samples for [startMs, endMs].
     *         Returns a silence buffer of the correct duration on any failure.
     */
    @NonNull
    public PcmAudioBuffer extractChunkPcm(@Nullable String audioUrl, long startMs, long endMs) {
        // --- Enforce hard chunk size limit (MAX_CHUNK_DURATION_MS) ---
        // This is the single authoritative enforcement point. No loop or caller can bypass it.
        long requestedWindowMs = Math.max(0L, endMs - startMs);
        if (requestedWindowMs > MAX_CHUNK_DURATION_MS) {
            long clampedEndMs = startMs + MAX_CHUNK_DURATION_MS;
            logWarn("AeonFlux_WhisperPCM", String.format(
                    "[PCM-CHUNK-CLAMP] Requested window %dms exceeds MAX_CHUNK_DURATION_MS (%dms). "
                    + "Clamping endMs from %dms to %dms.",
                    requestedWindowMs, MAX_CHUNK_DURATION_MS, endMs, clampedEndMs));
            endMs = clampedEndMs;
        }
        long windowMs = Math.max(0L, endMs - startMs);

        logDebug("AeonFlux_WhisperPCM", String.format(
                "[PCM-CHUNK-INIT] Seek-decode [%dms -> %dms] (%dms) from: %s",
                startMs, endMs, windowMs, audioUrl));

        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            logWarn("AeonFlux_WhisperPCM", "[PCM-CHUNK-INIT] Null/empty audioUrl. Returning silence.");
            return createSilenceBuffer(windowMs);
        }
        if (windowMs <= 0) {
            logWarn("AeonFlux_WhisperPCM", "[PCM-CHUNK-INIT] Zero-length window. Returning silence.");
            return createSilenceBuffer(0L);
        }

        MediaExtractor extractor = null;
        MediaCodec decoder = null;

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(audioUrl);

            // --- Track selection ---
            int trackIndex = -1;
            MediaFormat format = null;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat f = extractor.getTrackFormat(i);
                String mime = f.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("audio/")) {
                    trackIndex = i;
                    format = f;
                    break;
                }
            }
            if (trackIndex < 0 || format == null) {
                logWarn("AeonFlux_WhisperPCM", "[PCM-CHUNK-WARN] No audio track found. Returning silence.");
                return createSilenceBuffer(windowMs);
            }

            extractor.selectTrack(trackIndex);

            String mime = Objects.requireNonNull(format.getString(MediaFormat.KEY_MIME));
            int srcSampleRate = format.containsKey(MediaFormat.KEY_SAMPLE_RATE)
                    ? format.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 44100;
            int srcChannels = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)
                    ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 2;

            logDebug("AeonFlux_WhisperPCM", String.format(
                    "[PCM-CHUNK-FORMAT] Mime: %s | SampleRate: %dHz | Channels: %d",
                    mime, srcSampleRate, srcChannels));

            // --- Seek to chunk start (previous I-frame) ---
            long seekToUs = startMs * 1000L;
            extractor.seekTo(seekToUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            long endUs = endMs * 1000L;

            // --- Decoder setup ---
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, null, null, 0);
            decoder.start();

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            ByteArrayOutputStream pcmStream = new ByteArrayOutputStream();
            boolean isEOS = false;
            boolean inputDone = false;

            while (!isEOS) {
                // Feed encoded data into the decoder
                if (!inputDone) {
                    int inIdx = decoder.dequeueInputBuffer(CODEC_TIMEOUT_US);
                    if (inIdx >= 0) {
                        ByteBuffer inBuf = inputBuffers[inIdx];
                        long sampleTimeUs = extractor.getSampleTime();

                        if (sampleTimeUs < 0 || sampleTimeUs > endUs) {
                            // Past the window or end of stream: signal EOS to decoder
                            decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            int sampleSize = extractor.readSampleData(inBuf, 0);
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                inputDone = true;
                            } else {
                                decoder.queueInputBuffer(inIdx, 0, sampleSize, sampleTimeUs, 0);
                                extractor.advance();
                            }
                        }
                    }
                }

                // Pull decoded PCM output
                int outIdx = decoder.dequeueOutputBuffer(info, CODEC_TIMEOUT_US);
                if (outIdx == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = decoder.getOutputBuffers();
                } else if (outIdx >= 0) {
                    // Trim pre-roll frames from SEEK_TO_PREVIOUS_SYNC (allow 500ms tolerance)
                    boolean inWindow = (info.presentationTimeUs >= seekToUs - 500_000L);
                    if (inWindow && info.size > 0) {
                        ByteBuffer outBuf = outputBuffers[outIdx];
                        outBuf.position(info.offset);
                        byte[] chunk = new byte[info.size];
                        outBuf.get(chunk);
                        pcmStream.write(chunk);
                    }
                    decoder.releaseOutputBuffer(outIdx, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEOS = true;
                    }
                }
            }

            byte[] rawPcm = pcmStream.toByteArray();
            logDebug("AeonFlux_WhisperPCM", String.format(
                    "[PCM-CHUNK-COMPLETE] Decoded %d raw PCM bytes for window [%dms -> %dms]",
                    rawPcm.length, startMs, endMs));

            return processAndResampleTo16kHzMono(rawPcm, srcSampleRate, srcChannels, windowMs);

        } catch (Exception e) {
            logError("AeonFlux_WhisperPCM",
                    "[PCM-CHUNK-EXCEPT] Exception during seek-decode. Returning silence.", e);
            return createSilenceBuffer(windowMs);
        } finally {
            if (decoder != null) {
                try { decoder.stop(); decoder.release(); } catch (Throwable ignored) {}
            }
            if (extractor != null) {
                try { extractor.release(); } catch (Throwable ignored) {}
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Public API — legacy full-extract (kept for compatibility, capped at 5 minutes)
    // -----------------------------------------------------------------------------------------

    /**
     * [LEGACY] Extract PCM from the start of a stream up to {@link #MAX_FULL_EXTRACT_DURATION_MS}.
     * Prefer {@link #extractChunkPcm} for long-form audio.
     */
    @NonNull
    public PcmAudioBuffer extractPcmAudio(@Nullable String audioUrl, long fallbackDurationMs) {
        logDebug("AeonFlux_WhisperPCM",
                "[PCM-EXTRACT-INIT] Starting MediaExtractor decoding for audio URL: " + audioUrl);
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            logDebug("AeonFlux_WhisperPCM",
                    "[PCM-EXTRACT-INIT] Null/empty audioUrl. Returning fallback silence buffer.");
            return createSilenceBuffer(fallbackDurationMs);
        }

        long cappedDuration = Math.min(
                fallbackDurationMs > 0 ? fallbackDurationMs : MAX_FULL_EXTRACT_DURATION_MS,
                MAX_FULL_EXTRACT_DURATION_MS);

        if (fallbackDurationMs > MAX_FULL_EXTRACT_DURATION_MS) {
            logWarn("AeonFlux_WhisperPCM", String.format(
                    "[PCM-EXTRACT-WARN] Full-extract capped at %dms (requested %dms). "
                    + "Use extractChunkPcm() for long-form audio.",
                    MAX_FULL_EXTRACT_DURATION_MS, fallbackDurationMs));
        }

        return extractChunkPcm(audioUrl, 0L, cappedDuration);
    }

    // -----------------------------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------------------------

    @NonNull
    private PcmAudioBuffer processAndResampleTo16kHzMono(byte[] rawPcmBytes,
            int srcSampleRate, int srcChannels, long durationMs) {
        if (rawPcmBytes == null || rawPcmBytes.length < 2) {
            return createSilenceBuffer(durationMs);
        }

        ShortBuffer shortBuffer = ByteBuffer.wrap(rawPcmBytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer();
        int totalShorts = shortBuffer.remaining();
        int frameCount = totalShorts / Math.max(1, srcChannels);

        float[] resampledSamples;

        if (srcSampleRate == TARGET_SAMPLE_RATE && srcChannels == 1) {
            resampledSamples = new float[frameCount];
            for (int i = 0; i < frameCount; i++) {
                resampledSamples[i] = shortBuffer.get(i) / 32768.0f;
            }
        } else {
            double ratio = (double) TARGET_SAMPLE_RATE / (double) srcSampleRate;
            int targetLength = (int) (frameCount * ratio);
            resampledSamples = new float[targetLength];

            for (int i = 0; i < targetLength; i++) {
                double srcPos = i / ratio;
                int srcIdx = (int) srcPos;
                if (srcIdx < frameCount) {
                    float sum = 0.0f;
                    for (int c = 0; c < srcChannels; c++) {
                        int pos = srcIdx * srcChannels + c;
                        if (pos < totalShorts) {
                            sum += shortBuffer.get(pos) / 32768.0f;
                        }
                    }
                    resampledSamples[i] = sum / (float) srcChannels;
                }
            }
        }

        logDebug("AeonFlux_WhisperPCM", String.format(
                "[PCM-RESAMPLE] Resampled PCM to %dHz Mono Float Array. TotalSamples: %d",
                TARGET_SAMPLE_RATE, resampledSamples.length));
        return new PcmAudioBuffer(resampledSamples, TARGET_SAMPLE_RATE, durationMs);
    }

    @NonNull
    public PcmAudioBuffer createSilenceBuffer(long durationMs) {
        long effectiveDurationMs = durationMs > 0 ? durationMs : 300000L;
        int sampleCount = (int) ((effectiveDurationMs / 1000.0) * TARGET_SAMPLE_RATE);
        float[] silentSamples = new float[sampleCount];
        return new PcmAudioBuffer(silentSamples, TARGET_SAMPLE_RATE, effectiveDurationMs);
    }
}
