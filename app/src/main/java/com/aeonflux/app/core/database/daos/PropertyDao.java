/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.3 - Property DAO interface for Room queries.
 */
package com.aeonflux.app.core.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aeonflux.app.core.database.entities.PropertyEntity;

import java.util.List;

/**
 * [TSK-20260804-003.3] Data Access Object for EAV dynamic properties.
 */
@Dao
public interface PropertyDao {

    @Query("SELECT * FROM properties WHERE entity_type = :entityType AND entity_id = :entityId")
    List<PropertyEntity> getPropertiesForEntity(String entityType, String entityId);

    @Query("SELECT * FROM properties WHERE entity_type = :entityType AND entity_id = :entityId AND property_key = :key")
    PropertyEntity getProperty(String entityType, String entityId, String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProperty(PropertyEntity property);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProperties(List<PropertyEntity> properties);

    @Update
    void updateProperty(PropertyEntity property);

    @Delete
    void deleteProperty(PropertyEntity property);

    @Query("DELETE FROM properties WHERE entity_type = :entityType AND entity_id = :entityId AND property_key = :key")
    void deleteProperty(String entityType, String entityId, String key);

    @Query("DELETE FROM properties WHERE entity_type = :entityType AND entity_id = :entityId")
    void deleteAllPropertiesForEntity(String entityType, String entityId);
}
