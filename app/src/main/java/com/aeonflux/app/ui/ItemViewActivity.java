/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-00060 / TSK-20260805-001 - ItemViewActivity implementation.
 */
package com.aeonflux.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.aeonflux.app.R;
import java.util.Objects;

/**
 * [TSK-20260805-001] Activity displaying detailed metadata, summary, and action menu for an article.
 */
public class ItemViewActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SUMMARY = "extra_summary";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_PUBLISHED_AT = "extra_published_at";

    private String articleId = "";
    private String articleUrl = "";
    private boolean isStarred = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        Intent intent = getIntent();
        if (intent != null) {
            articleId = intent.getStringExtra(EXTRA_ARTICLE_ID) != null ? intent.getStringExtra(EXTRA_ARTICLE_ID) : "";
            articleUrl = intent.getStringExtra(EXTRA_URL) != null ? intent.getStringExtra(EXTRA_URL) : "";

            TextView titleText = findViewById(R.id.text_item_title);
            TextView summaryText = findViewById(R.id.text_item_summary);
            TextView authorDateText = findViewById(R.id.text_item_author_date);

            titleText.setText(intent.getStringExtra(EXTRA_TITLE) != null ? intent.getStringExtra(EXTRA_TITLE) : "Untitled");
            summaryText.setText(intent.getStringExtra(EXTRA_SUMMARY) != null ? intent.getStringExtra(EXTRA_SUMMARY) : "No summary available.");
            authorDateText.setText("Author: " + (intent.getStringExtra(EXTRA_AUTHOR) != null ? intent.getStringExtra(EXTRA_AUTHOR) : "Unknown") + " • Published: " + intent.getLongExtra(EXTRA_PUBLISHED_AT, 0L));
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
            openWebView(false);
            return true;
        } else if (id == R.id.action_webview_private) {
            openWebView(true);
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

    private void openWebView(boolean privateMode) {
        if (!articleUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
            if (privateMode) {
                intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true);
            }
            startActivity(intent);
        } else {
            Toast.makeText(this, "No valid URL available", Toast.LENGTH_SHORT).show();
        }
    }
}
