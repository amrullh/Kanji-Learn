package com.amr.kanjiwidget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.amr.kanjiwidget.data.KanjiRepository

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        KanjiRepository.advanceWindow(context)
        KanjiGlanceWidget().updateAll(context)
    }
}
