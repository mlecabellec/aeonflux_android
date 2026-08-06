/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-002 / TSK-20260806-001 - OPML Exporter implementation.
 */
package com.aeonflux.app.core.opml;

import androidx.annotation.NonNull;
import android.util.Xml;
import org.xmlpull.v1.XmlSerializer;

import com.aeonflux.app.core.database.entities.SourceEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * [TSK-20260806-001] Serializer to export database feeds and categories to OPML XML format.
 */
public class OpmlExporter {

    /**
     * [TSK-20260806-001] Exports sources grouped by category label to output stream.
     */
    public void export(@NonNull Map<String, List<SourceEntity>> groupedSources, @NonNull OutputStream outputStream) throws IOException {
        Objects.requireNonNull(groupedSources, "groupedSources must not be null");
        Objects.requireNonNull(outputStream, "outputStream must not be null");

        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);

        serializer.startDocument("UTF-8", true);
        serializer.startTag("", "opml");
        serializer.attribute("", "version", "2.0");

        // Head section
        serializer.startTag("", "head");
        serializer.startTag("", "title");
        serializer.text("AeonFlux Subscriptions Export");
        serializer.endTag("", "title");
        serializer.endTag("", "head");

        // Body section
        serializer.startTag("", "body");

        for (Map.Entry<String, List<SourceEntity>> entry : groupedSources.entrySet()) {
            String category = entry.getKey();
            List<SourceEntity> sources = entry.getValue();

            boolean hasCategory = category != null && !category.trim().isEmpty() && !"Uncategorized".equalsIgnoreCase(category);

            if (hasCategory) {
                serializer.startTag("", "outline");
                serializer.attribute("", "text", category);
                serializer.attribute("", "title", category);
            }

            for (SourceEntity source : sources) {
                serializer.startTag("", "outline");
                serializer.attribute("", "type", "rss");
                serializer.attribute("", "text", source.title);
                serializer.attribute("", "title", source.title);
                serializer.attribute("", "xmlUrl", source.url);
                if (source.iconUrl != null && !source.iconUrl.isEmpty()) {
                    serializer.attribute("", "imageUrl", source.iconUrl);
                }
                serializer.endTag("", "outline");
            }

            if (hasCategory) {
                serializer.endTag("", "outline");
            }
        }

        serializer.endTag("", "body");
        serializer.endTag("", "opml");
        serializer.endDocument();

        byte[] bytes = writer.toString().getBytes("UTF-8");
        outputStream.write(bytes);
        outputStream.flush();
    }
}
