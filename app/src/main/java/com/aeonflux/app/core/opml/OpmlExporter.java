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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260806-001] Serializer to export database feeds and categories to OPML XML format.
 */
public class OpmlExporter {

    private static final Logger LOGGER = Logger.getLogger(OpmlExporter.class.getName());

    /**
     * [TSK-20260806-001] Exports sources grouped by category label to output stream.
     */
    public void export(@NonNull Map<String, List<SourceEntity>> groupedSources, @NonNull OutputStream outputStream) throws IOException {
        Objects.requireNonNull(groupedSources, "groupedSources must not be null");
        Objects.requireNonNull(outputStream, "outputStream must not be null");

        LOGGER.info("Starting OPML export for " + groupedSources.size() + " category groups...");

        try {
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

            int totalExportedFeeds = 0;
            for (Map.Entry<String, List<SourceEntity>> entry : groupedSources.entrySet()) {
                if (entry == null) {
                    continue;
                }
                String category = entry.getKey();
                List<SourceEntity> sources = entry.getValue();

                if (sources == null || sources.isEmpty()) {
                    LOGGER.fine("Skipping empty category group: " + category);
                    continue;
                }

                boolean hasCategory = category != null && !category.trim().isEmpty() && !"Uncategorized".equalsIgnoreCase(category);

                if (hasCategory) {
                    serializer.startTag("", "outline");
                    serializer.attribute("", "text", category);
                    serializer.attribute("", "title", category);
                }

                for (SourceEntity source : sources) {
                    if (source == null || source.title == null || source.url == null) {
                        LOGGER.warning("Skipping invalid or incomplete SourceEntity in export");
                        continue;
                    }
                    serializer.startTag("", "outline");
                    serializer.attribute("", "type", "rss");
                    serializer.attribute("", "text", source.title);
                    serializer.attribute("", "title", source.title);
                    serializer.attribute("", "xmlUrl", source.url);
                    if (source.iconUrl != null && !source.iconUrl.isEmpty()) {
                        serializer.attribute("", "imageUrl", source.iconUrl);
                    }
                    serializer.endTag("", "outline");
                    totalExportedFeeds++;
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

            LOGGER.info("OPML export completed successfully. Total feeds written: " + totalExportedFeeds);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error during OPML export writing", e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected runtime error during OPML export serialization", e);
            throw new IOException("Failed to serialize OPML document: " + e.getMessage(), e);
        }
    }
}

