/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: TSK-20260804-003.2 - Source entity representing ingestion endpoints.
 */
package com.aeonflux.app.core.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Objects;

/**
 * [TSK-20260804-003.2] Entity class for content sources (RSS, PodCast, BlueSky, etc.).
 */
@Entity(tableName = "sources")
public class SourceEntity {

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

    @ColumnInfo(name = "source_type")
    @NonNull
    public String sourceType; // 'RSS', 'PODCAST', 'BLUESKY'

    @ColumnInfo(name = "refresh_interval_minutes")
    public int refreshIntervalMinutes = 60;

    @ColumnInfo(name = "last_refreshed_at")
    public Long lastRefreshedAt;

    @ColumnInfo(name = "is_contributed_to_gae")
    public int isContributedToGae = 0;

    @ColumnInfo(name = "cron_expression")
    public String cronExpression; // e.g. "0 */2 * * *" or minute intervals

    @ColumnInfo(name = "last_fetch_status")
    public String lastFetchStatus; // "SUCCESS", "ERROR: <msg>"

    @ColumnInfo(name = "next_fetch_timestamp")
    public Long nextFetchTimestamp;

    /* TSK-20260804-003.2 - Default Constructor required by Room */
    public SourceEntity() {
        this.id = "";
        this.url = "";
        this.title = "";
        this.sourceType = "RSS";
        this.cronExpression = "0 */1 * * *";
        this.lastFetchStatus = "PENDING";
        this.nextFetchTimestamp = 0L;
    }

    @androidx.room.Ignore
    public SourceEntity(@NonNull String id,
                        @NonNull String url,
                        @NonNull String title,
                        String description,
                        String iconUrl,
                        @NonNull String sourceType,
                        int refreshIntervalMinutes,
                        Long lastRefreshedAt,
                        int isContributedToGae) {
        this(id, url, title, description, iconUrl, sourceType, refreshIntervalMinutes, lastRefreshedAt, isContributedToGae, "0 */1 * * *", "PENDING", 0L);
    }

    @androidx.room.Ignore
    public SourceEntity(@NonNull String id,
                        @NonNull String url,
                        @NonNull String title,
                        String description,
                        String iconUrl,
                        @NonNull String sourceType,
                        int refreshIntervalMinutes,
                        Long lastRefreshedAt,
                        int isContributedToGae,
                        String cronExpression,
                        String lastFetchStatus,
                        Long nextFetchTimestamp) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = description;
        this.iconUrl = iconUrl;
        this.sourceType = Objects.requireNonNull(sourceType, "sourceType must not be null");
        this.refreshIntervalMinutes = refreshIntervalMinutes;
        this.lastRefreshedAt = lastRefreshedAt;
        this.isContributedToGae = isContributedToGae;
        this.cronExpression = cronExpression != null ? cronExpression : "0 */1 * * *";
        this.lastFetchStatus = lastFetchStatus != null ? lastFetchStatus : "PENDING";
        this.nextFetchTimestamp = nextFetchTimestamp != null ? nextFetchTimestamp : 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceEntity that = (SourceEntity) o;
        return refreshIntervalMinutes == that.refreshIntervalMinutes &&
               isContributedToGae == that.isContributedToGae &&
               Objects.equals(id, that.id) &&
               Objects.equals(url, that.url) &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               Objects.equals(iconUrl, that.iconUrl) &&
               Objects.equals(sourceType, that.sourceType) &&
               Objects.equals(lastRefreshedAt, that.lastRefreshedAt) &&
               Objects.equals(cronExpression, that.cronExpression) &&
               Objects.equals(lastFetchStatus, that.lastFetchStatus) &&
               Objects.equals(nextFetchTimestamp, that.nextFetchTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, title, description, iconUrl, sourceType, refreshIntervalMinutes, lastRefreshedAt, isContributedToGae, cronExpression, lastFetchStatus, nextFetchTimestamp);
    }
}

