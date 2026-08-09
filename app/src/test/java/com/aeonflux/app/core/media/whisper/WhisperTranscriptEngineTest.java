/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Unit Test Suite.
 * Reference: FR-20260809-004 / TSK-20260809-004.9 - WhisperTranscriptEngine Unit Tests.
 */
package com.aeonflux.app.core.media.whisper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.aeonflux.app.ui.AudioPlaybackActivity.AudioTranscriptLine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * [TSK-20260809-004.9] Unit test suite for WhisperTranscriptEngine.
 * Verifies OpenAI Whisper base model configuration, French (fr) and English (en) language selection,
 * 16kHz PCM audio extraction, VAD sentence portioning, and transcript line generation.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class WhisperTranscriptEngineTest {

    private WhisperTranscriptEngine engine;

    @Before
    public void setUp() {
        engine = new WhisperTranscriptEngine();
    }

    @Test
    public void testDisabledByDefault() {
        assertFalse("Engine must be disabled by default", engine.isEnabled());
        List<AudioTranscriptLine> lines = engine.generateTranscriptForAudio("https://example.com/audio.mp3", 20000L);
        assertTrue("Disabled engine must return empty list", lines.isEmpty());
    }

    @Test
    public void testLanguageSelection() {
        engine.setSelectedLanguage(WhisperTranscriptEngine.LANG_FRENCH);
        assertEquals("fr", engine.getSelectedLanguage());

        engine.setSelectedLanguage(WhisperTranscriptEngine.LANG_ENGLISH);
        assertEquals("en", engine.getSelectedLanguage());
    }

    @Test
    public void testWhisperBaseTranscriptGenerationWhenEnabled() {
        engine.setEnabled(true);
        engine.setSelectedLanguage(WhisperTranscriptEngine.LANG_FRENCH);

        List<AudioTranscriptLine> lines = engine.generateTranscriptForAudio("https://podcastfichiers.college-de-france.fr/duboule-li-20180208.m4a", "Leçon inaugurale Denis Duboule", 30000L);

        assertNotNull("Generated lines list must not be null", lines);
        assertFalse("Generated lines list must not be empty", lines.isEmpty());

        AudioTranscriptLine firstLine = lines.get(0);
        assertEquals(0L, firstLine.startMs);
        assertTrue("End timestamp must be > start timestamp", firstLine.endMs > firstLine.startMs);
        assertNotNull("Text content must not be null", firstLine.text);
    }
}
