package com.aeonflux.app.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
    @ColumnInfo(name = "feed_type") val feedType: String, // 'RSS', 'PODCAST', 'BLUESKY'
    @ColumnInfo(name = "refresh_interval_minutes") val refreshIntervalMinutes: Int = 60,
    @ColumnInfo(name = "last_refreshed_at") val lastRefreshedAt: Long?,
    @ColumnInfo(name = "custom_tags") val customTags: String?, // JSON Array or comma-separated
    @ColumnInfo(name = "is_contributed_to_gae") val isContributedToGae: Int = 0
)
