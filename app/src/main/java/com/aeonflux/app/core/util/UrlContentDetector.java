/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-002 / TSK-20260809-002.1 - 2-Tier URL Media Content-Type Detector.
 */
package com.aeonflux.app.core.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * [TSK-20260809-002.1] 2-Tier URL Content-Type Classification utility.
 * Tier 1: Instant regex pattern & file extension classifier.
 * Tier 2: Asynchronous HTTP HEAD request inspecting Content-Type MIME headers.
 */
public class UrlContentDetector {

    private static final Logger LOGGER = Logger.getLogger(UrlContentDetector.class.getName());

    public enum MediaType {
        AUDIO,
        VIDEO,
        GENERIC_WEBSITE
    }

    public interface Callback {
        void onDetected(@NonNull MediaType mediaType);
    }

    private static final Pattern AUDIO_EXT_PATTERN = Pattern.compile(".*\\.(mp3|m4a|aac|ogg|wav|flac|opus)(\\?.*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VIDEO_EXT_PATTERN = Pattern.compile(".*\\.(mp4|webm|mkv|m3u8|mov|avi)(\\?.*)?$", Pattern.CASE_INSENSITIVE);

    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * [TSK-20260809-002.1] Synchronous Tier-1 Pattern & Extension Classifier.
     */
    @NonNull
    public static MediaType detectTier1(@Nullable String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            LOGGER.fine("[TSK-20260809-002.1] Null or empty URL passed to detectTier1. Returning GENERIC_WEBSITE.");
            return MediaType.GENERIC_WEBSITE;
        }

        String url = urlString.trim().toLowerCase(Locale.US);

        if (AUDIO_EXT_PATTERN.matcher(url).matches() || url.contains("/podcast/") || url.contains("audio/")) {
            LOGGER.fine("[TSK-20260809-002.1] Tier-1 detected AUDIO for URL: " + urlString);
            return MediaType.AUDIO;
        }

        if (VIDEO_EXT_PATTERN.matcher(url).matches() || url.contains("youtube.com") || url.contains("youtu.be") || url.contains("vimeo.com")) {
            LOGGER.fine("[TSK-20260809-002.1] Tier-1 detected VIDEO for URL: " + urlString);
            return MediaType.VIDEO;
        }

        return MediaType.GENERIC_WEBSITE;
    }

    /**
     * [TSK-20260809-002.1] Asynchronous 2-Tier Content-Type Classifier.
     * Evaluates Tier-1 pattern first. If GENERIC_WEBSITE, performs background HTTP HEAD request.
     */
    public static void detectMediaTypeAsync(@Nullable String urlString, @NonNull Callback callback) {
        Objects.requireNonNull(callback, "callback must not be null");

        MediaType tier1Result = detectTier1(urlString);
        if (tier1Result != MediaType.GENERIC_WEBSITE || urlString == null || urlString.trim().isEmpty()) {
            callback.onDetected(tier1Result);
            return;
        }

        final String finalUrl = urlString.trim();

        BACKGROUND_EXECUTOR.execute(() -> {
            MediaType detected = MediaType.GENERIC_WEBSITE;
            HttpURLConnection connection = null;
            try {
                LOGGER.fine("[TSK-20260809-002.1] Initiating HTTP HEAD request for MIME detection: " + finalUrl);
                URL url = new URL(finalUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "AeonFlux/1.0 (MediaDetector)");

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 400) {
                    String contentType = connection.getContentType();
                    if (contentType != null) {
                        contentType = contentType.toLowerCase(Locale.US);
                        LOGGER.fine("[TSK-20260809-002.1] HTTP HEAD Content-Type: " + contentType);
                        if (contentType.startsWith("audio/")) {
                            detected = MediaType.AUDIO;
                        } else if (contentType.startsWith("video/")) {
                            detected = MediaType.VIDEO;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "[TSK-20260809-002.1] Exception during HTTP HEAD MIME detection for: " + finalUrl, e);
            } finally {
                if (connection != null) {
                    try {
                        connection.disconnect();
                    } catch (Exception ex) {
                        LOGGER.log(Level.FINE, "[TSK-20260809-002.1] Exception disconnecting HttpURLConnection", ex);
                    }
                }
            }

            final MediaType result = detected;
            callback.onDetected(result);
        });
    }
}
