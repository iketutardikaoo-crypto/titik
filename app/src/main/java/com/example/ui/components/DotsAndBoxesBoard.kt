package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DABPlayer
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color
import com.example.ui.theme.SlateBorder
import kotlin.math.abs

@Composable
fun DotsAndBoxesBoard(
    gridSize: Int, // e.g. 3 (3x3 boxes) or 4 (4x4 boxes)
    horizontalEdges: Set<Pair<Int, Int>>,
    verticalEdges: Set<Pair<Int, Int>>,
    boxes: Map<Pair<Int, Int>, DABPlayer>,
    onEdgeTapped: (isHorizontal: Boolean, row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val dotRadius = with(density) { 6.dp.toPx() }
    val edgeThickness = with(density) { 7.dp.toPx() }
    val strokeWidth2dp = with(density) { 2.dp.toPx() }
    val pad4dp = with(density) { 4.dp.toPx() }
    val pad8dp = with(density) { 8.dp.toPx() }
    val cornerRadius12dp = with(density) { 12.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .testTag("dots_and_boxes_board")
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val margin = 32f
        val playableWidth = width - 2 * margin
        val playableHeight = height - 2 * margin
        val stepX = playableWidth / gridSize
        val stepY = playableHeight / gridSize

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gridSize, horizontalEdges, verticalEdges) {
                    detectTapGestures { offset ->
                        val touchX = offset.x - margin
                        val touchY = offset.y - margin

                        var nearestEdge: Pair<Boolean, Pair<Int, Int>>? = null
                        var minDistance = Float.MAX_VALUE
                        val touchThreshold = stepX * 0.45f

                        // Check horizontal edges: r in 0..gridSize, c in 0 until gridSize
                        for (r in 0..gridSize) {
                            val edgeY = r * stepY
                            for (c in 0 until gridSize) {
                                val startX = c * stepX
                                val endX = (c + 1) * stepX
                                if (touchX in (startX - 10)..(endX + 10)) {
                                    val dist = abs(touchY - edgeY)
                                    if (dist < touchThreshold && dist < minDistance) {
                                        minDistance = dist
                                        nearestEdge = true to (r to c)
                                    }
                                }
                            }
                        }

                        // Check vertical edges: r in 0 until gridSize, c in 0..gridSize
                        for (c in 0..gridSize) {
                            val edgeX = c * stepX
                            for (r in 0 until gridSize) {
                                val startY = r * stepY
                                val endY = (r + 1) * stepY
                                if (touchY in (startY - 10)..(endY + 10)) {
                                    val dist = abs(touchX - edgeX)
                                    if (dist < touchThreshold && dist < minDistance) {
                                        minDistance = dist
                                        nearestEdge = false to (r to c)
                                    }
                                }
                            }
                        }

                        nearestEdge?.let { (isH, coord) ->
                            onEdgeTapped(isH, coord.first, coord.second)
                        }
                    }
                }
        ) {
            // 1. Draw completed boxes backgrounds
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val owner = boxes[r to c]
                    if (owner != null) {
                        val boxLeft = margin + c * stepX + pad4dp
                        val boxTop = margin + r * stepY + pad4dp
                        val boxW = stepX - pad8dp
                        val boxH = stepY - pad8dp

                        val boxColor = if (owner == DABPlayer.PLAYER_1) Player1Color else Player2Color
                        drawRoundRect(
                            color = boxColor.copy(alpha = 0.28f),
                            topLeft = Offset(boxLeft, boxTop),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(cornerRadius12dp)
                        )
                        drawRoundRect(
                            color = boxColor.copy(alpha = 0.6f),
                            topLeft = Offset(boxLeft, boxTop),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(cornerRadius12dp),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth2dp)
                        )

                        // Draw owner initial centered in box
                        val textPaint = Paint().apply {
                            color = boxColor.toArgb()
                            textAlign = Paint.Align.CENTER
                            textSize = if (gridSize <= 3) 54f else 38f
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                        val cx = margin + (c + 0.5f) * stepX
                        val cy = margin + (r + 0.5f) * stepY - ((textPaint.descent() + textPaint.ascent()) / 2)
                        drawContext.canvas.nativeCanvas.drawText(owner.tag, cx, cy, textPaint)
                    }
                }
            }

            // 2. Draw all potential and selected edges
            // Horizontal lines
            for (r in 0..gridSize) {
                val y = margin + r * stepY
                for (c in 0 until gridSize) {
                    val x1 = margin + c * stepX
                    val x2 = margin + (c + 1) * stepX
                    val isTaken = horizontalEdges.contains(r to c)

                    if (isTaken) {
                        drawLine(
                            color = Color(0xFF6366F1),
                            start = Offset(x1, y),
                            end = Offset(x2, y),
                            strokeWidth = edgeThickness,
                            cap = StrokeCap.Round
                        )
                    } else {
                        // Faint preview guide line
                        drawLine(
                            color = SlateBorder.copy(alpha = 0.4f),
                            start = Offset(x1 + dotRadius, y),
                            end = Offset(x2 - dotRadius, y),
                            strokeWidth = strokeWidth2dp,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Vertical lines
            for (c in 0..gridSize) {
                val x = margin + c * stepX
                for (r in 0 until gridSize) {
                    val y1 = margin + r * stepY
                    val y2 = margin + (r + 1) * stepY
                    val isTaken = verticalEdges.contains(r to c)

                    if (isTaken) {
                        drawLine(
                            color = Color(0xFF6366F1),
                            start = Offset(x, y1),
                            end = Offset(x, y2),
                            strokeWidth = edgeThickness,
                            cap = StrokeCap.Round
                        )
                    } else {
                        drawLine(
                            color = SlateBorder.copy(alpha = 0.4f),
                            start = Offset(x, y1 + dotRadius),
                            end = Offset(x, y2 - dotRadius),
                            strokeWidth = strokeWidth2dp,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 3. Draw grid pin dots
            for (r in 0..gridSize) {
                val y = margin + r * stepY
                for (c in 0..gridSize) {
                    val x = margin + c * stepX
                    // Outer glow
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = dotRadius * 1.5f,
                        center = Offset(x, y)
                    )
                    // Core dot
                    drawCircle(
                        color = Color.White,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
