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
        LOGGER.info("Starting scheduled RSS fetch background worker...");
        long now = System.currentTimeMillis();

        List<SourceEntity> sources = databaseService.getAllSources();
        int updatedCount = 0;

        for (SourceEntity source : sources) {
            try {
                if (fetchAndSeedSource(source)) {
                    source.lastRefreshedAt = now;
                    source.lastFetchStatus = "SUCCESS";
                    databaseService.updateSource(source);
                    updatedCount++;
                }
            } catch (Exception e) {
                source.lastFetchStatus = "ERROR: " + e.getMessage();
                databaseService.updateSource(source);
                LOGGER.warning("Error fetching source " + source.url + ": " + e.getMessage());
            }
        }

        LOGGER.info("Background RSS fetch completed. Updated " + updatedCount + " sources.");
        return Result.success();
    }

    public boolean fetchAndSeedSource(@NonNull SourceEntity source) throws Exception {
        Objects.requireNonNull(source, "source must not be null");
        URL url = new URL(source.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "AeonFlux/1.0 RSS Reader");

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (InputStream is = connection.getInputStream()) {
                RssFeedParser parser = new RssFeedParser();
                List<com.aeonflux.app.core.database.entities.ArticleEntity> articles = parser.parseFeedItems(is, source.id);
                for (com.aeonflux.app.core.database.entities.ArticleEntity article : articles) {
                    databaseService.insertArticle(article);
                }
                LOGGER.info("Successfully fetched " + articles.size() + " articles for " + source.title);
            } finally {
                connection.disconnect();
            }
            return true;
        } else {
            connection.disconnect();
            return false;
        }
    }
}

