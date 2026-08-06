/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: REQ-OPML-002 / TSK-20260806-001 - Activity to export feeds to OPML format.
 */
package com.aeonflux.app.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.aeonflux.app.R;
import com.aeonflux.app.core.database.DatabaseService;
import com.aeonflux.app.core.database.entities.SourceEntity;
import com.aeonflux.app.core.opml.OpmlExporter;

import dagger.hilt.android.AndroidEntryPoint;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import javax.inject.Inject;

@AndroidEntryPoint
public class ExportOpmlActivity extends AppCompatActivity {

    private Button btnExportFile;
    private TextView textStatus;

    @Inject
    DatabaseService databaseService;

    private final ActivityResultLauncher<String> createDocumentLauncher =
        registerForActivityResult(new ActivityResultContracts.CreateDocument("text/x-opml"), this::onTargetFileSelected);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_opml);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Export OPML Feeds");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnExportFile = findViewById(R.id.btn_select_export_target);
        textStatus = findViewById(R.id.text_export_status);

        btnExportFile.setOnClickListener(v -> createDocumentLauncher.launch("aeonflux_subscriptions.opml"));
    }

    private void onTargetFileSelected(Uri uri) {
        if (uri == null) return;
        textStatus.setText("Exporting sources to: " + uri.getPath());
        btnExportFile.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    runOnUiThread(() -> textStatus.setText("Error: Unable to write to target file."));
                    return;
                }

                List<SourceEntity> allSources = databaseService.getAllSources();
                Map<String, List<SourceEntity>> map = new HashMap<>();
                map.put("Feeds", allSources);

                OpmlExporter exporter = new OpmlExporter();
                exporter.export(map, outputStream);

                runOnUiThread(() -> {
                    btnExportFile.setEnabled(true);
                    textStatus.setText("Export completed successfully!\nExported " + allSources.size() + " sources.");
                    Toast.makeText(ExportOpmlActivity.this, "Export finished!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnExportFile.setEnabled(true);
                    textStatus.setText("Error exporting OPML: " + e.getMessage());
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
