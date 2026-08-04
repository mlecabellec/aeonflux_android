/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Setting entity for key-value application options.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * [TSK-20260804-003.2] Entity class for key-value settings.
 */
@Entity(tableName = "settings")
public class SettingEntity {

    @PrimaryKey
    @NonNull
    public String key;

    public String value;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public SettingEntity() {
        this.key = "";
    }

    @androidx.room.Ignore
    public SettingEntity(@NonNull String key, String value) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingEntity that = (SettingEntity) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
