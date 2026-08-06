/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-003 / TSK-20260806-001 - Activity to manually add new RSS feed.
 */
package com.aeonflux.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.aeonflux.app.R;
import com.aeonflux.app.core.database.DatabaseService;
import com.aeonflux.app.core.database.entities.SourceEntity;

import dagger.hilt.android.AndroidEntryPoint;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;

@AndroidEntryPoint
public class AddSourceActivity extends AppCompatActivity {

    private EditText editTitle;
    private EditText editUrl;
    private EditText editCategory;
    private Button btnSave;

    @Inject
    DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_source);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add New Feed");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTitle = findViewById(R.id.edit_source_title);
        editUrl = findViewById(R.id.edit_source_url);
        editCategory = findViewById(R.id.edit_source_category);
        btnSave = findViewById(R.id.btn_save_source);

        btnSave.setOnClickListener(v -> saveSource());
    }

    private void saveSource() {
        String title = editTitle.getText().toString().trim();
        String url = editUrl.getText().toString().trim();

        if (url.isEmpty()) {
            editUrl.setError("URL is required");
            return;
        }

        if (title.isEmpty()) {
            title = url;
        }

        SourceEntity existing = databaseService.getSourceByUrl(url);
        if (existing != null) {
            Toast.makeText(this, "Feed already exists in database", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = "src_" + UUID.randomUUID().toString().substring(0, 8);
        SourceEntity source = new SourceEntity(id, url, title, "Manual entry", null, "RSS", 60, System.currentTimeMillis(), 0);
        databaseService.insertSource(source);

        Toast.makeText(this, "Feed added successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
