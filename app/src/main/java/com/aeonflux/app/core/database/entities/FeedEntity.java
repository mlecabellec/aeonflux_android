package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "feeds")
public class FeedEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String url;

    @NonNull
    public String title;

    public String description;

    @ColumnInfo(name = "icon_url")
    public String iconUrl;

    @ColumnInfo(name = "feed_type")
    @NonNull
    public String feedType; // 'RSS', 'PODCAST', 'BLUESKY'

    @ColumnInfo(name = "refresh_interval_minutes")
    public int refreshIntervalMinutes = 60;

    @ColumnInfo(name = "last_refreshed_at")
    public Long lastRefreshedAt;

    @ColumnInfo(name = "custom_tags")
    public String customTags; // JSON Array or comma-separated

    @ColumnInfo(name = "is_contributed_to_gae")
    public int isContributedToGae = 0;

    public FeedEntity() {
    }

    public FeedEntity(@NonNull String id, @NonNull String url, @NonNull String title, String description,
                      String iconUrl, @NonNull String feedType, int refreshIntervalMinutes,
                      Long lastRefreshedAt, String customTags, int isContributedToGae) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.feedType = feedType;
        this.refreshIntervalMinutes = refreshIntervalMinutes;
        this.lastRefreshedAt = lastRefreshedAt;
        this.customTags = customTags;
        this.isContributedToGae = isContributedToGae;
    }
}
