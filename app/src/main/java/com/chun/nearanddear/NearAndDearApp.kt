package com.chun.nearanddear

import android.app.Application
import com.chun.nearanddear.logging.CrashFileLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NearAndDearApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashFileLogger.install(this)
    }
}
