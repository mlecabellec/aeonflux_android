package com.aeonflux.app;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AeonFluxApplication extends Application {
    private static AeonFluxApplication instance;


    @javax.inject.Inject
    com.aeonflux.app.core.database.DatabaseService databaseService;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static com.aeonflux.app.core.database.DatabaseService getDatabaseService() {
        return instance != null ? instance.databaseService : null;
    }
}

