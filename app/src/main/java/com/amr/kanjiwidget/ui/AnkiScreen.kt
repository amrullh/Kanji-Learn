package com.amr.kanjiwidget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.amr.kanjiwidget.data.Flashcard
import com.amr.kanjiwidget.data.KanjiRepository
import com.amr.kanjiwidget.data.SrsRating
import com.amr.kanjiwidget.data.SrsStore

/**
 * Anki-style review over the combined kanji + kotoba deck.
 *
 * The queue is driven by SM-2 scheduling: cards due now come first, then
 * learning cards, then cards not yet seen. Review state persists across
 * restarts, so reopening the app resumes where the deck left off instead of
 * restarting from the first card.
 */
@Composable
fun AnkiScreen(onSpeak: (String) -> Unit) {
    val context = LocalContext.current
    val store = remember { SrsStore(context) }
    val kanji = remember { KanjiRepository.kanji(context).kanji }
    val words = remember { KanjiRepository.allWords(context) }

    val allCards = remember {
        (kanji.map { Flashcard.KanjiCard(it) } + words.map { Flashcard.WordCard(it) })
    }

    // Session queue: cards due now (or new), ordered most-overdue first.
    var queue by remember { mutableStateOf(buildQueue(allCards, store)) }
    var queuePos by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var sessionReviewed by remember { mutableIntStateOf(0) }
    var sessionVersion by remember { mutableIntStateOf(0) }
    // Cards answered in this session, held back until the queue is exhausted.
    val answeredThisSession = remember { mutableListOf<String>() }
    var sessionDone by remember { mutableStateOf(false) }

    val card = queue.getOrNull(queuePos)
    val title = card?.title ?: ""
    val reading = card?.reading ?: ""
    val meaning = card?.meaning ?: ""
    val sentence = card?.sentence ?: ""
    val sentenceReading = card?.sentenceReading ?: ""

    fun answer(rating: SrsRating) {
        val current = card ?: return
        store.review(current.id, rating)
        sessionReviewed++
        // Hold the card back for the rest of this session; a card rated Again
        // is due immediately, so without this it would reappear at position 0
        // forever and the counter would never advance.
        answeredThisSession.add(current.id)
        val remaining = queue.filterNot { answeredThisSession.contains(it.id) }
        if (remaining.isEmpty()) {
            // Session finished: release everything and start a fresh pass.
            answeredThisSession.clear()
            queue = buildQueue(allCards, store)
            sessionDone = true
        } else {
            queue = remaining
            sessionDone = false
        }
        queuePos = 0
        revealed = false
        sessionVersion++
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (queue.isEmpty()) {
                    "Semua kartu selesai"
                } else {
                    "Kartu ${queuePos + 1} dari ${queue.size}"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "Sesi: $sessionReviewed",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (queue.isEmpty()) 0f else (queuePos + 1f) / queue.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            if (sessionDone) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sesi selesai",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$sessionReviewed kartu sudah direview.\nJadwal berikutnya sudah diperbarui.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            sessionDone = false
                            queue = buildQueue(allCards, store)
                            queuePos = 0
                            revealed = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mulai sesi baru")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (revealed) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(reading, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            meaning,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                        if (sentence.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                sentence,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            if (sentenceReading.isNotBlank()) {
                                Text(
                                    sentenceReading,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tekan 'Tampilkan' untuk melihat arti",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!sessionDone) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(onClick = { onSpeak(title) }) { Text("Dengar kata") }
                if (revealed && sentence.isNotBlank()) {
                    OutlinedButton(onClick = { onSpeak(sentence) }) { Text("Dengar kalimat") }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!revealed) {
                Button(
                    onClick = { revealed = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tampilkan jawaban", fontSize = 16.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RatingButton("Again", SrsRating.AGAIN, Color(0xFFC62828)) { answer(it) }
                    RatingButton("Hard", SrsRating.HARD, Color(0xFFEF6C00)) { answer(it) }
                    RatingButton("Good", SrsRating.GOOD, Color(0xFF2E7D32)) { answer(it) }
                    RatingButton("Easy", SrsRating.EASY, Color(0xFF1565C0)) { answer(it) }
                }
            }
        }
    }
}

/**
 * Orders the deck the way Anki does: overdue cards first (most overdue at the
 * top), then cards due today, then new cards never reviewed.
 */
private fun buildQueue(cards: List<Flashcard>, store: SrsStore): List<Flashcard> {
    val now = System.currentTimeMillis()
    return cards
        .map { it to store.card(it.id) }
        .sortedWith(
            compareBy(
                // Overdue first: smaller (more negative) dueAt sorts earlier.
                { it.second.dueAt - now },
                // Never-reviewed cards after reviewed ones.
                { if (it.second.reviews == 0) 1 else 0 }
            )
        )
        .map { it.first }
}

@Composable
private fun RowScope.RatingButton(
    label: String,
    rating: SrsRating,
    color: Color,
    onClick: (SrsRating) -> Unit
) {
    Button(
        onClick = { onClick(rating) },
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
    ) {
        Text(label, fontSize = 12.sp, maxLines = 1)
    }
}
