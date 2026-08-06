package com.aeonflux.app.core.opml;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260806-001] Utility helper to recursively flatten nested OPML outline hierarchies into feed nodes with accurate label/category context.
 */
public class OpmlTreeFlattener {

    private static final Logger LOGGER = Logger.getLogger(OpmlTreeFlattener.class.getName());

    /**
     * [TSK-20260806-001] Recursively extracts all feed items from an OPML item tree, preserving parent group titles as categories/labels.
     */
    @NonNull
    public List<OpmlItem> extractAllFeeds(@NonNull List<OpmlItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        LOGGER.fine("Starting recursive extraction of feeds from OPML tree (" + items.size() + " root nodes)...");

        List<OpmlItem> flatFeeds = new ArrayList<>();
        try {
            for (OpmlItem item : items) {
                if (item != null) {
                    collectFeedsRecursive(item, "", flatFeeds);
                }
            }
            LOGGER.info("Successfully flattened OPML tree into " + flatFeeds.size() + " feed items.");
            return Collections.unmodifiableList(flatFeeds);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected exception flattening OPML tree", e);
            return Collections.unmodifiableList(flatFeeds);
        }
    }

    private void collectFeedsRecursive(OpmlItem item, String activeCategory, List<OpmlItem> accumulator) {
        if (item == null || accumulator == null) {
            return;
        }

        try {
            String safeCategory = activeCategory != null ? activeCategory : "";
            String currentCategory = safeCategory;
            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                currentCategory = item.getCategory();
            } else if (!item.isFeed() && item.getTitle() != null && !item.getTitle().isEmpty()) {
                currentCategory = item.getTitle();
            }

            if (item.isFeed()) {
                OpmlItem resolvedItem = new OpmlItem(
                    item.getTitle() != null ? item.getTitle() : "Untitled Feed",
                    item.getText() != null ? item.getText() : "",
                    item.getXmlUrl() != null ? item.getXmlUrl() : "",
                    item.getHtmlUrl() != null ? item.getHtmlUrl() : "",
                    item.getType() != null ? item.getType() : "rss",
                    currentCategory,
                    Collections.emptyList()
                );
                accumulator.add(resolvedItem);
            }

            List<OpmlItem> children = item.getChildren();
            if (children != null) {
                for (OpmlItem child : children) {
                    if (child != null) {
                        collectFeedsRecursive(child, currentCategory, accumulator);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing OPML node during recursive collection", e);
        }
    }
}

