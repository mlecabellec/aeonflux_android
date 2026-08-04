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

    @Query("DELETE FROM sources")
    void deleteAllSources();
}
