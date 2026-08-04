/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Unit test for database entities.
 */
package com.aeonflux.app.core.database.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * [TSK-20260804-003.2] Unit tests verifying contracts for all entity classes.
 */
public class EntityPojoTest {

    @Test
    public void testSourceEntityConstructorAndEquals() {
        SourceEntity source1 = new SourceEntity("src-1", "https://example.com/rss", "Sample Source", "Desc", "https://example.com/icon.png", "RSS", 30, 1000L, 0);
        SourceEntity source2 = new SourceEntity("src-1", "https://example.com/rss", "Sample Source", "Desc", "https://example.com/icon.png", "RSS", 30, 1000L, 0);
        SourceEntity source3 = new SourceEntity("src-2", "https://example.com/rss", "Sample Source", "Desc", "https://example.com/icon.png", "RSS", 30, 1000L, 0);

        assertEquals(source1, source2);
        assertEquals(source1.hashCode(), source2.hashCode());
        assertNotEquals(source1, source3);
    }

    @Test
    public void testSourceEntityNullRejection() {
        try {
            new SourceEntity(null, "https://example.com", "Title", null, null, "RSS", 60, null, 0);
            fail("Expected NullPointerException for null id");
        } catch (NullPointerException expected) {
            assertEquals("id must not be null", expected.getMessage());
        }
    }

    @Test
    public void testArticleEntityConstructorAndEquals() {
        ArticleEntity article1 = new ArticleEntity("art-1", "src-1", "guid-123", "Article Title", "raw", "clean", "Author", 2000L, "https://example.com/art-1");
        ArticleEntity article2 = new ArticleEntity("art-1", "src-1", "guid-123", "Article Title", "raw", "clean", "Author", 2000L, "https://example.com/art-1");

        assertEquals(article1, article2);
        assertEquals(article1.hashCode(), article2.hashCode());
    }

    @Test
    public void testKeywordAndLabelEntities() {
        KeywordEntity kw1 = new KeywordEntity("kw-1", "java");
        KeywordEntity kw2 = new KeywordEntity("kw-1", "java");
        assertEquals(kw1, kw2);

        LabelEntity lbl1 = new LabelEntity("lbl-1", "AI Summary", "#FF0000");
        LabelEntity lbl2 = new LabelEntity("lbl-1", "AI Summary", "#FF0000");
        assertEquals(lbl1, lbl2);
    }

    @Test
    public void testPropertyAndSecretEntities() {
        PropertyEntity prop1 = new PropertyEntity("SOURCE", "src-1", "priority", "high", "STRING");
        PropertyEntity prop2 = new PropertyEntity("SOURCE", "src-1", "priority", "high", "STRING");
        assertEquals(prop1, prop2);

        SourceSecretEntity secret1 = new SourceSecretEntity("src-1", "api_token", "encryptedBase64", "ivBase64");
        SourceSecretEntity secret2 = new SourceSecretEntity("src-1", "api_token", "encryptedBase64", "ivBase64");
        assertEquals(secret1, secret2);
    }

    @Test
    public void testSettingEntity() {
        SettingEntity set1 = new SettingEntity("theme", "dark");
        SettingEntity set2 = new SettingEntity("theme", "dark");
        assertEquals(set1, set2);
    }
}
