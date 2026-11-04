package com.amr.kanjiwidget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjiwidget.data.KanjiEntry
import com.amr.kanjiwidget.data.KanjiRepository
import com.amr.kanjiwidget.stroke.StrokePath
import com.amr.kanjiwidget.stroke.StrokeBuilder
import com.amr.kanjiwidget.stroke.StrokeMode
import com.amr.kanjiwidget.stroke.StrokeOrderCanvas

/** Detail page for a kanji: readings, meaning, example words, sentences, stroke order. */
@Composable
fun KanjiDetailScreen(
    entry: KanjiEntry,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val sentences = remember(entry) {
        entry.sentences.ifEmpty {
            listOf(
                com.amr.kanjiwidget.data.Sentence(
                    jp = entry.sentence,
                    reading = entry.sentenceReading,
                    id = ""
                )
            )
        }
    }
    val strokes: List<StrokePath> = remember(entry) {
        val data = KanjiRepository.strokes(context)[entry.kanji]
        if (data != null) {
            StrokeBuilder.build(
                data.strokes,
                data.starts.map { listOf(it[0].toDouble(), it[1].toDouble()) }
            )
        } else {
            emptyList()
        }
    }
    var mode by remember { mutableStateOf(StrokeMode.WATCH) }
    var strokeProgress by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.kanji,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(96.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (entry.on.isNotEmpty()) {
                        Text(
                            text = "On: ${entry.on.joinToString(", ")}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (entry.kun.isNotEmpty()) {
                        Text(
                            text = "Kun: ${entry.kun.joinToString(", ")}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.meaning,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                SpeakButton(text = entry.kanji, onSpeak = onSpeak)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(text = entry.level)
                Badge(text = "${strokes.size} gores")
            }
        }

        if (strokes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Urutan gores",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StrokeChip("Tonton", mode == StrokeMode.WATCH) { mode = StrokeMode.WATCH }
                StrokeChip("Telusuri", mode == StrokeMode.TRACE) { mode = StrokeMode.TRACE }
                StrokeChip("Latihan", mode == StrokeMode.PRACTICE) { mode = StrokeMode.PRACTICE }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StrokeOrderCanvas(
                        strokes = strokes,
                        mode = mode,
                        guideColor = MaterialTheme.colorScheme.outline,
                        strokeColor = MaterialTheme.colorScheme.primary,
                        startDotColor = MaterialTheme.colorScheme.secondary,
                        userColor = MaterialTheme.colorScheme.secondary,
                        onStrokeComplete = { strokeProgress = it + 1 },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (mode) {
                            StrokeMode.WATCH -> "Gores ${(strokeProgress).coerceAtMost(strokes.size)} dari ${strokes.size}"
                            StrokeMode.TRACE -> "Ikuti gores tipis dengan jari"
                            StrokeMode.PRACTICE -> "Tulis sendiri, gores tipis sebagai panduan"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (entry.words.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Contoh kata",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    entry.words.forEach { w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = w.surface,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(96.dp)
                            )
                            Text(
                                text = w.reading,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = w.meaning,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Kalimat percakapan",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        sentences.forEach { sentence ->
            SentenceCard(sentence = sentence, onSpeak = onSpeak)
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun StrokeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = null
    )
}
