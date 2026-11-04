package com.amr.kanjiwidget.data

import android.content.Context
import kotlinx.serialization.json.Json

/** Loads the bundled JFT datasets, stroke data, and owns widget rotation state. */
object KanjiRepository {

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var kotobaCache: KotobaDataset? = null

    @Volatile
    private var kanjiCache: KanjiDataset? = null

    @Volatile
    private var strokesCache: Map<String, StrokeData>? = null

    fun kotoba(context: Context): KotobaDataset =
        kotobaCache ?: synchronized(this) {
            kotobaCache ?: json.decodeFromString<KotobaDataset>(
                context.assets.open("kotoba.json").bufferedReader().readText()
            ).also { kotobaCache = it }
        }

    fun kanji(context: Context): KanjiDataset =
        kanjiCache ?: synchronized(this) {
            kanjiCache ?: json.decodeFromString<KanjiDataset>(
                context.assets.open("kanji.json").bufferedReader().readText()
            ).also { kanjiCache = it }
        }

    fun strokes(context: Context): Map<String, StrokeData> =
        strokesCache ?: synchronized(this) {
            strokesCache ?: json.decodeFromString<Map<String, StrokeData>>(
                context.assets.open("strokes.json").bufferedReader().readText()
            ).also { strokesCache = it }
        }

    fun allWords(context: Context): List<Word> =
        kotoba(context).themes.flatMap { theme -> theme.words }

    fun findKanji(context: Context, kanji: String): KanjiEntry? =
        kanji(context).kanji.firstOrNull { it.kanji == kanji }

    fun findWord(context: Context, surface: String): Word? =
        allWords(context).firstOrNull { it.surface == surface }

    /** Kanji characters that compose a word, in order, for drill-down navigation. */
    fun kanjiInWord(context: Context, word: Word): List<KanjiEntry> =
        word.surface.mapNotNull { ch -> findKanji(context, ch.toString()) }
            .distinctBy { it.kanji }

    /** Deterministic 4-hour rotation index: same window yields the same kanji. */
    fun rotationIndex(): Int {
        val cal = java.util.Calendar.getInstance()
        val dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
        val year = cal.get(java.util.Calendar.YEAR)
        val window = cal.get(java.util.Calendar.HOUR_OF_DAY) / 4
        return (year * 366 + dayOfYear) * 6 + window
    }

    /** Kanji for the current time window, plus any manual skip offset. */
    fun kanjiForCurrentWindow(context: Context, offset: Int = 0): KanjiEntry? {
        val list = kanji(context).kanji
        if (list.isEmpty()) return null
        return list[((rotationIndex() + offset) % list.size + list.size) % list.size]
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("kanji_widget_prefs", Context.MODE_PRIVATE)

    fun widgetOffset(context: Context): Int = prefs(context).getInt(KEY_OFFSET, 0)

    fun advanceWindow(context: Context) {
        prefs(context).edit()
            .putInt(KEY_OFFSET, widgetOffset(context) + 1)
            .apply()
    }

    private const val KEY_OFFSET = "manual_offset"
}
