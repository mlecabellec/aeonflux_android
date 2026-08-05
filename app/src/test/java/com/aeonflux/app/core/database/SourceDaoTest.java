/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & DAO Unit Test Suite
 * Reference: REQ-00020 / REQ-00050 / TSK-20260805-001 - SourceDao and ArticleDao Unit Test
 */
package com.aeonflux.app.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.aeonflux.app.core.database.daos.ArticleDao;
import com.aeonflux.app.core.database.daos.SourceDao;
import com.aeonflux.app.core.database.entities.ArticleEntity;
import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.database.models.SourceWithUnreadCount;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * [TSK-20260805-001] Unit test suite for SourceDao and ArticleDao queries.
 */
public class SourceDaoTest {

    private SourceEntity s1;
    private SourceEntity s2;
    private ArticleEntity a1;
    private ArticleEntity a2;

    @Before
    public void setUp() {
        s1 = new SourceEntity("s_1", "https://feed1.com", "Beta Feed", "Desc 1", null, "RSS", 60, 1000L, 0);
        s2 = new SourceEntity("s_2", "https://feed2.com", "Alpha Feed", "Desc 2", null, "RSS", 60, 2000L, 0);

        a1 = new ArticleEntity("a_1", "s_1", "g_1", "Article 1", "Content", "Content", "Author 1", 5000L, "https://link1.com");
        a1.isRead = 0;

        a2 = new ArticleEntity("a_2", "s_1", "g_2", "Article 2", "Content", "Content", "Author 2", 9000L, "https://link2.com");
        a2.isRead = 1;
    }

    @Test
    public void testSourceWithUnreadCountModel() {
        SourceWithUnreadCount dto = new SourceWithUnreadCount(s1, 1, 9000L);
        assertNotNull(dto.source);
        assertEquals("Beta Feed", dto.source.title);
        assertEquals(1, dto.unreadCount);
        assertEquals(9000L, dto.lastArticleAt);
    }
}
