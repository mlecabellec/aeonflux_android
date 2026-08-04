/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Article Keyword join entity.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Objects;

/**
 * [TSK-20260804-003.2] Cross reference table connecting articles to keywords.
 */
@Entity(
    tableName = "article_keywords",
    primaryKeys = {"article_id", "keyword_id"},
    foreignKeys = {
        @ForeignKey(
            entity = ArticleEntity.class,
            parentColumns = "id",
            childColumns = "article_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = KeywordEntity.class,
            parentColumns = "id",
            childColumns = "keyword_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"article_id"}),
        @Index(value = {"keyword_id"})
    }
)
public class ArticleKeywordCrossRef {

    @ColumnInfo(name = "article_id")
    @NonNull
    public String articleId;

    @ColumnInfo(name = "keyword_id")
    @NonNull
    public String keywordId;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public ArticleKeywordCrossRef() {
        this.articleId = "";
        this.keywordId = "";
    }

    @androidx.room.Ignore
    public ArticleKeywordCrossRef(@NonNull String articleId, @NonNull String keywordId) {
        this.articleId = Objects.requireNonNull(articleId, "articleId must not be null");
        this.keywordId = Objects.requireNonNull(keywordId, "keywordId must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleKeywordCrossRef that = (ArticleKeywordCrossRef) o;
        return Objects.equals(articleId, that.articleId) && Objects.equals(keywordId, that.keywordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, keywordId);
    }
}
