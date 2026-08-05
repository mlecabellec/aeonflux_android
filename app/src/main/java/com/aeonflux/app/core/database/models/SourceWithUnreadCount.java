/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-00020 / TSK-20260805-001 - Source with unread item count and last article timestamp DTO.
 */
package com.aeonflux.app.core.database.models;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import com.aeonflux.app.core.database.entities.SourceEntity;
import java.util.Objects;

/**
 * [TSK-20260805-001] Model combining SourceEntity with unread count and latest article publication date.
 */
public class SourceWithUnreadCount {

    @Embedded
    @NonNull
    public SourceEntity source;

    public int unreadCount;

    public long lastArticleAt;

    public SourceWithUnreadCount() {
        this.source = new SourceEntity();
        this.unreadCount = 0;
        this.lastArticleAt = 0L;
    }

    @androidx.room.Ignore
    public SourceWithUnreadCount(@NonNull SourceEntity source, int unreadCount, long lastArticleAt) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.unreadCount = unreadCount;
        this.lastArticleAt = lastArticleAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceWithUnreadCount that = (SourceWithUnreadCount) o;
        return unreadCount == that.unreadCount &&
               lastArticleAt == that.lastArticleAt &&
               Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, unreadCount, lastArticleAt);
    }
}
