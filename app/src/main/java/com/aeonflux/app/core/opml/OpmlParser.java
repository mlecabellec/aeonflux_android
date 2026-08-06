/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-001 / TSK-20260806-001 - Multi-flavor OPML XML Parser implementation.
 */
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

/**
 * [TSK-20260806-001] Robust parser for OPML 1.0, 1.1, 2.0, Feeder, and PocketCasts files.
 */
public class OpmlParser {

    /**
     * [TSK-20260806-001] Parse input stream into a list of top-level OPML outline nodes.
     */
    @NonNull
    public List<OpmlItem> parse(@NonNull InputStream inputStream) throws XmlPullParserException, IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");

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

        return Collections.unmodifiableList(result);
    }

    private OpmlItem parseOutline(XmlPullParser parser, String parentCategory) throws XmlPullParserException, IOException {
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

        String currentCategory = parentCategory;
        if (xmlUrl.isEmpty()) {
            // Folder / Category outline node
            currentCategory = displayTitle;
        } else if (category.isEmpty() && !parentCategory.isEmpty()) {
            category = parentCategory;
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

        return new OpmlItem(displayTitle, text, xmlUrl, htmlUrl, type, category, children);
    }

    private String getAttributeValue(XmlPullParser parser, String attributeName) {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (attributeName.equalsIgnoreCase(parser.getAttributeName(i))) {
                String val = parser.getAttributeValue(i);
                return val != null ? val.trim() : "";
            }
        }
        return "";
    }
}
