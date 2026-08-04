/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.6 - Integration unit tests for DatabaseService.
 */
package com.aeonflux.app.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.aeonflux.app.core.database.entities.ArticleEntity;
import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.security.CryptographyManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * [TSK-20260804-003.6] Unit and Integration test suite for DatabaseService.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DatabaseServiceTest {

    private AppDatabase database;
    private CryptographyManager cryptoManager;
    private DatabaseService databaseService;

    @Before
    public void setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase.class
        ).allowMainThreadQueries().build();

        cryptoManager = new CryptographyManager();
        databaseService = new DatabaseService(database, cryptoManager);
    }

    @After
    public void tearDown() {
        if (database != null) {
            database.close();
        }
    }

    @Test
    public void testSourceCrudAndObserverHooks() {
        List<SourceEntity> createdList = new ArrayList<>();
        List<SourceEntity> updatedList = new ArrayList<>();
        List<SourceEntity> deletedList = new ArrayList<>();

        DatabaseService.OnEntityChangeListener<SourceEntity> listener = new DatabaseService.OnEntityChangeListener<SourceEntity>() {
            @Override
            public void onCreated(SourceEntity entity) {
                createdList.add(entity);
            }

            @Override
            public void onUpdated(SourceEntity entity) {
                updatedList.add(entity);
            }

            @Override
            public void onDeleted(SourceEntity entity) {
                deletedList.add(entity);
            }
        };

        databaseService.registerListener(SourceEntity.class, listener);

        SourceEntity source = new SourceEntity(
            "src-100",
            "https://feed.example.com/rss",
            "Tech News",
            "Daily news",
            "https://feed.example.com/icon.png",
            "RSS",
            30,
            1000000L,
            0
        );

        // Test Insert
        databaseService.insertSource(source);
        assertEquals(1, createdList.size());
        assertEquals("src-100", createdList.get(0).id);

        SourceEntity fetched = databaseService.getSourceById("src-100");
        assertNotNull(fetched);
        assertEquals("Tech News", fetched.title);

        // Test Update
        fetched.title = "Tech News Updated";
        databaseService.updateSource(fetched);
        assertEquals(1, updatedList.size());
        assertEquals("Tech News Updated", databaseService.getSourceById("src-100").title);

        // Test Delete
        databaseService.deleteSource(fetched);
        assertEquals(1, deletedList.size());
        assertNull(databaseService.getSourceById("src-100"));

        databaseService.unregisterListener(SourceEntity.class, listener);
    }

    @Test
    public void testArticleCrudOperations() {
        SourceEntity source = new SourceEntity("src-200", "https://rss.org", "Podcast Source", null, null, "PODCAST", 60, null, 0);
        databaseService.insertSource(source);

        ArticleEntity article = new ArticleEntity(
            "art-500",
            "src-200",
            "guid-500",
            "Episode #1",
            "<p>Raw Content</p>",
            "Raw Content",
            "Host",
            1600000000000L,
            "https://rss.org/ep1"
        );

        databaseService.insertArticle(article);

        ArticleEntity fetched = databaseService.getArticleById("art-500");
        assertNotNull(fetched);
        assertEquals("Episode #1", fetched.title);

        List<ArticleEntity> articles = databaseService.getArticlesForSource("src-200");
        assertEquals(1, articles.size());
        assertEquals("art-500", articles.get(0).id);
    }

    @Test
    public void testDynamicPropertiesEav() {
        databaseService.setProperty("SOURCE", "src-1", "priority", "10", "INTEGER");
        databaseService.setProperty("SOURCE", "src-1", "is_active", "true", "BOOLEAN");
        databaseService.setProperty("SOURCE", "src-1", "rating", "4.85", "DOUBLE");

        assertEquals(10, databaseService.getPropertyInt("SOURCE", "src-1", "priority", 0));
        assertTrue(databaseService.getPropertyBoolean("SOURCE", "src-1", "is_active", false));
        assertEquals(4.85, databaseService.getPropertyDouble("SOURCE", "src-1", "rating", 0.0), 0.001);
        assertEquals("10", databaseService.getPropertyString("SOURCE", "src-1", "priority"));

        assertEquals(99, databaseService.getPropertyInt("SOURCE", "src-1", "non_existent", 99));
    }

    @Test
    public void testCipheredSecretsManagement() {
        SourceEntity source = new SourceEntity("src-300", "https://api.bluesky.com", "BlueSky Feed", null, null, "BLUESKY", 15, null, 0);
        databaseService.insertSource(source);

        String secretToken = "bsky-api-jwt-secret-key-987654321";
        databaseService.setSecret("src-300", "auth_token", secretToken);

        String retrievedSecret = databaseService.getSecret("src-300", "auth_token");
        assertEquals(secretToken, retrievedSecret);

        databaseService.deleteSecret("src-300", "auth_token");
        assertNull(databaseService.getSecret("src-300", "auth_token"));
    }

    @Test
    public void testKeyValuesSettings() {
        databaseService.setSetting("custom_setting", "value_xyz");
        assertEquals("value_xyz", databaseService.getSetting("custom_setting"));
    }

    @Test
    public void testTransactionManagement() {
        databaseService.runInTransaction(() -> {
            SourceEntity s = new SourceEntity("src-tx", "https://tx.com", "Tx Source", null, null, "RSS", 60, null, 0);
            databaseService.insertSource(s);
        });

        assertNotNull(databaseService.getSourceById("src-tx"));
    }
}
