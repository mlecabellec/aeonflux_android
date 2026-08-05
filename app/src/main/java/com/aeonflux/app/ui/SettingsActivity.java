/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards & Settings Activity Compliance
 * Reference: REQ-00070 / TSK-20260805-001 - SettingsActivity implementation.
 */
package com.aeonflux.app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.aeonflux.app.R;

/**
 * [TSK-20260805-001] Activity for configuring application preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
