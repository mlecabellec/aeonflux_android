package com.aeonflux.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.aeonflux.app.databinding.ActivityMainBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.welcomeText.setText("Welcome to AeonFlux (Java Edition)!");
    }
}
