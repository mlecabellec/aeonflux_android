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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260806-001] Stream parser for RSS channel items and Atom entries.
 */
public class RssFeedParser {

    private static final Logger LOGGER = Logger.getLogger(RssFeedParser.class.getName());

    /**
     * [TSK-20260806-001] Parse RSS XML stream into ArticleEntity domain objects.
     */
    @NonNull
    public List<ArticleEntity> parseFeedItems(@NonNull InputStream inputStream, @NonNull String sourceId) throws Exception {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        LOGGER.fine("Starting RSS stream parsing for sourceId: " + sourceId);

        List<ArticleEntity> articles = new ArrayList<>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

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
                        try {
                            if ("title".equalsIgnoreCase(tagName)) {
                                currentTitle = safeNextText(parser);
                            } else if ("link".equalsIgnoreCase(tagName)) {
                                currentLink = safeNextText(parser);
                            } else if ("guid".equalsIgnoreCase(tagName) || "id".equalsIgnoreCase(tagName)) {
                                currentGuid = safeNextText(parser);
                            } else if ("description".equalsIgnoreCase(tagName) || "summary".equalsIgnoreCase(tagName) || "content".equalsIgnoreCase(tagName)) {
                                currentDescription = safeNextText(parser);
                            } else if ("author".equalsIgnoreCase(tagName) || "dc:creator".equalsIgnoreCase(tagName)) {
                                currentAuthor = safeNextText(parser);
                            }
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, "Error reading RSS tag content for tag: " + tagName, ex);
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

            LOGGER.info("RSS feed stream parsing completed for source " + sourceId + ". Extracted " + articles.size() + " articles.");
            return Collections.unmodifiableList(articles);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse RSS XML stream for source: " + sourceId, e);
            return Collections.unmodifiableList(articles);
        }
    }

    private String safeNextText(XmlPullParser parser) {
        if (parser == null) {
            return "";
        }
        try {
            String text = parser.nextText();
            return text != null ? text.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }
}

