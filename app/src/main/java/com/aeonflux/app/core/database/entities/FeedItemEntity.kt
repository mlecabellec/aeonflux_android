package com.aeonflux.app.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feed_items",
    foreignKeys = [
        ForeignKey(
            entity = FeedEntity::class,
            parentColumns = ["id"],
            childColumns = ["feed_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["feed_id"]),
        Index(value = ["published_at"])
    ]
)
data class FeedItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "feed_id") val feedId: String,
    val guid: String,
    val title: String,
    @ColumnInfo(name = "content_raw") val contentRaw: String?,
    @ColumnInfo(name = "content_cleaned") val contentCleaned: String?,
    val author: String?,
    @ColumnInfo(name = "published_at") val publishedAt: Long,
    val url: String,
    @ColumnInfo(name = "is_read") val isRead: Int = 0,
    @ColumnInfo(name = "is_bookmarked") val isBookmarked: Int = 0,
    
    // Podcasts
    @ColumnInfo(name = "media_url") val mediaUrl: String?,
    @ColumnInfo(name = "media_duration_ms") val mediaDurationMs: Long?,
    @ColumnInfo(name = "playback_position_ms") val playbackPositionMs: Long = 0,
    @ColumnInfo(name = "is_downloaded") val isDownloaded: Int = 0,
    @ColumnInfo(name = "local_media_path") val localMediaPath: String?,
    
    // Web Cache & AI
    @ColumnInfo(name = "cached_html_path") val cachedHtmlPath: String?,
    @ColumnInfo(name = "ai_summary") val aiSummary: String?,
    @ColumnInfo(name = "transcript_text") val transcriptText: String?
)
