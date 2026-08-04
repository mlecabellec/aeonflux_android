/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Keyword entity for extracted article terms.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Objects;

/**
 * [TSK-20260804-003.2] Entity class for extracted keywords.
 */
@Entity(
    tableName = "keywords",
    indices = {@Index(value = {"keyword"}, unique = true)}
)
public class KeywordEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String keyword;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public KeywordEntity() {
        this.id = "";
        this.keyword = "";
    }

    @androidx.room.Ignore
    public KeywordEntity(@NonNull String id, @NonNull String keyword) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.keyword = Objects.requireNonNull(keyword, "keyword must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeywordEntity that = (KeywordEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(keyword, that.keyword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keyword);
    }
}
