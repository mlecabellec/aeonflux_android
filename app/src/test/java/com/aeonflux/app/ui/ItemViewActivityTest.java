/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Unit Test Suite Compliance.
 * Reference: FR-20260809-001 / TSK-20260809-001.5 - ItemViewActivity Test Suite.
 */
package com.aeonflux.app.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * [TSK-20260809-001.5] Unit and contract verification tests for ItemViewActivity.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ItemViewActivityTest {

    @Test
    public void testExtraConstantsContract() {
        assertEquals("extra_article_id", ItemViewActivity.EXTRA_ARTICLE_ID);
        assertEquals("extra_title", ItemViewActivity.EXTRA_TITLE);
        assertEquals("extra_summary", ItemViewActivity.EXTRA_SUMMARY);
        assertEquals("extra_url", ItemViewActivity.EXTRA_URL);
        assertEquals("extra_author", ItemViewActivity.EXTRA_AUTHOR);
        assertEquals("extra_published_at", ItemViewActivity.EXTRA_PUBLISHED_AT);
        assertEquals("extra_is_read", ItemViewActivity.EXTRA_IS_READ);
    }

    @Test
    public void testIntentCreationForEmbeddedWebView() {
        Intent intent = new Intent();
        intent.putExtra(ItemViewActivity.EXTRA_URL, "https://aeonflux.dev/test");
        intent.putExtra(ItemViewActivity.EXTRA_TITLE, "Test Article Title");

        assertNotNull(intent.getStringExtra(ItemViewActivity.EXTRA_URL));
        assertEquals("https://aeonflux.dev/test", intent.getStringExtra(ItemViewActivity.EXTRA_URL));
        assertEquals("Test Article Title", intent.getStringExtra(ItemViewActivity.EXTRA_TITLE));
    }

    @Test
    public void testIntentCreationForPrivateWebView() {
        Intent intent = new Intent();
        intent.putExtra(PrivateWebViewActivity.EXTRA_URL, "https://aeonflux.dev/private-test");
        intent.putExtra(PrivateWebViewActivity.EXTRA_TITLE, "Private Test Title");

        assertNotNull(intent.getStringExtra(PrivateWebViewActivity.EXTRA_URL));
        assertEquals("https://aeonflux.dev/private-test", intent.getStringExtra(PrivateWebViewActivity.EXTRA_URL));
        assertEquals("Private Test Title", intent.getStringExtra(PrivateWebViewActivity.EXTRA_TITLE));
    }
}
