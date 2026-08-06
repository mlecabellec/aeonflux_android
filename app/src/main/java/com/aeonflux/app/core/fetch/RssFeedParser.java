/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-FETCH-001 / TSK-20260806-001 - Lightweight RSS/Atom Feed XML Item Parser.
 */
package com.aeonflux.app.core.fetch;

import androidx.annotation.NonNull;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;

import com.aeonflux.app.core.database.entities.ArticleEntity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * [TSK-20260806-001] Stream parser for RSS channel items and Atom entries.
 */
public class RssFeedParser {

    /**
     * [TSK-20260806-001] Parse RSS XML stream into ArticleEntity domain objects.
     */
    @NonNull
    public List<ArticleEntity> parseFeedItems(@NonNull InputStream inputStream, @NonNull String sourceId) throws Exception {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);

        List<ArticleEntity> articles = new ArrayList<>();
        int eventType = parser.getEventType();

        String currentTitle = "";
        String currentLink = "";
        String currentGuid = "";
        String currentDescription = "";
        String currentAuthor = "";
        long publishedAt = System.currentTimeMillis();

        boolean inItem = false;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if ("item".equalsIgnoreCase(tagName) || "entry".equalsIgnoreCase(tagName)) {
                    inItem = true;
                    currentTitle = "";
                    currentLink = "";
                    currentGuid = "";
                    currentDescription = "";
                    currentAuthor = "";
                    publishedAt = System.currentTimeMillis();
                } else if (inItem) {
                    if ("title".equalsIgnoreCase(tagName)) {
                        currentTitle = parser.nextText();
                    } else if ("link".equalsIgnoreCase(tagName)) {
                        currentLink = parser.nextText();
                    } else if ("guid".equalsIgnoreCase(tagName) || "id".equalsIgnoreCase(tagName)) {
                        currentGuid = parser.nextText();
                    } else if ("description".equalsIgnoreCase(tagName) || "summary".equalsIgnoreCase(tagName) || "content".equalsIgnoreCase(tagName)) {
                        currentDescription = parser.nextText();
                    } else if ("author".equalsIgnoreCase(tagName) || "dc:creator".equalsIgnoreCase(tagName)) {
                        currentAuthor = parser.nextText();
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if ("item".equalsIgnoreCase(tagName) || "entry".equalsIgnoreCase(tagName)) {
                    inItem = false;
                    if (!currentTitle.isEmpty() || !currentLink.isEmpty()) {
                        String articleId = "art_" + UUID.randomUUID().toString().substring(0, 8);
                        String guid = !currentGuid.isEmpty() ? currentGuid : (!currentLink.isEmpty() ? currentLink : articleId);
                        ArticleEntity article = new ArticleEntity(
                            articleId,
                            sourceId,
                            guid,
                            currentTitle.isEmpty() ? currentLink : currentTitle,
                            currentDescription,
                            currentDescription.replaceAll("<[^>]*>", "").trim(),
                            currentAuthor.isEmpty() ? "RSS Feed" : currentAuthor,
                            publishedAt,
                            currentLink
                        );
                        articles.add(article);
                    }
                }
            }
            eventType = parser.next();
        }

        return Collections.unmodifiableList(articles);
    }
}
