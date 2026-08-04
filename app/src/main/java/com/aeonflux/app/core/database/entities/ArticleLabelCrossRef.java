/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Article Label join entity.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Objects;

/**
 * [TSK-20260804-003.2] Cross reference table connecting articles to labels.
 */
@Entity(
    tableName = "article_labels",
    primaryKeys = {"article_id", "label_id"},
    foreignKeys = {
        @ForeignKey(
            entity = ArticleEntity.class,
            parentColumns = "id",
            childColumns = "article_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = LabelEntity.class,
            parentColumns = "id",
            childColumns = "label_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"article_id"}),
        @Index(value = {"label_id"})
    }
)
public class ArticleLabelCrossRef {

    @ColumnInfo(name = "article_id")
    @NonNull
    public String articleId;

    @ColumnInfo(name = "label_id")
    @NonNull
    public String labelId;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public ArticleLabelCrossRef() {
        this.articleId = "";
        this.labelId = "";
    }

    @androidx.room.Ignore
    public ArticleLabelCrossRef(@NonNull String articleId, @NonNull String labelId) {
        this.articleId = Objects.requireNonNull(articleId, "articleId must not be null");
        this.labelId = Objects.requireNonNull(labelId, "labelId must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleLabelCrossRef that = (ArticleLabelCrossRef) o;
        return Objects.equals(articleId, that.articleId) && Objects.equals(labelId, that.labelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, labelId);
    }
}
