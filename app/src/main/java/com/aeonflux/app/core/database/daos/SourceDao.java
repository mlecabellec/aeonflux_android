/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Source DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aeonflux.app.core.database.entities.SourceEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for Source entities.
 */
@Dao
public interface SourceDao {

    @Query("SELECT * FROM sources ORDER BY title ASC")
    List<SourceEntity> getAllSources();

    @Query("SELECT * FROM sources ORDER BY title ASC")
    LiveData<List<SourceEntity>> getAllSourcesLiveData();

    @Query("SELECT * FROM sources WHERE id = :sourceId")
    SourceEntity getSourceById(String sourceId);

    @Query("SELECT * FROM sources WHERE source_type = :sourceType ORDER BY title ASC")
    List<SourceEntity> getSourcesByType(String sourceType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSource(SourceEntity source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSources(List<SourceEntity> sources);

    @Update
    void updateSource(SourceEntity source);

    @Delete
    void deleteSource(SourceEntity source);

    @Query("DELETE FROM sources WHERE id = :sourceId")
    void deleteSourceById(String sourceId);

    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY s.title ASC")
    LiveData<List<com.aeonflux.app.core.database.models.SourceWithUnreadCount>> getSourcesWithUnreadCountLiveData();

    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY s.title ASC")
    List<com.aeonflux.app.core.database.models.SourceWithUnreadCount> getSourcesWithUnreadCountAlphabeticalAsc();


    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY s.title DESC")
    List<com.aeonflux.app.core.database.models.SourceWithUnreadCount> getSourcesWithUnreadCountAlphabeticalDesc();

    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY s.last_refreshed_at ASC")
    List<com.aeonflux.app.core.database.models.SourceWithUnreadCount> getSourcesWithUnreadCountLastFetchAsc();

    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY s.last_refreshed_at DESC")
    List<com.aeonflux.app.core.database.models.SourceWithUnreadCount> getSourcesWithUnreadCountLastFetchDesc();

    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY lastArticleAt ASC")
    List<com.aeonflux.app.core.database.models.SourceWithUnreadCount> getSourcesWithUnreadCountLastArticleAsc();

    @Query("SELECT s.*, (SELECT COUNT(*) FROM articles a WHERE a.source_id = s.id AND a.is_read = 0) as unreadCount, (SELECT COALESCE(MAX(a.published_at), 0) FROM articles a WHERE a.source_id = s.id) as lastArticleAt FROM sources s ORDER BY lastArticleAt DESC")
    List<com.aeonflux.app.core.database.models.SourceWithUnreadCount> getSourcesWithUnreadCountLastArticleDesc();

    @Query("SELECT * FROM sources WHERE url = :url LIMIT 1")
    SourceEntity getSourceByUrl(String url);

    @Query("SELECT * FROM sources WHERE next_fetch_timestamp <= :currentTimestamp")
    List<SourceEntity> getSourcesDueForFetch(long currentTimestamp);

    @Query("DELETE FROM sources")
    void deleteAllSources();
}

