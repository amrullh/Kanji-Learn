package com.amr.kanjiwidget.stroke

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import kotlin.math.sqrt

/**
 * Minimal SVG path parser -> Compose Path.
 *
 * KanjiVG paths use M, C, c, S and s. The lowercase forms are relative to the
 * current point, so the parser tracks that point while it walks the command
 * list. Treating them as absolute collapses every curve onto the origin and
 * the kanji turns into a scribble.
 */
object SvgPathParser {

    fun parse(d: String): Path {
        val path = Path()
        var cx = 0f
        var cy = 0f
        var lastCtrlX = 0f
        var lastCtrlY = 0f
        var prevWasCubic = false

        for (cmd in parseCommands(d)) {
            val rel = cmd.command.isLowerCase()
            when (cmd.command.uppercaseChar()) {
                'M' -> {
                    var x = cmd.args[0]; var y = cmd.args[1]
                    if (rel) { x += cx; y += cy }
                    path.moveTo(x, y)
                    cx = x; cy = y
                    prevWasCubic = false
                }
                'C' -> {
                    var x1 = cmd.args[0]; var y1 = cmd.args[1]
                    var x2 = cmd.args[2]; var y2 = cmd.args[3]
                    var x = cmd.args[4]; var y = cmd.args[5]
                    if (rel) {
                        x1 += cx; y1 += cy
                        x2 += cx; y2 += cy
                        x += cx; y += cy
                    }
                    path.cubicTo(x1, y1, x2, y2, x, y)
                    lastCtrlX = x2; lastCtrlY = y2
                    cx = x; cy = y
                    prevWasCubic = true
                }
                'S' -> {
                    var x2 = cmd.args[0]; var y2 = cmd.args[1]
                    var x = cmd.args[2]; var y = cmd.args[3]
                    if (rel) {
                        x2 += cx; y2 += cy
                        x += cx; y += cy
                    }
                    // Smooth cubic: first control point mirrors the previous one.
                    val x1 = if (prevWasCubic) 2 * cx - lastCtrlX else cx
                    val y1 = if (prevWasCubic) 2 * cy - lastCtrlY else cy
                    path.cubicTo(x1, y1, x2, y2, x, y)
                    lastCtrlX = x2; lastCtrlY = y2
                    cx = x; cy = y
                    prevWasCubic = true
                }
            }
        }
        return path
    }

    private data class Cmd(val command: Char, val args: List<Float>)

    /** Tokenizes a path string into commands, preserving case (relative vs absolute). */
    private fun parseCommands(d: String): List<Cmd> {
        val result = mutableListOf<Cmd>()
        var i = 0
        val n = d.length
        while (i < n) {
            val ch = d[i]
            if (ch.isLetter()) {
                val upper = ch.uppercaseChar()
                val argCount = when (upper) {
                    'M' -> 2
                    'C' -> 6
                    'S' -> 4
                    else -> 0
                }
                if (argCount == 0) { i++; continue }
                val args = mutableListOf<Float>()
                i++
                while (args.size < argCount && i < n) {
                    while (i < n && (d[i] == ',' || d[i] == ' ' || d[i] == '\n' || d[i] == '\t' || d[i] == '\r')) i++
                    if (i >= n) break
                    val start = i
                    if (d[i] == '-' || d[i] == '+') i++
                    while (i < n && (d[i].isDigit() || d[i] == '.' || d[i] == 'e' || d[i] == 'E')) i++
                    if (i > start) args.add(d.substring(start, i).toFloatOrNull() ?: 0f)
                    else break
                }
                if (args.size == argCount) {
                    result.add(Cmd(ch, args))
                } else {
                    break
                }
            } else {
                i++
            }
        }
        return result
    }

    /** Path length approximation for animation timing. */
    fun approxLength(path: Path): Float {
        val bounds = path.getBounds()
        return sqrt((bounds.width * bounds.width + bounds.height * bounds.height).toDouble()).toFloat()
    }
}

data class StrokePath(
    val path: Path,
    val startX: Float,
    val startY: Float,
    val length: Float,
    val points: List<Offset>
)

object StrokeBuilder {

    /** KanjiVG coordinate space is 0..109. */
    const val VIEWBOX = 109f

    private const val SAMPLE_STEPS = 48

    fun build(strokePaths: List<String>, startPoints: List<List<Double>>): List<StrokePath> {
        return strokePaths.mapIndexed { index, d ->
            val path = SvgPathParser.parse(d)
            val start = startPoints.getOrNull(index)
            // Prefer the real start point from the parsed path when available.
            val bounds = path.getBounds()
            val startX = (start?.getOrNull(0) ?: bounds.left.toDouble()).toFloat()
            val startY = (start?.getOrNull(1) ?: bounds.top.toDouble()).toFloat()
            StrokePath(
                path = path,
                startX = startX,
                startY = startY,
                length = SvgPathParser.approxLength(path),
                points = samplePoints(path)
            )
        }
    }

    /** Samples a path into a polyline so partial drawing needs no per-frame measurement. */
    private fun samplePoints(path: Path): List<Offset> {
        val measure = android.graphics.PathMeasure()
        measure.setPath(path.asAndroidPath(), false)
        val len = measure.length
        if (len <= 0f) return emptyList()
        val out = ArrayList<Offset>(SAMPLE_STEPS + 1)
        val pos = FloatArray(2)
        for (i in 0..SAMPLE_STEPS) {
            measure.getPosTan(len * i / SAMPLE_STEPS, pos, null)
            out.add(Offset(pos[0], pos[1]))
        }
        return out
    }
}
