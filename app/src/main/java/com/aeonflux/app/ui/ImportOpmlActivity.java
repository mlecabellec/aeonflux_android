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

    private void onFileSelected(Uri uri) {
        if (uri == null) return;
        appendLog("File selected: " + uri.getPath());
        btnSelectFile.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream == null) {
                    runOnUiThread(() -> appendLog("Error: Unable to open selected file."));
                    return;
                }

                OpmlParser parser = new OpmlParser();
                List<OpmlItem> items = parser.parse(inputStream);
                OpmlTreeFlattener flattener = new OpmlTreeFlattener();
                List<OpmlItem> allFeeds = flattener.extractAllFeeds(items);

                runOnUiThread(() -> appendLog("Parsed " + allFeeds.size() + " feeds from OPML hierarchy. Processing database insertion & labeling..."));

                int imported = 0;
                int skipped = 0;

                for (OpmlItem feed : allFeeds) {
                    if (processFeedItem(feed)) {
                        imported++;
                    } else {
                        skipped++;
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
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnSelectFile.setEnabled(true);
                    appendLog("Fatal error during import: " + e.getMessage());
                });
            }
        });
    }

    private boolean processFeedItem(OpmlItem item) {
        String url = item.getXmlUrl();
        if (url == null || url.isEmpty()) return false;

        SourceEntity existing = databaseService.getSourceByUrl(url);
        if (existing != null) {
            runOnUiThread(() -> appendLog("Skipping duplicate: " + item.getTitle() + " (" + url + ")"));
            return false;
        }

        String id = "src_" + UUID.randomUUID().toString().substring(0, 8);
        SourceEntity source = new SourceEntity(id, url, item.getTitle(), item.getText(), null, "RSS", 60, System.currentTimeMillis(), 0);
        databaseService.insertSource(source);

        if (!item.getCategory().isEmpty()) {
            String labelId = "lbl_" + item.getCategory().toLowerCase().replaceAll("[^a-z0-9]", "_");
            LabelEntity label = new LabelEntity(labelId, item.getCategory(), "#3B82F6");
            databaseService.insertLabel(label);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                    try (InputStream is = conn.getInputStream()) {
                        com.aeonflux.app.core.fetch.RssFeedParser parser = new com.aeonflux.app.core.fetch.RssFeedParser();
                        List<SourceEntity> sources = databaseService.getAllSources();
                        List<ArticleEntity> articles = parser.parseFeedItems(is, id);
                        for (ArticleEntity article : articles) {
                            databaseService.insertArticle(article);
                        }
                    }
                }
                conn.disconnect();
            } catch (Exception ignored) {
            }
        });

        runOnUiThread(() -> appendLog("Imported: " + item.getTitle()));
        return true;
    }



    private void appendLog(String message) {
        logTextView.append(message + "\n");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
