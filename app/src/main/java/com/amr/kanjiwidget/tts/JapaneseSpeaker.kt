package com.amr.kanjiwidget.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** Japanese TTS helper. Reports whether a Japanese voice is available. */
class JapaneseSpeaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var japaneseAvailable = false

    /** Invoked when Japanese voice availability changes. */
    var onAvailabilityChanged: ((Boolean) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                val engine = tts
                if (engine != null) {
                    val res = engine.setLanguage(Locale.JAPAN)
                    japaneseAvailable = res != TextToSpeech.LANG_MISSING_DATA &&
                        res != TextToSpeech.LANG_NOT_SUPPORTED
                    if (japaneseAvailable) {
                        engine.setSpeechRate(0.9f)
                    }
                }
            }
            onAvailabilityChanged?.invoke(japaneseAvailable)
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun stop() {
        tts?.stop()
    }

    fun isJapaneseAvailable(): Boolean = japaneseAvailable

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
