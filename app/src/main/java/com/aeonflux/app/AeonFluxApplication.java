package com.aeonflux.app;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AeonFluxApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
