package com.amr.kanjiwidget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjiwidget.data.KanjiEntry
import com.amr.kanjiwidget.data.KanjiRepository
import com.amr.kanjiwidget.data.Word

/** Home tab: current widget kanji, quick stats, and a shortcut into the decks. */
@Composable
fun HomeScreen(
    onOpenKanji: (KanjiEntry) -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val entry = remember { KanjiRepository.kanjiForCurrentWindow(context, KanjiRepository.widgetOffset(context)) }
    val kanjiCount = remember { KanjiRepository.kanji(context).kanji.size }
    val wordCount = remember { KanjiRepository.allWords(context).size }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Widget menampilkan kanji ini sekarang",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        item {
            if (entry != null) {
                WidgetKanjiCard(entry = entry, onOpen = { onOpenKanji(entry) }, onSpeak = onSpeak)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(label = "Kanji", value = "$kanjiCount", modifier = Modifier.weight(1f))
                StatCard(label = "Kotoba", value = "$wordCount", modifier = Modifier.weight(1f))
            }
        }
        item {
            Text(
                text = "Rotasi widget: setiap 4 jam. Ketuk ↻ di widget untuk kanji berikutnya.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun WidgetKanjiCard(
    entry: KanjiEntry,
    onOpen: () -> Unit,
    onSpeak: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(text = entry.level)
                Text(
                    text = "${entry.sentences.size} kalimat",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.kanji,
                fontSize = 88.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.meaning,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = listOfNotNull(
                    entry.on.takeIf { it.isNotEmpty() }?.joinToString(", ") { "On: $it" },
                    entry.kun.takeIf { it.isNotEmpty() }?.joinToString(", ") { "Kun: $it" }
                ).joinToString("  •  "),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            SpeakButton(text = entry.kanji, onSpeak = onSpeak)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KanjiListScreen(onSpeak: (String) -> Unit, onOpen: (KanjiEntry) -> Unit) {
    val context = LocalContext.current
    val entries = remember { KanjiRepository.kanji(context).kanji }
    var levelFilter by remember { mutableStateOf<String?>(null) }
    val visible = remember(levelFilter) {
        entries.filter { levelFilter == null || it.level == levelFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelChip("Semua", levelFilter == null) { levelFilter = null }
            LevelChip("N5", levelFilter == "N5") { levelFilter = "N5" }
            LevelChip("N4", levelFilter == "N4") { levelFilter = "N4" }
            LevelChip("Ekstra", levelFilter == "EXTRA") { levelFilter = "EXTRA" }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visible.size, key = { visible[it].kanji }) { index ->
                val entry = visible[index]
                KanjiCard(entry = entry, onSpeak = onSpeak, onOpen = onOpen)
            }
        }
    }
}

@Composable
fun KotobaListScreen(onSpeak: (String) -> Unit, onOpen: (Word) -> Unit) {
    val context = LocalContext.current
    val themes = remember { KanjiRepository.kotoba(context).themes }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        themes.forEach { theme ->
            item(key = "header_${theme.title}") {
                SectionHeader(theme.title, theme.words.size)
            }
            // Chunked rows instead of a nested LazyVerticalGrid: a nested grid
            // with a fixed height clips the last partial row.
            theme.words.chunked(3).forEachIndexed { rowIndex, rowWords ->
                item(key = "row_${theme.title}_$rowIndex") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowWords.forEach { word ->
                            Box(modifier = Modifier.weight(1f)) {
                                WordCard(word = word, onSpeak = onSpeak, onOpen = onOpen)
                            }
                        }
                        // Fill the empty slots of a partial last row so cards
                        // keep a uniform width.
                        repeat(3 - rowWords.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item(key = "space_${theme.title}") {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LevelChip(label: String, selected: Boolean, onClick: () -> Unit) {
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

@Composable
fun WordCard(word: Word, onSpeak: (String) -> Unit, onOpen: (Word) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onOpen(word) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = word.surface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = word.reading,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = word.meaning,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun KanjiCard(entry: KanjiEntry, onSpeak: (String) -> Unit, onOpen: (KanjiEntry) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onOpen(entry) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = entry.kanji,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Reading preview: on-yomi and kun-yomi, the two ways the kanji is read.
            val onReading = entry.on.joinToString("・")
            val kunReading = entry.kun.joinToString("・")
            val reading = buildList {
                if (onReading.isNotBlank()) add(onReading)
                if (kunReading.isNotBlank()) add(kunReading)
            }.joinToString(" / ")
            Text(
                text = reading,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.meaning,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}
