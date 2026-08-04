/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Label entity for tags and categorization.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Objects;

/**
 * [TSK-20260804-003.2] Entity class for user/AI managed labels.
 */
@Entity(
    tableName = "labels",
    indices = {@Index(value = {"name"}, unique = true)}
)
public class LabelEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String name;

    public String color;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public LabelEntity() {
        this.id = "";
        this.name = "";
    }

    @androidx.room.Ignore
    public LabelEntity(@NonNull String id, @NonNull String name, String color) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelEntity that = (LabelEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }
}
