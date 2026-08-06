/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-001 / TSK-20260806-001 - Recursive feed extraction helper.
 */
package com.aeonflux.app.core.opml;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * [TSK-20260806-001] Utility helper to recursively flatten nested OPML outline hierarchies into feed nodes with accurate label/category context.
 */
public class OpmlTreeFlattener {

    /**
     * [TSK-20260806-001] Recursively extracts all feed items from an OPML item tree, preserving parent group titles as categories/labels.
     */
    @NonNull
    public List<OpmlItem> extractAllFeeds(@NonNull List<OpmlItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        List<OpmlItem> flatFeeds = new ArrayList<>();
        for (OpmlItem item : items) {
            collectFeedsRecursive(item, "", flatFeeds);
        }
        return Collections.unmodifiableList(flatFeeds);
    }

    private void collectFeedsRecursive(OpmlItem item, String activeCategory, List<OpmlItem> accumulator) {
        if (item == null) return;

        String currentCategory = activeCategory;
        if (!item.getCategory().isEmpty()) {
            currentCategory = item.getCategory();
        } else if (!item.isFeed() && !item.getTitle().isEmpty()) {
            currentCategory = item.getTitle();
        }

        if (item.isFeed()) {
            OpmlItem resolvedItem = new OpmlItem(
                item.getTitle(),
                item.getText(),
                item.getXmlUrl(),
                item.getHtmlUrl(),
                item.getType(),
                currentCategory,
                Collections.emptyList()
            );
            accumulator.add(resolvedItem);
        }

        for (OpmlItem child : item.getChildren()) {
            collectFeedsRecursive(child, currentCategory, accumulator);
        }
    }
}
