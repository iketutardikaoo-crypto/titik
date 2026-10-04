package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.DotColor
import com.example.model.DotCoordinate
import com.example.model.DotItem
import kotlin.math.sqrt

@Composable
fun DotsBoardCanvas(
    board: List<List<DotItem>>,
    currentChain: List<DotCoordinate>,
    isLoopFormed: Boolean,
    onDotTouchStart: (Int, Int) -> Unit,
    onDotTouchDrag: (Int, Int) -> Unit,
    onDotTouchEnd: () -> Unit,
    onDotTapped: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = board.size
    val cols = if (board.isNotEmpty()) board[0].size else 0

    var currentPointerOffset by remember { mutableStateOf<Offset?>(null) }

    // Pulsing aura animation when loop is formed
    val infiniteTransition = rememberInfiniteTransition(label = "loopPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(12.dp)
            .testTag("dots_connect_board")
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val cellWidth = if (cols > 0) width / cols else 0f
        val cellHeight = if (rows > 0) height / rows else 0f
        val baseDotRadius = (minOf(cellWidth, cellHeight) * 0.32f)

        // Helper function to map pointer (x, y) to dot grid coordinate
        fun getCoordAt(offset: Offset): Pair<Int, Int>? {
            val c = (offset.x / cellWidth).toInt()
            val r = (offset.y / cellHeight).toInt()
            if (r in 0 until rows && c in 0 until cols) {
                val centerX = (c + 0.5f) * cellWidth
                val centerY = (r + 0.5f) * cellHeight
                val dx = offset.x - centerX
                val dy = offset.y - centerY
                val dist = sqrt(dx * dx + dy * dy)
                // Lenient touch radius (80% of cell) so players can drag smoothly
                if (dist <= cellWidth * 0.48f) {
                    return r to c
                }
            }
            return null
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(rows, cols) {
                    detectTapGestures { offset ->
                        getCoordAt(offset)?.let { (r, c) ->
                            onDotTapped(r, c)
                        }
                    }
                }
                .pointerInput(rows, cols) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPointerOffset = offset
                            getCoordAt(offset)?.let { (r, c) ->
                                onDotTouchStart(r, c)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPointerOffset = change.position
                            getCoordAt(change.position)?.let { (r, c) ->
                                onDotTouchDrag(r, c)
                            }
                        },
                        onDragEnd = {
                            currentPointerOffset = null
                            onDotTouchEnd()
                        },
                        onDragCancel = {
                            currentPointerOffset = null
                            onDotTouchEnd()
                        }
                    )
                }
        ) {
            if (rows == 0 || cols == 0) return@Canvas

            // 1. Draw subtle background dot grid markers
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val cx = (c + 0.5f) * cellWidth
                    val cy = (r + 0.5f) * cellHeight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 3.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // 2. Draw connecting lines
            if (currentChain.isNotEmpty()) {
                val chainColor = board[currentChain.first().row][currentChain.first().col].color.composeColor
                val strokePx = 10.dp.toPx()

                // Draw existing chain connections
                val path = Path()
                currentChain.forEachIndexed { index, coord ->
                    val cx = (coord.col + 0.5f) * cellWidth
                    val cy = (coord.row + 0.5f) * cellHeight
                    if (index == 0) {
                        path.moveTo(cx, cy)
                    } else {
                        path.lineTo(cx, cy)
                    }
                }

                // Outer soft glow for the path
                drawPath(
                    path = path,
                    color = chainColor.copy(alpha = 0.35f),
                    style = Stroke(
                        width = strokePx * 1.8f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Main vibrant connection line
                drawPath(
                    path = path,
                    color = chainColor,
                    style = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw trailing line to current finger position if dragging
                currentPointerOffset?.let { pointer ->
                    if (!isLoopFormed) {
                        val lastCoord = currentChain.last()
                        val lastCx = (lastCoord.col + 0.5f) * cellWidth
                        val lastCy = (lastCoord.row + 0.5f) * cellHeight

                        drawLine(
                            color = chainColor.copy(alpha = 0.7f),
                            start = Offset(lastCx, lastCy),
                            end = pointer,
                            strokeWidth = strokePx * 0.85f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 3. Draw dots
            val chainSet = currentChain.toSet()
            val loopColor = if (currentChain.isNotEmpty() && isLoopFormed) {
                board[currentChain.first().row][currentChain.first().col].color
            } else null

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val dot = board[r][c]
                    val cx = (c + 0.5f) * cellWidth
                    val cy = (r + 0.5f) * cellHeight
                    val isChained = chainSet.contains(DotCoordinate(r, c))
                    val isAllColorTarget = loopColor != null && dot.color == loopColor

                    var radius = baseDotRadius
                    var alpha = 1.0f

                    if (dot.isPopping) {
                        radius *= 0.2f
                        alpha = 0f
                    } else if (isChained) {
                        radius *= 1.25f // Pop out when chained
                    }

                    // Pulsing halo aura if loop is formed on all matching dots
                    if (isAllColorTarget && !dot.isPopping) {
                        drawCircle(
                            color = dot.color.composeColor.copy(alpha = pulseAlpha),
                            radius = baseDotRadius * pulseScale,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = pulseAlpha * 0.7f),
                            radius = baseDotRadius * (pulseScale * 0.8f),
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Shadow / glow behind dot
                    drawCircle(
                        color = dot.color.composeColor.copy(alpha = 0.35f * alpha),
                        radius = radius * 1.2f,
                        center = Offset(cx, cy + 2.dp.toPx())
                    )

                    // Main Dot
                    drawCircle(
                        color = dot.color.composeColor.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(cx, cy)
                    )

                    // Inner soft top-left highlight for clean glossy look
                    if (alpha > 0.5f) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.32f),
                            radius = radius * 0.38f,
                            center = Offset(cx - radius * 0.3f, cy - radius * 0.3f)
                        )
                    }

                    // Chained dot white outer ring
                    if (isChained && !dot.isPopping) {
                        drawCircle(
                            color = Color.White,
                            radius = radius + 2.5.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
