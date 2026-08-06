package com.aeonflux.app.core.opml;

import androidx.annotation.NonNull;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260806-001] Robust parser for OPML 1.0, 1.1, 2.0, Feeder, and PocketCasts files.
 */
public class OpmlParser {

    private static final Logger LOGGER = Logger.getLogger(OpmlParser.class.getName());

    /**
     * [TSK-20260806-001] Parse input stream into a list of top-level OPML outline nodes.
     */
    @NonNull
    public List<OpmlItem> parse(@NonNull InputStream inputStream) throws XmlPullParserException, IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        LOGGER.fine("Starting OPML parsing from input stream...");

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            List<OpmlItem> result = new ArrayList<>();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "outline".equalsIgnoreCase(parser.getName())) {
                    OpmlItem item = parseOutline(parser, "");
                    if (item != null) {
                        result.add(item);
                    }
                }
                eventType = parser.next();
            }

            LOGGER.info("OPML parsing completed successfully. Total root items parsed: " + result.size());
            return Collections.unmodifiableList(result);
        } catch (XmlPullParserException e) {
            LOGGER.log(Level.SEVERE, "XML syntax error occurred while parsing OPML file", e);
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error occurred while reading OPML stream", e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected runtime exception occurred while parsing OPML", e);
            throw new XmlPullParserException("Unexpected failure parsing OPML stream: " + e.getMessage());
        }
    }

    private OpmlItem parseOutline(XmlPullParser parser, String parentCategory) throws XmlPullParserException, IOException {
        if (parser == null) {
            LOGGER.warning("XmlPullParser parameter is null in parseOutline");
            return null;
        }
        String safeParentCategory = parentCategory != null ? parentCategory : "";

        try {
            String title = getAttributeValue(parser, "title");
            String text = getAttributeValue(parser, "text");
            String xmlUrl = getAttributeValue(parser, "xmlUrl");
            if (xmlUrl.isEmpty()) {
                xmlUrl = getAttributeValue(parser, "xmlurl");
            }
            String htmlUrl = getAttributeValue(parser, "htmlUrl");
            if (htmlUrl.isEmpty()) {
                htmlUrl = getAttributeValue(parser, "htmlurl");
            }
            String type = getAttributeValue(parser, "type");
            String category = getAttributeValue(parser, "category");

            String displayTitle = !title.isEmpty() ? title : (!text.isEmpty() ? text : "Untitled Feed");

            String currentCategory = safeParentCategory;
            if (xmlUrl.isEmpty()) {
                currentCategory = displayTitle;
            } else if (category.isEmpty() && !safeParentCategory.isEmpty()) {
                category = safeParentCategory;
            }

            List<OpmlItem> children = new ArrayList<>();
            int depth = parser.getDepth();

            while (true) {
                int token = parser.next();
                if (token == XmlPullParser.END_TAG && parser.getDepth() == depth) {
                    break;
                }
                if (token == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                if (token == XmlPullParser.START_TAG && "outline".equalsIgnoreCase(parser.getName())) {
                    OpmlItem child = parseOutline(parser, currentCategory);
                    if (child != null) {
                        children.add(child);
                    }
                }
            }

            LOGGER.fine("Parsed outline node: title='" + displayTitle + "', xmlUrl='" + xmlUrl + "', category='" + category + "'");
            return new OpmlItem(displayTitle, text, xmlUrl, htmlUrl, type, category, children);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing outline node at depth " + parser.getDepth(), e);
            return null;
        }
    }

    private String getAttributeValue(XmlPullParser parser, String attributeName) {
        if (parser == null || attributeName == null) {
            return "";
        }
        try {
            int count = parser.getAttributeCount();
            for (int i = 0; i < count; i++) {
                if (attributeName.equalsIgnoreCase(parser.getAttributeName(i))) {
                    String val = parser.getAttributeValue(i);
                    return val != null ? val.trim() : "";
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to read attribute '" + attributeName + "'", e);
        }
        return "";
    }
}

