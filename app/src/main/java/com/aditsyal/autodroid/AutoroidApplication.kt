package com.aditsyal.autodroid

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AutoroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        // Check if app is debuggable instead of using BuildConfig
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
