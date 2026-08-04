/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - EAV Property entity for data model extensions.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * [TSK-20260804-003.2] EAV Property entity class for dynamically attaching properties to any entity.
 */
@Entity(
    tableName = "properties",
    indices = {
        @Index(value = {"entity_type", "entity_id", "property_key"}, unique = true)
    }
)
public class PropertyEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "entity_type")
    @NonNull
    public String entityType; // 'SOURCE', 'ARTICLE', 'KEYWORD', 'LABEL'

    @ColumnInfo(name = "entity_id")
    @NonNull
    public String entityId;

    @ColumnInfo(name = "property_key")
    @NonNull
    public String propertyKey;

    @ColumnInfo(name = "property_value")
    public String propertyValue;

    @ColumnInfo(name = "data_type")
    @NonNull
    public String dataType; // 'STRING', 'INTEGER', 'BOOLEAN', 'DOUBLE'

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public PropertyEntity() {
        this.entityType = "SOURCE";
        this.entityId = "";
        this.propertyKey = "";
        this.dataType = "STRING";
    }

    @androidx.room.Ignore
    public PropertyEntity(@NonNull String entityType,
                          @NonNull String entityId,
                          @NonNull String propertyKey,
                          String propertyValue,
                          @NonNull String dataType) {
        this.entityType = Objects.requireNonNull(entityType, "entityType must not be null");
        this.entityId = Objects.requireNonNull(entityId, "entityId must not be null");
        this.propertyKey = Objects.requireNonNull(propertyKey, "propertyKey must not be null");
        this.propertyValue = propertyValue;
        this.dataType = Objects.requireNonNull(dataType, "dataType must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyEntity that = (PropertyEntity) o;
        return id == that.id &&
               Objects.equals(entityType, that.entityType) &&
               Objects.equals(entityId, that.entityId) &&
               Objects.equals(propertyKey, that.propertyKey) &&
               Objects.equals(propertyValue, that.propertyValue) &&
               Objects.equals(dataType, that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entityType, entityId, propertyKey, propertyValue, dataType);
    }
}
