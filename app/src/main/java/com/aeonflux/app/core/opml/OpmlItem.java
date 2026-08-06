/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-001 / TSK-20260806-001 - OPML Outline Item representation.
 */
package com.aeonflux.app.core.opml;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * [TSK-20260806-001] Model representing an OPML outline node (feed or category folder).
 */
public class OpmlItem {

    private final String title;
    private final String text;
    private final String xmlUrl;
    private final String htmlUrl;
    private final String type;
    private final String category;
    private final List<OpmlItem> children;

    public OpmlItem(String title, String text, String xmlUrl, String htmlUrl, String type, String category, List<OpmlItem> children) {
        this.title = title != null ? title : (text != null ? text : "");
        this.text = text != null ? text : this.title;
        this.xmlUrl = xmlUrl != null ? xmlUrl : "";
        this.htmlUrl = htmlUrl != null ? htmlUrl : "";
        this.type = type != null ? type : "rss";
        this.category = category != null ? category : "";
        this.children = children != null ? new ArrayList<>(children) : Collections.emptyList();
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @NonNull
    public String getXmlUrl() {
        return xmlUrl;
    }

    @NonNull
    public String getHtmlUrl() {
        return htmlUrl;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public List<OpmlItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean isFeed() {
        return !xmlUrl.isEmpty();
    }

    public boolean isCategory() {
        return xmlUrl.isEmpty() && !children.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpmlItem opmlItem = (OpmlItem) o;
        return Objects.equals(title, opmlItem.title) &&
               Objects.equals(xmlUrl, opmlItem.xmlUrl) &&
               Objects.equals(category, opmlItem.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, xmlUrl, category);
    }
}
