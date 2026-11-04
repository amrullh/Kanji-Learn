package com.amr.kanjiwidget.data

import kotlinx.serialization.Serializable

@Serializable
data class Theme(
    val title: String,
    val words: List<Word>
)

@Serializable
data class Word(
    val surface: String,
    val reading: String,
    val meaning: String,
    val sentence: String,
    @kotlinx.serialization.SerialName("sentence_reading")
    val sentenceReading: String = "",
    val sentences: List<Sentence> = emptyList()
)

@Serializable
data class Sentence(
    val jp: String,
    val reading: String,
    val id: String
)

/** One reviewable card: either a kanji or a kotoba entry. */
sealed interface Flashcard {
    val id: String
    val title: String
    val reading: String
    val meaning: String
    val sentence: String
    val sentenceReading: String

    data class KanjiCard(val entry: KanjiEntry) : Flashcard {
        override val id get() = "k:${entry.kanji}"
        override val title get() = entry.kanji
        override val reading get() = entry.kun.firstOrNull() ?: entry.on.firstOrNull() ?: ""
        override val meaning get() = entry.meaning
        override val sentence get() = entry.sentence
        override val sentenceReading get() = entry.sentenceReading
    }

    data class WordCard(val word: Word) : Flashcard {
        override val id get() = "w:${word.surface}"
        override val title get() = word.surface
        override val reading get() = word.reading
        override val meaning get() = word.meaning
        override val sentence get() = word.sentence
        override val sentenceReading get() = word.sentenceReading
    }
}

@Serializable
data class KotobaDataset(
    val themes: List<Theme>
)

@Serializable
data class KanjiEntry(
    val kanji: String,
    val on: List<String> = emptyList(),
    val kun: List<String> = emptyList(),
    val meaning: String,
    val words: List<KanjiWord> = emptyList(),
    val sentence: String,
    @kotlinx.serialization.SerialName("sentence_reading")
    val sentenceReading: String = "",
    val sentences: List<Sentence> = emptyList(),
    val level: String
)

@Serializable
data class KanjiWord(
    val surface: String,
    val reading: String,
    val meaning: String
)

@Serializable
data class KanjiDataset(
    val kanji: List<KanjiEntry>
)

/** Stroke order data for one kanji: SVG path per stroke plus its start point. */
@Serializable
data class StrokeData(
    val strokes: List<String>,
    val starts: List<List<Float>> = emptyList()
)
