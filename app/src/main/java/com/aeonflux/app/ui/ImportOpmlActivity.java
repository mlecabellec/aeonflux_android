/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-001 / TSK-20260806-001 - Activity to import OPML files with real-time logging.
 */
package com.aeonflux.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.aeonflux.app.R;
import com.aeonflux.app.core.database.DatabaseService;
import com.aeonflux.app.core.database.entities.ArticleEntity;
import com.aeonflux.app.core.database.entities.LabelEntity;

import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.opml.OpmlItem;
import com.aeonflux.app.core.opml.OpmlParser;
import com.aeonflux.app.core.opml.OpmlTreeFlattener;


import dagger.hilt.android.AndroidEntryPoint;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import javax.inject.Inject;

@AndroidEntryPoint
public class ImportOpmlActivity extends AppCompatActivity {

    private Button btnSelectFile;
    private ProgressBar progressBar;
    private TextView logTextView;

    @Inject
    DatabaseService databaseService;

    private final ActivityResultLauncher<String> filePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), this::onFileSelected);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_opml);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Import OPML Feeds");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSelectFile = findViewById(R.id.btn_select_opml_file);
        progressBar = findViewById(R.id.import_progress_bar);
        logTextView = findViewById(R.id.text_import_logs);

        btnSelectFile.setOnClickListener(v -> filePickerLauncher.launch("*/*"));
    }

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ImportOpmlActivity.class.getName());

    private void onFileSelected(Uri uri) {
        if (uri == null) {
            LOGGER.warning("onFileSelected invoked with null URI");
            return;
        }
        LOGGER.info("User selected OPML import file URI: " + uri);
        appendLog("File selected: " + uri.getPath());
        btnSelectFile.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream == null) {
                    LOGGER.severe("Unable to open InputStream for URI: " + uri);
                    runOnUiThread(() -> {
                        appendLog("Error: Unable to open selected file.");
                        btnSelectFile.setEnabled(true);
                        progressBar.setVisibility(ProgressBar.GONE);
                    });
                    return;
                }

                OpmlParser parser = new OpmlParser();
                List<OpmlItem> items = parser.parse(inputStream);
                OpmlTreeFlattener flattener = new OpmlTreeFlattener();
                List<OpmlItem> allFeeds = flattener.extractAllFeeds(items);

                LOGGER.info("Parsed " + allFeeds.size() + " feed nodes from selected OPML file.");
                runOnUiThread(() -> appendLog("Parsed " + allFeeds.size() + " feeds from OPML hierarchy. Processing database insertion & labeling..."));

                int imported = 0;
                int skipped = 0;

                for (OpmlItem feed : allFeeds) {
                    if (feed != null) {
                        try {
                            if (processFeedItem(feed)) {
                                imported++;
                            } else {
                                skipped++;
                            }
                        } catch (Exception ex) {
                            LOGGER.log(java.util.logging.Level.WARNING, "Error processing OPML feed item: " + feed.getTitle(), ex);
                            skipped++;
                        }
                    }
                }

                int finalImported = imported;
                int finalSkipped = skipped;
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnSelectFile.setEnabled(true);
                    appendLog("\n=== Import Completed ===");
                    appendLog("Successfully imported: " + finalImported + " feeds.");
                    appendLog("Skipped (duplicates): " + finalSkipped + " feeds.");
                    Toast.makeText(ImportOpmlActivity.this, "Import finished!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Fatal error executing OPML import task", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnSelectFile.setEnabled(true);
                    appendLog("Fatal error during import: " + e.getMessage());
                });
            }
        });
    }

    private boolean processFeedItem(OpmlItem item) {
        if (item == null) {
            return false;
        }
        String url = item.getXmlUrl();
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            SourceEntity existing = databaseService.getSourceByUrl(url);
            if (existing != null) {
                runOnUiThread(() -> appendLog("Skipping duplicate: " + item.getTitle() + " (" + url + ")"));
                return false;
            }

            String id = "src_" + UUID.randomUUID().toString().substring(0, 8);
            SourceEntity source = new SourceEntity(id, url, item.getTitle(), item.getText(), null, "RSS", 60, System.currentTimeMillis(), 0);
            databaseService.insertSource(source);

            if (item.getCategory() != null && !item.getCategory().trim().isEmpty()) {
                String labelId = "lbl_" + item.getCategory().toLowerCase().replaceAll("[^a-z0-9]", "_");
                LabelEntity label = new LabelEntity(labelId, item.getCategory(), "#3B82F6");
                databaseService.insertLabel(label);
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                HttpURLConnection conn = null;
                try {
                    URL urlObj = new URL(url);
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                        try (InputStream is = conn.getInputStream()) {
                            com.aeonflux.app.core.fetch.RssFeedParser parser = new com.aeonflux.app.core.fetch.RssFeedParser();
                            List<ArticleEntity> articles = parser.parseFeedItems(is, id);
                            if (articles != null) {
                                for (ArticleEntity article : articles) {
                                    if (article != null) {
                                        databaseService.insertArticle(article);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.log(java.util.logging.Level.FINE, "Initial background fetch failed during import for feed: " + url, ex);
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                        } catch (Exception ignored) {
                        }
                    }
                }
            });

            runOnUiThread(() -> appendLog("Imported: " + item.getTitle()));
            return true;
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Exception processing feed item: " + url, e);
            return false;
        }
    }

    private void appendLog(String message) {
        if (logTextView != null && message != null) {
            logTextView.append(message + "\n");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

