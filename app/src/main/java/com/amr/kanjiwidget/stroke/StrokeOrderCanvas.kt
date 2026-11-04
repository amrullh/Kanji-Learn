package com.amr.kanjiwidget.stroke

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class StrokeMode { WATCH, TRACE, PRACTICE }

/**
 * Lightweight kanji stroke canvas.
 * WATCH: animated playback of stroke order.
 * TRACE: faint guide strokes to follow with a finger.
 * PRACTICE: draw freely on top of faint guides.
 */
@Composable
fun StrokeOrderCanvas(
    strokes: List<StrokePath>,
    mode: StrokeMode,
    guideColor: Color,
    strokeColor: Color,
    startDotColor: Color,
    userColor: Color,
    onStrokeComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (strokes.isEmpty()) return

    var currentStroke by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var userPaths by remember { mutableStateOf(listOf<Pair<Offset, List<Offset>>>()) }
    var activePath by remember { mutableStateOf<List<Offset>?>(null) }

    LaunchedEffect(mode, strokes) {
        currentStroke = 0
        progress = 0f
        userPaths = emptyList()
        activePath = null
    }

    LaunchedEffect(mode, currentStroke, strokes) {
        if (mode != StrokeMode.WATCH) return@LaunchedEffect
        if (currentStroke >= strokes.size) {
            delay(1200)
            currentStroke = 0
            progress = 0f
            return@LaunchedEffect
        }
        progress = 0f
        val duration = (strokes[currentStroke].length * 12).toInt().coerceIn(120, 900)
        val anim = Animatable(0f)
        anim.animateTo(1f, tween(duration, easing = LinearEasing)) {
            progress = value
        }
        onStrokeComplete(currentStroke)
        delay(220)
        currentStroke += 1
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(mode, strokes) {
                    if (mode == StrokeMode.WATCH) return@pointerInput
                    val vb = StrokeBuilder.VIEWBOX
                    detectDragGestures(
                        onDragStart = { offset ->
                            val p = Offset(
                                offset.x / size.width * vb,
                                offset.y / size.height * vb
                            )
                            activePath = listOf(p)
                        },
                        onDrag = { change, _ ->
                            val p = Offset(
                                change.position.x / size.width * vb,
                                change.position.y / size.height * vb
                            )
                            activePath = (activePath ?: emptyList()) + p
                        },
                        onDragEnd = {
                            activePath?.let { userPaths = userPaths + (Offset.Zero to it) }
                            activePath = null
                            onStrokeComplete(currentStroke)
                            if (mode == StrokeMode.PRACTICE) {
                                currentStroke = (currentStroke + 1).coerceAtMost(strokes.size)
                            }
                        },
                        onDragCancel = { activePath = null }
                    )
                }
        ) {
            val scale = size.minDimension / StrokeBuilder.VIEWBOX
            withTransform({ scale(scale, scale, pivot = Offset.Zero) }) {
                val vb = StrokeBuilder.VIEWBOX
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset.Zero,
                    size = Size(vb, vb)
                )

                // Guide strokes: all completed strokes plus the active one while watching.
                strokes.forEachIndexed { index, stroke ->
                    val showGuide = when (mode) {
                        StrokeMode.WATCH -> index < currentStroke
                        StrokeMode.TRACE -> true
                        StrokeMode.PRACTICE -> index < currentStroke
                    }
                    if (showGuide) {
                        drawPath(
                            path = stroke.path,
                            color = strokeColor,
                            style = Stroke(width = 3.5f)
                        )
                    }
                }

                // Active stroke being animated.
                if (mode == StrokeMode.WATCH && currentStroke < strokes.size) {
                    val stroke = strokes[currentStroke]
                    drawPath(
                        path = stroke.path,
                        color = strokeColor.copy(alpha = 0.18f),
                        style = Stroke(width = 3.5f)
                    )
                    drawPartialStroke(
                        stroke = stroke,
                        progress = progress,
                        color = strokeColor,
                        width = 4f
                    )
                }

                // Start dots for the next stroke to write.
                val nextIndex = if (mode == StrokeMode.WATCH) currentStroke else currentStroke
                if (nextIndex < strokes.size) {
                    val s = strokes[nextIndex]
                    drawCircle(
                        color = startDotColor,
                        radius = 3.2f,
                        center = Offset(s.startX, s.startY)
                    )
                }

                // User-drawn strokes.
                userPaths.forEach { (_, pts) ->
                    if (pts.size >= 2) {
                        val p = androidx.compose.ui.graphics.Path()
                        p.moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) p.lineTo(pts[i].x, pts[i].y)
                        drawPath(path = p, color = userColor, style = Stroke(width = 4.5f))
                    }
                }
                activePath?.let { pts ->
                    if (pts.size >= 2) {
                        val p = androidx.compose.ui.graphics.Path()
                        p.moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) p.lineTo(pts[i].x, pts[i].y)
                        drawPath(path = p, color = userColor, style = Stroke(width = 4.5f))
                    }
                }
            }
        }
    }
}

/** Draws the first `progress` fraction of a stroke from its precomputed polyline. */
private fun DrawScope.drawPartialStroke(
    stroke: StrokePath,
    progress: Float,
    color: Color,
    width: Float
) {
    val pts = stroke.points
    if (pts.size < 2 || progress <= 0f) return
    val cut = ((pts.size - 1) * progress).toInt().coerceAtLeast(1)
    val p = androidx.compose.ui.graphics.Path()
    p.moveTo(pts[0].x, pts[0].y)
    for (i in 1..cut) p.lineTo(pts[i].x, pts[i].y)
    drawPath(path = p, color = color, style = Stroke(width = width))
}
