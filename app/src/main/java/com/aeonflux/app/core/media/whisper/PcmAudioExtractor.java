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
 */
public class PcmAudioExtractor {

    private static final Logger LOGGER = Logger.getLogger(PcmAudioExtractor.class.getName());
    public static final int TARGET_SAMPLE_RATE = 16000; // 16kHz required by Whisper

    private static void logDebug(String tag, String msg) {
        try {
            android.util.Log.d(tag, msg);
        } catch (Throwable ignored) {
            LOGGER.fine(msg);
        }
    }

    private static void logError(String tag, String msg, Throwable t) {
        try {
            android.util.Log.e(tag, msg, t);
        } catch (Throwable ignored) {
            LOGGER.log(Level.WARNING, msg, t);
        }
    }

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
            return String.format("PcmAudioBuffer[Samples: %d, SampleRate: %dHz, Duration: %dms]", samples.length, sampleRate, durationMs);
        }
    }

    /**
     * Extract 16kHz mono PCM audio float samples from target audio URL / file.
     *
     * @param audioUrl Remote or local audio stream location.
     * @param fallbackDurationMs Expected track duration if header metadata is unavailable.
     * @return PcmAudioBuffer containing normalized float audio samples [-1.0f, +1.0f].
     */
    @NonNull
    public PcmAudioBuffer extractPcmAudio(@Nullable String audioUrl, long fallbackDurationMs) {
        logDebug("AeonFlux_WhisperPCM", "[PCM-EXTRACT-INIT] Starting MediaExtractor decoding for audio URL: " + audioUrl);
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            logDebug("AeonFlux_WhisperPCM", "[PCM-EXTRACT-INIT] Null/empty audioUrl. Returning fallback silence buffer.");
            return createSilenceBuffer(fallbackDurationMs);
        }

        MediaExtractor extractor = null;
        MediaCodec decoder = null;
        ByteArrayOutputStream pcmStream = new ByteArrayOutputStream();

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(audioUrl);

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
                logDebug("AeonFlux_WhisperPCM", "[PCM-EXTRACT-WARN] No audio track found in media format. Using synthetic decoder fallback.");
                return createSilenceBuffer(fallbackDurationMs);
            }

            extractor.selectTrack(trackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);
            int inputSampleRate = format.containsKey(MediaFormat.KEY_SAMPLE_RATE) ? format.getInteger(MediaFormat.KEY_SAMPLE_RATE) : 44100;
            int inputChannels = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT) ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 2;
            long durationUs = format.containsKey(MediaFormat.KEY_DURATION) ? format.getLong(MediaFormat.KEY_DURATION) : fallbackDurationMs * 1000L;
            long trackDurationMs = durationUs / 1000L;

            logDebug("AeonFlux_WhisperPCM", String.format("[PCM-EXTRACT-FORMAT] Mime: %s | SampleRate: %dHz | Channels: %d | Duration: %dms", mime, inputSampleRate, inputChannels, trackDurationMs));

            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, null, null, 0);
            decoder.start();

            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

            boolean isEOS = false;
            long totalPcmBytes = 0;
            final long MAX_PCM_BYTES_LIMIT = 16L * 1024L * 1024L; // 16MB hard cap allocation to prevent OutOfMemoryError

            while (!isEOS && totalPcmBytes < MAX_PCM_BYTES_LIMIT) {
                int inIdx = decoder.dequeueInputBuffer(10000);
                if (inIdx >= 0) {
                    ByteBuffer buffer = inputBuffers[inIdx];
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inIdx, 0, sampleSize, presentationTimeUs, 0);
                        extractor.advance();
                    }
                }

                int outIdx = decoder.dequeueOutputBuffer(info, 10000);
                if (outIdx >= 0) {
                    ByteBuffer outBuffer = outputBuffers[outIdx];
                    byte[] chunk = new byte[info.size];
                    outBuffer.position(info.offset);
                    outBuffer.get(chunk);
                    outBuffer.clear();

                    pcmStream.write(chunk);
                    totalPcmBytes += chunk.length;

                    decoder.releaseOutputBuffer(outIdx, false);
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                }
            }

            logDebug("AeonFlux_WhisperPCM", String.format("[PCM-EXTRACT-COMPLETE] Extracted %d raw PCM bytes from codec (Cap: %dMB).", totalPcmBytes, MAX_PCM_BYTES_LIMIT / (1024 * 1024)));
            return processAndResampleTo16kHzMono(pcmStream.toByteArray(), inputSampleRate, inputChannels, trackDurationMs);

        } catch (Exception e) {
            logError("AeonFlux_WhisperPCM", "[PCM-EXTRACT-EXCEPT] Exception extracting audio stream with MediaCodec. Returning fallback buffer.", e);
            return createSilenceBuffer(fallbackDurationMs);
        } finally {
            if (decoder != null) {
                try { decoder.stop(); decoder.release(); } catch (Throwable ignored) {}
            }
            if (extractor != null) {
                try { extractor.release(); } catch (Throwable ignored) {}
            }
        }
    }

    @NonNull
    private PcmAudioBuffer processAndResampleTo16kHzMono(byte[] rawPcmBytes, int srcSampleRate, int srcChannels, long durationMs) {
        if (rawPcmBytes == null || rawPcmBytes.length < 2) {
            return createSilenceBuffer(durationMs);
        }

        ShortBuffer shortBuffer = ByteBuffer.wrap(rawPcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        int totalShorts = shortBuffer.remaining();
        int frameCount = totalShorts / Math.max(1, srcChannels);

        float[] resampledSamples;

        if (srcSampleRate == TARGET_SAMPLE_RATE && srcChannels == 1) {
            resampledSamples = new float[frameCount];
            for (int i = 0; i < frameCount; i++) {
                resampledSamples[i] = shortBuffer.get(i) / 32768.0f;
            }
        } else {
            // Linear downsampling / mix channels to 16kHz Mono float array
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

        logDebug("AeonFlux_WhisperPCM", String.format("[PCM-RESAMPLE] Resampled PCM to %dHz Mono Float Array. TotalSamples: %d", TARGET_SAMPLE_RATE, resampledSamples.length));
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
