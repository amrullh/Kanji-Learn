package com.amr.kanjiwidget.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.amr.kanjiwidget.MainActivity
import com.amr.kanjiwidget.R
import com.amr.kanjiwidget.data.KanjiEntry
import com.amr.kanjiwidget.data.KanjiRepository

class KanjiGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry: KanjiEntry? =
            KanjiRepository.kanjiForCurrentWindow(context, KanjiRepository.widgetOffset(context))

        provideContent {
            GlanceTheme {
                if (entry != null) {
                    WidgetContent(
                        context = context,
                        kanji = entry.kanji,
                        reading = entry.kun.firstOrNull() ?: entry.on.firstOrNull() ?: "",
                        meaning = entry.meaning,
                        level = entry.level
                    )
                }
            }
        }
    }
}

class KanjiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KanjiGlanceWidget()
}

private val BG = ColorProvider(Color(0xFF1F1B18))
private val CREAM = ColorProvider(Color(0xFFF2EBE2))
private val ACCENT = ColorProvider(Color(0xFFE07856))
private val ACCENT_SOFT = ColorProvider(Color(0xFFF2C094))
private val MUTED = ColorProvider(Color(0xFFA89F94))
private val SURFACE_HIGH = ColorProvider(Color(0xFF2C2620))

@Composable
private fun WidgetContent(
    context: Context,
    kanji: String,
    reading: String,
    meaning: String,
    level: String
) {
    val detailIntent = Intent().apply {
        component = ComponentName(context.packageName, MainActivity::class.java.name)
        putExtra("kanji", kanji)
    }
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BG)
            .cornerRadius(24.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(detailIntent)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kanji hero
            Column(
                modifier = GlanceModifier.width(92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = kanji,
                    style = TextStyle(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = CREAM
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "JLPT $level",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ACCENT
                    )
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            // Reading + meaning
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = reading,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = ACCENT_SOFT
                    ),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(6.dp))
                Text(
                    text = meaning,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MUTED
                    ),
                    maxLines = 2
                )
            }

            Spacer(GlanceModifier.width(8.dp))

            // Skip / refresh button
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .cornerRadius(20.dp)
                    .background(SURFACE_HIGH)
                    .clickable(actionRunCallback<RefreshWidgetAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "Kanji berikutnya",
                    modifier = GlanceModifier.size(20.dp)
                )
            }
        }
    }
}
