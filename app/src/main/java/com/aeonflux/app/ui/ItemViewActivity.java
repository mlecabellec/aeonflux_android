/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-001 / TSK-20260809-001.3 - ItemViewActivity Implementation.
 */
package com.aeonflux.app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aeonflux.app.R;
import com.aeonflux.app.core.database.DatabaseService;
import com.aeonflux.app.core.database.entities.ArticleEntity;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * [TSK-20260809-001.3] Activity displaying detailed article metadata and action buttons
 * for embedded WebView, private WebView, external browser, read status toggling, and clipboard copy.
 */
@AndroidEntryPoint
public class ItemViewActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(ItemViewActivity.class.getName());

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SUMMARY = "extra_summary";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_PUBLISHED_AT = "extra_published_at";
    public static final String EXTRA_IS_READ = "extra_is_read";

    @Inject
    DatabaseService databaseService;

    private String articleId = "";
    private String articleTitle = "";
    private String articleUrl = "";
    private boolean isStarred = false;
    private boolean isRead = false;

    private Button btnToggleReadStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("AeonFlux_ItemView", "[DEBUG-LOG] ItemViewActivity onCreate called.");

        try {
            setContentView(R.layout.activity_item_view);

            Intent intent = getIntent();
            if (intent != null) {
                articleId = intent.getStringExtra(EXTRA_ARTICLE_ID) != null ? intent.getStringExtra(EXTRA_ARTICLE_ID) : "";
                articleTitle = intent.getStringExtra(EXTRA_TITLE) != null ? intent.getStringExtra(EXTRA_TITLE) : "Untitled";
                articleUrl = intent.getStringExtra(EXTRA_URL) != null ? intent.getStringExtra(EXTRA_URL) : "";
                isRead = intent.getBooleanExtra(EXTRA_IS_READ, false);

                android.util.Log.d("AeonFlux_ItemView", "[DEBUG-LOG] Extras received: id=" + articleId + ", title=" + articleTitle + ", url=" + articleUrl + ", isRead=" + isRead);

                TextView titleText = findViewById(R.id.text_item_title);
                TextView summaryText = findViewById(R.id.text_item_summary);
                TextView authorDateText = findViewById(R.id.text_item_author_date);

                if (titleText != null) titleText.setText(articleTitle);
                if (summaryText != null) summaryText.setText(intent.getStringExtra(EXTRA_SUMMARY) != null ? intent.getStringExtra(EXTRA_SUMMARY) : "No summary available.");
                if (authorDateText != null) authorDateText.setText("Author: " + (intent.getStringExtra(EXTRA_AUTHOR) != null ? intent.getStringExtra(EXTRA_AUTHOR) : "Unknown") + " • Published: " + intent.getLongExtra(EXTRA_PUBLISHED_AT, 0L));
            } else {
                android.util.Log.w("AeonFlux_ItemView", "[DEBUG-LOG] getIntent() is NULL!");
            }

            setupActionButtons();

        } catch (Exception e) {
            android.util.Log.e("AeonFlux_ItemView", "[DEBUG-LOG] FATAL EXCEPTION in ItemViewActivity onCreate!", e);
            Toast.makeText(this, "Error initializing article view", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupActionButtons() {
        try {
            Button btnEmbeddedWebView = findViewById(R.id.btn_open_embedded_webview);
            Button btnPrivateWebView = findViewById(R.id.btn_open_private_webview);
            Button btnExternalBrowser = findViewById(R.id.btn_open_external_browser);
            btnToggleReadStatus = findViewById(R.id.btn_toggle_read_status);
            Button btnCopyUrl = findViewById(R.id.btn_copy_url);

            updateReadButtonLabel();

            if (btnEmbeddedWebView != null) {
                btnEmbeddedWebView.setOnClickListener(v -> launchEmbeddedWebView());
            }

            if (btnPrivateWebView != null) {
                btnPrivateWebView.setOnClickListener(v -> launchPrivateWebView());
            }

            if (btnExternalBrowser != null) {
                btnExternalBrowser.setOnClickListener(v -> openExternalBrowser());
            }

            if (btnToggleReadStatus != null) {
                btnToggleReadStatus.setOnClickListener(v -> toggleReadStatus());
            }

            if (btnCopyUrl != null) {
                btnCopyUrl.setOnClickListener(v -> copyUrlToClipboard());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.3] Failed to setup action buttons.", e);
        }
    }

    private void updateReadButtonLabel() {
        if (btnToggleReadStatus != null) {
            btnToggleReadStatus.setText(isRead ? "✓ Mark as Unread" : "✓ Mark as Read");
        }
    }

    private String getValidSanitizedUrl() {
        if (articleUrl == null) return "";
        String trimmed = articleUrl.trim();
        if (trimmed.isEmpty()) return "";
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            if (trimmed.contains(".")) {
                return "https://" + trimmed;
            }
            return "";
        }
        return trimmed;
    }

    private void launchEmbeddedWebView() {
        try {
            String targetUrl = getValidSanitizedUrl();
            LOGGER.fine("[TSK-20260809-001.3] Launching Embedded WebView for targetUrl: " + targetUrl);
            if (!targetUrl.isEmpty()) {
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, targetUrl);
                intent.putExtra(WebViewActivity.EXTRA_TITLE, articleTitle);
                startActivity(intent);
            } else {
                android.util.Log.w("AeonFlux_ItemView", "[DEBUG-LOG] No valid web URL associated with article ID=" + articleId);
                Toast.makeText(this, "No valid web URL associated with this article", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.3] Error launching embedded webview.", e);
            Toast.makeText(this, "Unable to launch embedded webview", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchPrivateWebView() {
        try {
            String targetUrl = getValidSanitizedUrl();
            LOGGER.fine("[TSK-20260809-001.3] Launching Private WebView for targetUrl: " + targetUrl);
            if (!targetUrl.isEmpty()) {
                Intent intent = new Intent(this, PrivateWebViewActivity.class);
                intent.putExtra(PrivateWebViewActivity.EXTRA_URL, targetUrl);
                intent.putExtra(PrivateWebViewActivity.EXTRA_TITLE, articleTitle);
                startActivity(intent);
            } else {
                android.util.Log.w("AeonFlux_ItemView", "[DEBUG-LOG] No valid web URL associated with article ID=" + articleId);
                Toast.makeText(this, "No valid web URL associated with this article", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.3] Error launching private webview.", e);
            Toast.makeText(this, "Unable to launch private webview", Toast.LENGTH_SHORT).show();
        }
    }

    private void openExternalBrowser() {
        try {
            String targetUrl = getValidSanitizedUrl();
            LOGGER.fine("[TSK-20260809-001.3] Opening default web browser for targetUrl: " + targetUrl);
            if (!targetUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl));
                startActivity(intent);
            } else {
                android.util.Log.w("AeonFlux_ItemView", "[DEBUG-LOG] No valid web URL associated with article ID=" + articleId);
                Toast.makeText(this, "No valid web URL associated with this article", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.3] Error opening external browser.", e);
            Toast.makeText(this, "No browser application found", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleReadStatus() {
        try {
            isRead = !isRead;
            updateReadButtonLabel();

            if (articleId != null && !articleId.trim().isEmpty() && databaseService != null) {
                databaseService.updateReadStatusAsync(articleId, isRead);
            }

            Toast.makeText(this, isRead ? "Marked as Read" : "Marked as Unread", Toast.LENGTH_SHORT).show();
            LOGGER.fine("[TSK-20260809-001.3] Read status toggled to: " + isRead);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.3] Exception toggling read status.", e);
        }
    }

    private void copyUrlToClipboard() {
        try {
            String targetUrl = getValidSanitizedUrl();
            if (!targetUrl.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Article URL", targetUrl);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                    LOGGER.fine("[TSK-20260809-001.3] Copied URL to clipboard: " + targetUrl);
                }
            } else {
                Toast.makeText(this, "No URL available to copy", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.3] Error copying URL to clipboard.", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_download) {
            Toast.makeText(this, "Downloading article and attachments...", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_webview_normal) {
            launchEmbeddedWebView();
            return true;
        } else if (id == R.id.action_webview_private) {
            launchPrivateWebView();
            return true;
        } else if (id == R.id.action_associate_label) {
            Toast.makeText(this, "Associate Label dialog opened", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_toggle_starred) {
            isStarred = !isStarred;
            item.setTitle(isStarred ? "Unstar Article" : "Star Article");
            Toast.makeText(this, isStarred ? "Article Starred" : "Article Unstarred", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
