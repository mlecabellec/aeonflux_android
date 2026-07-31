package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "feed_items",
    foreignKeys = {
        @ForeignKey(
            entity = FeedEntity.class,
            parentColumns = {"id"},
            childColumns = {"feed_id"},
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = {"feed_id"}),
        @Index(value = {"published_at"})
    }
)
public class FeedItemEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "feed_id")
    @NonNull
    public String feedId;

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

    public FeedItemEntity() {
    }
}
