package com.amr.kanjiwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amr.kanjiwidget.data.KanjiEntry
import com.amr.kanjiwidget.data.Word
import com.amr.kanjiwidget.tts.JapaneseSpeaker
import com.amr.kanjiwidget.ui.AnkiScreen
import com.amr.kanjiwidget.ui.HomeScreen
import com.amr.kanjiwidget.ui.KanjiDetailScreen
import com.amr.kanjiwidget.ui.KanjiListScreen
import com.amr.kanjiwidget.ui.KotobaListScreen
import com.amr.kanjiwidget.ui.WarmAccent
import com.amr.kanjiwidget.ui.WarmAccentSoft
import com.amr.kanjiwidget.ui.WarmBg
import com.amr.kanjiwidget.ui.WarmBorder
import com.amr.kanjiwidget.ui.WarmCream
import com.amr.kanjiwidget.ui.WarmMuted
import com.amr.kanjiwidget.ui.WarmSurfaceHigh
import com.amr.kanjiwidget.ui.WarmSurfaceHigher
import com.amr.kanjiwidget.ui.WordDetailScreen
import com.amr.kanjiwidget.ui.KanjiTheme

private enum class Tab(
    val label: String,
    val iconRes: Int
) {
    HOME("Beranda", R.drawable.ic_home_jp),
    KANJI("Kanji", R.drawable.ic_kanji),
    KOTOBA("Kotoba", R.drawable.ic_kotoba),
    ANKI("Anki", R.drawable.ic_anki)
}

private sealed interface Screen {
    data object Tabs : Screen
    data class KanjiDetail(val entry: KanjiEntry) : Screen
    data class WordDetail(val word: Word) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KanjiTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val speaker = remember { JapaneseSpeaker(context) }
    var tab by remember { mutableStateOf(Tab.HOME) }
    var screen by remember { mutableStateOf<Screen>(Screen.Tabs) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { speaker.shutdown() }
    }

    // System back from a detail screen returns to the previous screen, not the launcher.
    BackHandler(enabled = screen !is Screen.Tabs) {
        screen = Screen.Tabs
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (screen is Screen.Tabs) {
                WarmBottomBar(current = tab, onSelect = { tab = it })
            }
        }
    ) { padding ->
        val content = Modifier.padding(padding)
        when (val s = screen) {
            Screen.Tabs -> Box(content) {
                when (tab) {
                    Tab.HOME -> HomeScreen(
                        onOpenKanji = { screen = Screen.KanjiDetail(it) },
                        onSpeak = { speaker.speak(it) }
                    )
                    Tab.KANJI -> KanjiListScreen(
                        onSpeak = { speaker.speak(it) },
                        onOpen = { screen = Screen.KanjiDetail(it) }
                    )
                    Tab.KOTOBA -> KotobaListScreen(
                        onSpeak = { speaker.speak(it) },
                        onOpen = { screen = Screen.WordDetail(it) }
                    )
                    Tab.ANKI -> AnkiScreen(onSpeak = { speaker.speak(it) })
                }
            }
            is Screen.KanjiDetail -> Box(content) {
                KanjiDetailScreen(
                    entry = s.entry,
                    onSpeak = { speaker.speak(it) }
                )
            }
            is Screen.WordDetail -> Box(content) {
                WordDetailScreen(
                    word = s.word,
                    onSpeak = { speaker.speak(it) },
                    onOpenKanji = { screen = Screen.KanjiDetail(it) }
                )
            }
        }
    }
}

/**
 * Floating warm-dark bottom bar: compact icon + label, pill indicator that
 * springs under the active tab.
 */
@Composable
private fun WarmBottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(WarmSurfaceHigher, WarmSurfaceHigh)
                )
            )
    ) {
        // hairline top edge keeps the bar from floating flat against the bg
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, WarmBorder, Color.Transparent)
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { t ->
                BottomTabItem(
                    tab = t,
                    selected = current == t,
                    onClick = { onSelect(t) }
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) WarmAccent else WarmMuted,
        label = "tabIcon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) WarmCream else WarmMuted,
        label = "tabLabel"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 26.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabIndicator"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier.size(21.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            maxLines = 1
        )
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    if (selected) {
                        Brush.horizontalGradient(
                            listOf(
                                WarmAccent.copy(alpha = 0.35f),
                                WarmAccent,
                                WarmAccentSoft,
                                WarmAccent.copy(alpha = 0.35f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
        )
    }
}
