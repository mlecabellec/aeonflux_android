package com.aeonflux.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AeonFluxApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
