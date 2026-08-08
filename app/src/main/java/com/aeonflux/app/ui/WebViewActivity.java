/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-001 / TSK-20260809-001.1 - Embedded WebView Activity.
 */
package com.aeonflux.app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
 * [TSK-20260809-001.1] Standard embedded WebView activity to display article web pages safely.
 */
public class WebViewActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(WebViewActivity.class.getName());

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    private WebView webView;
    private ProgressBar progressBar;
    private String articleUrl = "";
    private String articleTitle = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("[TSK-20260809-001.1] Initializing WebViewActivity.");

        try {
            setContentView(R.layout.activity_web_view);

            Toolbar toolbar = findViewById(R.id.toolbar_webview);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }

            progressBar = findViewById(R.id.progress_webview);
            webView = findViewById(R.id.webview_content);

            Intent intent = getIntent();
            if (intent != null) {
                articleUrl = intent.getStringExtra(EXTRA_URL) != null ? intent.getStringExtra(EXTRA_URL) : "";
                articleTitle = intent.getStringExtra(EXTRA_TITLE) != null ? intent.getStringExtra(EXTRA_TITLE) : "Web View";
            }

            if (actionBar != null) {
                actionBar.setTitle(articleTitle);
            }

            setupWebView();

            if (articleUrl != null && !articleUrl.trim().isEmpty()) {
                LOGGER.fine("[TSK-20260809-001.1] Loading URL in WebView: " + articleUrl);
                webView.loadUrl(articleUrl);
            } else {
                LOGGER.warning("[TSK-20260809-001.1] Invalid or empty URL provided to WebViewActivity.");
                Toast.makeText(this, "Invalid article URL", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.1] Error during WebViewActivity initialization.", e);
            Toast.makeText(this, "Failed to load web view", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupWebView() {
        try {
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
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
                    LOGGER.warning("[TSK-20260809-001.1] WebView loading error: " + (error != null ? error.getDescription() : "unknown"));
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
            LOGGER.log(Level.SEVERE, "[TSK-20260809-001.1] Exception configuring WebView settings.", e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
