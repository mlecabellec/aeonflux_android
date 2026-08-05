/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-00020 / TSK-20260805-001 - Source Group DTO for label grouped tree display.
 */
package com.aeonflux.app.core.database.models;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * [TSK-20260805-001] Model representing a Label Group node holding child SourceWithUnreadCount items.
 */
public class SourceGroupDTO {

    @NonNull
    public String labelId;

    @NonNull
    public String labelName;

    @NonNull
    public String labelColor;

    @NonNull
    public List<SourceWithUnreadCount> sources;

    public SourceGroupDTO(@NonNull String labelId, @NonNull String labelName, @NonNull String labelColor) {
        this.labelId = Objects.requireNonNull(labelId, "labelId must not be null");
        this.labelName = Objects.requireNonNull(labelName, "labelName must not be null");
        this.labelColor = Objects.requireNonNull(labelColor, "labelColor must not be null");
        this.sources = new ArrayList<>();
    }

    public void addSource(@NonNull SourceWithUnreadCount source) {
        this.sources.add(Objects.requireNonNull(source, "source must not be null"));
    }

    public int getTotalUnreadCount() {
        int count = 0;
        for (SourceWithUnreadCount s : sources) {
            count += s.unreadCount;
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceGroupDTO that = (SourceGroupDTO) o;
        return Objects.equals(labelId, that.labelId) &&
               Objects.equals(labelName, that.labelName) &&
               Objects.equals(labelColor, that.labelColor) &&
               Objects.equals(sources, that.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labelId, labelName, labelColor, sources);
    }
}
