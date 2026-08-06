/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-001 / TSK-20260806-001 - Unit tests for multi-flavor OPML parser & exporter.
 */
package com.aeonflux.app.core.opml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.aeonflux.app.core.database.entities.SourceEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class OpmlParserTest {

    private OpmlParser parser;
    private OpmlExporter exporter;

    @Before
    public void setUp() {
        parser = new OpmlParser();
        exporter = new OpmlExporter();
    }

    @Test
    public void testParsePodcastsOpmlXmlResource() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("podcasts_opml.xml");
        assertNotNull("podcasts_opml.xml resource must exist", is);

        List<OpmlItem> items = parser.parse(is);
        assertNotNull(items);

        OpmlTreeFlattener flattener = new OpmlTreeFlattener();
        List<OpmlItem> feeds = flattener.extractAllFeeds(items);
        assertNotNull(feeds);
        assertFalse("Parsed feeds should not be empty", feeds.isEmpty());
        assertTrue("Should parse all podcast RSS feeds from nested outlines", feeds.size() >= 50);
    }

    @Test
    public void testParseFeederExportOpmlResource() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("feeder-export-2026-01-20-67634.opml");
        assertNotNull("feeder-export-2026-01-20-67634.opml resource must exist", is);

        List<OpmlItem> items = parser.parse(is);
        assertNotNull(items);

        OpmlTreeFlattener flattener = new OpmlTreeFlattener();
        List<OpmlItem> feeds = flattener.extractAllFeeds(items);
        assertNotNull(feeds);
        assertFalse("Parsed feeds should not be empty", feeds.isEmpty());
        assertTrue("Should extract all feeds across all categories in feeder-export OPML", feeds.size() >= 100);
    }

    @Test
    public void testExportAndReParseOpml() throws Exception {
        SourceEntity source = new SourceEntity("s1", "https://test.com/rss", "Test Feed", "Desc", null, "RSS", 60, System.currentTimeMillis(), 0);
        Map<String, List<SourceEntity>> map = new HashMap<>();
        map.put("Tech", Collections.singletonList(source));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.export(map, baos);

        String xmlText = baos.toString("UTF-8");
        assertTrue(xmlText.contains("https://test.com/rss"));
        assertTrue(xmlText.contains("Test Feed"));

        ByteArrayInputStream bais = new ByteArrayInputStream(xmlText.getBytes(StandardCharsets.UTF_8));
        List<OpmlItem> items = parser.parse(bais);

        assertNotNull(items);
        assertFalse(items.isEmpty());

        OpmlTreeFlattener flattener = new OpmlTreeFlattener();
        List<OpmlItem> extracted = flattener.extractAllFeeds(items);
        assertEquals(1, extracted.size());
        assertEquals("https://test.com/rss", extracted.get(0).getXmlUrl());
    }
}

