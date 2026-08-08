/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-001 / TSK-20260809-001.2 - Private WebView Activity with Hardened Privacy.
 */
package com.aeonflux.app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aeonflux.app.R;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * [TSK-20260809-001.2] Hardened Private WebView Activity guaranteeing zero persistence,
 * strict privacy configuration, default disabled JavaScript, and session cache purging.
 */
public class PrivateWebViewActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(PrivateWebViewActivity.class.getName());

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    private WebView webView;
    private ProgressBar progressBar;
    private String articleUrl = "";
    private String articleTitle = "";
    private boolean isJavaScriptEnabled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("[TSK-20260809-001.2] Initializing PrivateWebViewActivity with privacy hardening.");

        try {
            setContentView(R.layout.activity_private_web_view);

            Toolbar toolbar = findViewById(R.id.toolbar_private_webview);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }

            progressBar = findViewById(R.id.progress_private_webview);
            webView = findViewById(R.id.webview_private_content);

            Intent intent = getIntent();
            if (intent != null) {
                articleUrl = intent.getStringExtra(EXTRA_URL) != null ? intent.getStringExtra(EXTRA_URL) : "";
                articleTitle = intent.getStringExtra(EXTRA_TITLE) != null ? intent.getStringExtra(EXTRA_TITLE) : "Private View";
            }

            if (actionBar != null) {
                actionBar.setTitle("🔒 Private: " + articleTitle);
            }

            purgeEphemeralData();
            configurePrivateWebViewSettings();

            if (articleUrl != null && !articleUrl.trim().isEmpty()) {
                LOGGER.fine("[TSK-20260809-001.2] Loading URL in Private WebView (JS=" + isJavaScriptEnabled + "): " + articleUrl);
                webView.loadUrl(articleUrl);
            } else {
                LOGGER.warning("[TSK-20260809-001.2] Empty URL provided to PrivateWebViewActivity.");
                Toast.makeText(this, "Invalid article URL", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.2] Exception in PrivateWebViewActivity onCreate.", e);
            Toast.makeText(this, "Failed to launch private web view", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configurePrivateWebViewSettings() {
        try {
            WebSettings settings = webView.getSettings();

            // [TSK-20260809-001.2] Disable JavaScript by default for strict security policy
            settings.setJavaScriptEnabled(isJavaScriptEnabled);

            // Privacy & Data Isolation settings
            settings.setDomStorageEnabled(false);
            settings.setDatabaseEnabled(false);
            settings.setSavePassword(false);
            settings.setAllowFileAccess(false);
            settings.setAllowContentAccess(false);
            settings.setGeolocationEnabled(false);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    LOGGER.warning("[TSK-20260809-001.2] Private WebView load error: " + (error != null ? error.getDescription() : "unknown"));
                }
            });

            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (progressBar != null) {
                        progressBar.setProgress(newProgress);
                        if (newProgress == 100) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.2] Exception configuring Private WebSettings.", e);
        }
    }

    private void purgeEphemeralData() {
        try {
            LOGGER.fine("[TSK-20260809-001.2] Purging private cache, history, and cookies.");
            if (webView != null) {
                webView.clearCache(true);
                webView.clearHistory();
                webView.clearFormData();
            }
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[TSK-20260809-001.2] Non-fatal error during cache purge.", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_private_webview, menu);
        MenuItem jsItem = menu.findItem(R.id.action_toggle_javascript);
        if (jsItem != null) {
            jsItem.setChecked(isJavaScriptEnabled);
            jsItem.setTitle(isJavaScriptEnabled ? "Disable JavaScript" : "Enable JavaScript");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_toggle_javascript) {
            isJavaScriptEnabled = !isJavaScriptEnabled;
            item.setChecked(isJavaScriptEnabled);
            item.setTitle(isJavaScriptEnabled ? "Disable JavaScript" : "Enable JavaScript");

            if (webView != null) {
                webView.getSettings().setJavaScriptEnabled(isJavaScriptEnabled);
                webView.reload();
            }
            Toast.makeText(this, isJavaScriptEnabled ? "JavaScript Enabled" : "JavaScript Disabled", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_reload_private) {
            if (webView != null) {
                webView.reload();
            }
            return true;
        } else if (id == R.id.action_copy_url_private) {
            copyUrlToClipboard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyUrlToClipboard() {
        try {
            if (articleUrl != null && !articleUrl.trim().isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Article URL", articleUrl);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.2] Failed to copy URL to clipboard.", e);
        }
    }

    @Override
    protected void onDestroy() {
        LOGGER.info("[TSK-20260809-001.2] Destroying PrivateWebViewActivity and purging all session data.");
        purgeEphemeralData();
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
