/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-FETCH-001 / TSK-20260806-001 - Background RSS Fetching WorkManager Worker.
 */
package com.aeonflux.app.core.fetch;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aeonflux.app.core.database.DatabaseService;
import com.aeonflux.app.core.database.entities.SourceEntity;

import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RssFetchWorker extends Worker {

    private static final Logger LOGGER = Logger.getLogger(RssFetchWorker.class.getName());
    private final DatabaseService databaseService;

    public RssFetchWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.databaseService = com.aeonflux.app.AeonFluxApplication.getDatabaseService();
    }



    @NonNull
    @Override
    public Result doWork() {
        LOGGER.info("Starting scheduled RSS fetch background worker execution...");
        long now = System.currentTimeMillis();

        try {
            if (databaseService == null) {
                LOGGER.severe("DatabaseService instance is null in RssFetchWorker! Cannot perform fetch.");
                return Result.failure();
            }

            List<SourceEntity> sources = databaseService.getAllSources();
            if (sources == null || sources.isEmpty()) {
                LOGGER.info("No sources registered in DatabaseService. Background worker finished with 0 updates.");
                return Result.success();
            }

            int updatedCount = 0;
            for (SourceEntity source : sources) {
                if (source == null || source.url == null || source.url.trim().isEmpty()) {
                    LOGGER.warning("Skipping invalid or null SourceEntity in RssFetchWorker");
                    continue;
                }
                try {
                    if (fetchAndSeedSource(source)) {
                        source.lastRefreshedAt = now;
                        source.lastFetchStatus = "SUCCESS";
                        databaseService.updateSource(source);
                        updatedCount++;
                    }
                } catch (Exception e) {
                    source.lastFetchStatus = "ERROR: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    try {
                        databaseService.updateSource(source);
                    } catch (Exception dbEx) {
                        LOGGER.log(Level.WARNING, "Failed to update source status in database for: " + source.url, dbEx);
                    }
                    LOGGER.log(Level.WARNING, "Error fetching source " + source.url + ": " + e.getMessage(), e);
                }
            }

            LOGGER.info("Background RSS fetch worker completed successfully. Updated " + updatedCount + " sources.");
            return Result.success();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal exception encountered during RssFetchWorker execution", e);
            return Result.retry();
        }
    }

    public boolean fetchAndSeedSource(@NonNull SourceEntity source) throws Exception {
        Objects.requireNonNull(source, "source must not be null");
        if (source.url == null || source.url.trim().isEmpty()) {
            LOGGER.warning("Source URL is null or empty in fetchAndSeedSource");
            return false;
        }

        LOGGER.fine("Opening HTTP connection to feed URL: " + source.url);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source.url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "AeonFlux/1.0 RSS Reader");

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (InputStream is = connection.getInputStream()) {
                    RssFeedParser parser = new RssFeedParser();
                    List<com.aeonflux.app.core.database.entities.ArticleEntity> articles = parser.parseFeedItems(is, source.id);
                    if (articles != null) {
                        for (com.aeonflux.app.core.database.entities.ArticleEntity article : articles) {
                            if (article != null && databaseService != null) {
                                databaseService.insertArticle(article);
                            }
                        }
                        LOGGER.info("Successfully fetched and persisted " + articles.size() + " articles for feed: " + source.title);
                    }
                }
                return true;
            } else {
                LOGGER.warning("HTTP server returned failure response code (" + responseCode + ") for URL: " + source.url);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Network or parsing exception for URL: " + source.url, e);
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Failed to disconnect HttpURLConnection cleanly", ex);
                }
            }
        }
    }

}

