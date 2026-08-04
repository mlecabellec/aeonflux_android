/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Setting DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.aeonflux.app.core.database.entities.SettingEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for settings key-value store.
 */
@Dao
public interface SettingDao {

    @Query("SELECT * FROM settings")
    List<SettingEntity> getAllSettings();

    @Query("SELECT * FROM settings")
    LiveData<List<SettingEntity>> getAllSettingsLiveData();

    @Query("SELECT * FROM settings WHERE `key` = :key")
    SettingEntity getSetting(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSetting(SettingEntity setting);

    @Delete
    void deleteSetting(SettingEntity setting);

    @Query("DELETE FROM settings WHERE `key` = :key")
    void deleteSetting(String key);
}
