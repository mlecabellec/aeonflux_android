/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Article entity representing feed items/articles.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Objects;

/**
 * [TSK-20260804-003.2] Entity class for items/articles belonging to a source.
 */
@Entity(
    tableName = "articles",
    foreignKeys = {
        @ForeignKey(
            entity = SourceEntity.class,
            parentColumns = {"id"},
            childColumns = {"source_id"},
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"source_id"}),
        @Index(value = {"published_at"})
    }
)
public class ArticleEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "source_id")
    @NonNull
    public String sourceId;

    @NonNull
    public String guid;

    @NonNull
    public String title;

    @ColumnInfo(name = "content_raw")
    public String contentRaw;

    @ColumnInfo(name = "content_cleaned")
    public String contentCleaned;

    public String author;

    @ColumnInfo(name = "published_at")
    public long publishedAt;

    @NonNull
    public String url;

    @ColumnInfo(name = "is_read")
    public int isRead = 0;

    @ColumnInfo(name = "is_bookmarked")
    public int isBookmarked = 0;

    // Podcasts
    @ColumnInfo(name = "media_url")
    public String mediaUrl;

    @ColumnInfo(name = "media_duration_ms")
    public Long mediaDurationMs;

    @ColumnInfo(name = "playback_position_ms")
    public long playbackPositionMs = 0;

    @ColumnInfo(name = "is_downloaded")
    public int isDownloaded = 0;

    @ColumnInfo(name = "local_media_path")
    public String localMediaPath;

    // Web Cache & AI
    @ColumnInfo(name = "cached_html_path")
    public String cachedHtmlPath;

    @ColumnInfo(name = "ai_summary")
    public String aiSummary;

    @ColumnInfo(name = "transcript_text")
    public String transcriptText;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public ArticleEntity() {
        this.id = "";
        this.sourceId = "";
        this.guid = "";
        this.title = "";
        this.url = "";
    }

    @androidx.room.Ignore
    public ArticleEntity(@NonNull String id,
                         @NonNull String sourceId,
                         @NonNull String guid,
                         @NonNull String title,
                         String contentRaw,
                         String contentCleaned,
                         String author,
                         long publishedAt,
                         @NonNull String url) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.guid = Objects.requireNonNull(guid, "guid must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.contentRaw = contentRaw;
        this.contentCleaned = contentCleaned;
        this.author = author;
        this.publishedAt = publishedAt;
        this.url = Objects.requireNonNull(url, "url must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleEntity that = (ArticleEntity) o;
        return publishedAt == that.publishedAt &&
               isRead == that.isRead &&
               isBookmarked == that.isBookmarked &&
               playbackPositionMs == that.playbackPositionMs &&
               isDownloaded == that.isDownloaded &&
               Objects.equals(id, that.id) &&
               Objects.equals(sourceId, that.sourceId) &&
               Objects.equals(guid, that.guid) &&
               Objects.equals(title, that.title) &&
               Objects.equals(contentRaw, that.contentRaw) &&
               Objects.equals(contentCleaned, that.contentCleaned) &&
               Objects.equals(author, that.author) &&
               Objects.equals(url, that.url) &&
               Objects.equals(mediaUrl, that.mediaUrl) &&
               Objects.equals(mediaDurationMs, that.mediaDurationMs) &&
               Objects.equals(localMediaPath, that.localMediaPath) &&
               Objects.equals(cachedHtmlPath, that.cachedHtmlPath) &&
               Objects.equals(aiSummary, that.aiSummary) &&
               Objects.equals(transcriptText, that.transcriptText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceId, guid, title, contentRaw, contentCleaned, author, publishedAt, url, isRead, isBookmarked, mediaUrl, mediaDurationMs, playbackPositionMs, isDownloaded, localMediaPath, cachedHtmlPath, aiSummary, transcriptText);
    }
}
