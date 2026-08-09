/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Unit Test Suite.
 * Reference: FR-20260809-004 / TSK-20260809-004.4 - AudioSilenceDetector Unit Tests & Contiguity Verification.
 */
package com.aeonflux.app.core.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aeonflux.app.core.media.AudioSilenceDetector.AudioSegment;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

/**
 * [TSK-20260809-004.4] Unit test suite for AudioSilenceDetector VAD sentence splitting,
 * timeline contiguity (zero holes/gaps), and dynamic duration distribution.
 */
public class AudioSilenceDetectorTest {

    private static final Logger LOGGER = Logger.getLogger(AudioSilenceDetectorTest.class.getName());
    private AudioSilenceDetector silenceDetector;

    @Before
    public void setUp() {
        silenceDetector = new AudioSilenceDetector();
    }

    @Test
    public void testEmptyDurationReturnsEmptyList() {
        List<AudioSegment> segments = silenceDetector.detectSentencesAndSplit(0L);
        LOGGER.info("[TEST-VAD-STATISTICS] Duration 0ms -> Segments count: " + segments.size());
        assertTrue("Zero duration must return empty segments list", segments.isEmpty());
    }

    @Test
    public void testShortDurationSingleSegment() {
        List<AudioSegment> segments = silenceDetector.detectSentencesAndSplit(3500L);
        LOGGER.info("[TEST-VAD-STATISTICS] Short Duration 3500ms -> Segment 0: " + (segments.isEmpty() ? "None" : segments.get(0).toString()));
        assertEquals(1, segments.size());
        assertEquals(0L, segments.get(0).startMs);
        assertEquals(3500L, segments.get(0).endMs);
    }

    @Test
    public void testContiguousTimelineNoHolesOrGaps() {
        long durationMs = 3839745L; // 1-hour lecture (3,839,745ms)
        List<AudioSegment> segments = silenceDetector.detectSentencesAndSplit(durationMs);

        assertFalse("Segments list must not be empty", segments.isEmpty());
        assertEquals("First segment must start at 0ms", 0L, segments.get(0).startMs);
        assertEquals("Last segment must end at total duration", durationMs, segments.get(segments.size() - 1).endMs);

        // Mathematical Proof: Assert zero holes or gaps between consecutive segments
        for (int i = 0; i < segments.size() - 1; i++) {
            AudioSegment current = segments.get(i);
            AudioSegment next = segments.get(i + 1);
            assertEquals("Segment endMs must equal next segment startMs (zero timeline gap)", current.endMs, next.startMs);
        }

        LOGGER.info("[CONTIGUITY-PROOF] Verified " + segments.size() + " segments across " + durationMs + "ms timeline with ZERO holes or gaps!");
    }

    @Test
    public void testDynamicSentencePauseDistribution() {
        long durationMs = 3839745L;
        List<AudioSegment> segments = silenceDetector.detectSentencesAndSplit(durationMs);

        LOGGER.info("================== VAD SILENCE DETECTOR DIAGNOSTIC STATISTICS ==================");
        LOGGER.info("[VAD-STATS] Audio Total Duration: " + durationMs + " ms (" + (durationMs / 1000) + " s)");
        LOGGER.info("[VAD-STATS] Total Sentences / Portions Produced: " + segments.size());
        LOGGER.info("[VAD-STATS] Pause Silence Threshold: " + AudioSilenceDetector.PAUSE_THRESHOLD_MS + " ms");
        LOGGER.info("[VAD-STATS] Min Portion Duration Constraint: " + AudioSilenceDetector.MIN_SEGMENT_MS + " ms");
        LOGGER.info("[VAD-STATS] Max Portion Duration Constraint: " + AudioSilenceDetector.MAX_SEGMENT_MS + " ms");

        boolean foundVariedDuration = false;
        long firstDur = segments.get(0).getDurationMs();

        for (int i = 0; i < segments.size(); i++) {
            AudioSegment segment = segments.get(i);
            long segmentDuration = segment.getDurationMs();

            if (segmentDuration != firstDur) {
                foundVariedDuration = true;
            }

            if (i < 8 || i >= segments.size() - 5) {
                LOGGER.info(String.format("[VAD-PART-SAMPLE] Part #%03d: start=%6dms, end=%6dms, duration=%5dms", i, segment.startMs, segment.endMs, segmentDuration));
            }

            assertTrue("Segment duration must be >= 4s or final segment", segmentDuration >= AudioSilenceDetector.MIN_SEGMENT_MS || segment.endMs == durationMs);
            assertTrue("Segment duration must be <= 20s hard cap", segmentDuration <= AudioSilenceDetector.MAX_SEGMENT_MS);
        }

        assertTrue("VAD sentence portions must have dynamic natural sentence durations", foundVariedDuration);
        LOGGER.info("================================================================================");
    }
}
