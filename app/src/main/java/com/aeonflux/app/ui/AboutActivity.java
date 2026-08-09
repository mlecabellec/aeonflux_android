/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-003 / TSK-20260809-003.1 - About Activity Implementation.
 */
package com.aeonflux.app.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aeonflux.app.R;

import java.util.logging.Level;
import java.util.logging.Logger;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * [TSK-20260809-003.1] Activity displaying application overview, licenses, and third-party
 * attributions via an embedded local asset WebView (about.html).
 */
@AndroidEntryPoint
public class AboutActivity extends AppCompatActivity {

    private static final Logger LOGGER = Logger.getLogger(AboutActivity.class.getName());

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("[TSK-20260809-003.1] Initializing AboutActivity.");

        try {
            setContentView(R.layout.activity_about);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("About AeonFlux");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            webView = findViewById(R.id.webview_about);
            progressBar = findViewById(R.id.progress_about_loading);

            if (webView != null) {
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(false);
                settings.setAllowFileAccess(true);
                settings.setDomStorageEnabled(false);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                LOGGER.fine("[TSK-20260809-003.1] Loading local asset about.html");
                webView.loadUrl("file:///android_asset/about.html");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[TSK-20260809-003.1] Exception initializing AboutActivity.", e);
            Toast.makeText(this, "Unable to load About information", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            try {
                webView.stopLoading();
                webView.destroy();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "[TSK-20260809-003.1] Exception destroying webView", e);
            }
        }
    }
}
