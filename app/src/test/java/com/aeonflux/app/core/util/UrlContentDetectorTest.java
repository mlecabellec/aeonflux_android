/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Unit Test Suite.
 * Reference: FR-20260809-002 / TSK-20260809-002.6 - UrlContentDetector Unit Tests.
 */
package com.aeonflux.app.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * [TSK-20260809-002.6] Unit tests for UrlContentDetector classification logic.
 */
public class UrlContentDetectorTest {

    @Test
    public void testDetectAudioUrls() {
        assertEquals(UrlContentDetector.MediaType.AUDIO, UrlContentDetector.detectTier1("https://example.com/podcast/episode1.mp3"));
        assertEquals(UrlContentDetector.MediaType.AUDIO, UrlContentDetector.detectTier1("https://example.com/audio/sample.m4a?token=abc"));
        assertEquals(UrlContentDetector.MediaType.AUDIO, UrlContentDetector.detectTier1("https://example.com/podcast/feed"));
    }

    @Test
    public void testDetectVideoUrls() {
        assertEquals(UrlContentDetector.MediaType.VIDEO, UrlContentDetector.detectTier1("https://youtube.com/watch?v=dQw4w9WgXcQ"));
        assertEquals(UrlContentDetector.MediaType.VIDEO, UrlContentDetector.detectTier1("https://youtu.be/dQw4w9WgXcQ"));
        assertEquals(UrlContentDetector.MediaType.VIDEO, UrlContentDetector.detectTier1("https://example.com/video/stream.mp4"));
        assertEquals(UrlContentDetector.MediaType.VIDEO, UrlContentDetector.detectTier1("https://vimeo.com/123456789"));
    }

    @Test
    public void testDetectGenericWebsiteUrls() {
        assertEquals(UrlContentDetector.MediaType.GENERIC_WEBSITE, UrlContentDetector.detectTier1("https://news.ycombinator.com"));
        assertEquals(UrlContentDetector.MediaType.GENERIC_WEBSITE, UrlContentDetector.detectTier1("https://bsky.app/profile/user.bsky.social"));
        assertEquals(UrlContentDetector.MediaType.GENERIC_WEBSITE, UrlContentDetector.detectTier1(null));
        assertEquals(UrlContentDetector.MediaType.GENERIC_WEBSITE, UrlContentDetector.detectTier1("   "));
    }
}
