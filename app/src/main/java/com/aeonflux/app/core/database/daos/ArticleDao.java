/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Article DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aeonflux.app.core.database.entities.ArticleEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for Article entities.
 */
@Dao
public interface ArticleDao {

    @Query("SELECT * FROM articles WHERE source_id = :sourceId ORDER BY published_at DESC")
    List<ArticleEntity> getArticlesForSource(String sourceId);

    @Query("SELECT * FROM articles WHERE source_id = :sourceId ORDER BY published_at DESC")
    LiveData<List<ArticleEntity>> getArticlesForSourceLiveData(String sourceId);

    @Query("SELECT * FROM articles WHERE id = :articleId")
    ArticleEntity getArticleById(String articleId);

    @Query("SELECT * FROM articles WHERE is_bookmarked = 1 ORDER BY published_at DESC")
    List<ArticleEntity> getBookmarkedArticles();

    @Query("SELECT * FROM articles WHERE is_read = 0 ORDER BY published_at DESC")
    List<ArticleEntity> getUnreadArticles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(ArticleEntity article);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticles(List<ArticleEntity> articles);

    @Update
    void updateArticle(ArticleEntity article);

    @Query("UPDATE articles SET is_read = :isRead WHERE id = :articleId")
    void updateReadStatus(String articleId, int isRead);

    @Query("UPDATE articles SET is_bookmarked = :isBookmarked WHERE id = :articleId")
    void updateBookmarkStatus(String articleId, int isBookmarked);

    @Query("UPDATE articles SET playback_position_ms = :positionMs WHERE id = :articleId")
    void updatePlaybackPosition(String articleId, long positionMs);

    @Delete
    void deleteArticle(ArticleEntity article);

    @Query("DELETE FROM articles WHERE id = :articleId")
    void deleteArticleById(String articleId);

    @Query("DELETE FROM articles WHERE source_id = :sourceId")
    void deleteArticlesForSource(String sourceId);
}
