package com.amr.kanjiwidget

import android.app.Application

class KanjiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Widget rotation is handled by Glance's updatePeriodMillis (4h) in
        // kanji_widget_info.xml — no WorkManager needed for v1.
    }
}
