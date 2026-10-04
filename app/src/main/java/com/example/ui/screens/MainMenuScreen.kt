package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.OpponentMode
import com.example.ui.theme.DotCoral
import com.example.ui.theme.DotCyan
import com.example.ui.theme.DotGold
import com.example.ui.theme.DotMint
import com.example.ui.theme.DotPurple
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface

@Composable
fun MainMenuScreen(
    onPlayDotsConnect: (GameMode) -> Unit,
    onPlayDotsAndBoxes: (OpponentMode) -> Unit,
    onOpenStats: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    hapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHowToPlay by remember { mutableStateOf(false) }

    // Floating dot animation in background
    val infiniteTransition = rememberInfiniteTransition(label = "bgFloating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingAnim"
    )

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        // Decorative background floating dots
        Canvas(modifier = Modifier.fillMaxSize()) {
            val colors = listOf(DotCoral, DotCyan, DotGold, DotMint, DotPurple)
            val positions = listOf(
                Offset(size.width * 0.15f, size.height * 0.12f + floatOffset),
                Offset(size.width * 0.85f, size.height * 0.18f - floatOffset),
                Offset(size.width * 0.2f, size.height * 0.82f - floatOffset),
                Offset(size.width * 0.82f, size.height * 0.75f + floatOffset),
                Offset(size.width * 0.5f, size.height * 0.92f + floatOffset * 0.5f)
            )
            positions.forEachIndexed { i, pos ->
                drawCircle(
                    color = colors[i % colors.size].copy(alpha = 0.12f),
                    radius = 35.dp.toPx(),
                    center = pos
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header / App Title
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Dot Ring
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(DotCoral, DotCyan, DotGold, DotMint, DotPurple).forEach { dotColor ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "DOTS",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        color = Color.White
                    )

                    Text(
                        text = "Hubungkan Warna • Buat Kotak • Pecahkan Rekor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Section 1: Dots Connect Game
            item {
                Text(
                    text = "HUBUNGKAN TITIK (DOTS CONNECT)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DotCyan,
                    letterSpacing = 1.sp
                )
            }

            item {
                // Mode 1: Timed 60s
                ModePlayCard(
                    title = "60 Detik (Waktu)",
                    subtitle = "Tantangan kecepatan! Cetak skor terbanyak dalam 1 menit.",
                    tag = "Populer",
                    accentColor = DotCoral,
                    icon = Icons.Default.Speed,
                    testTag = "play_timed_mode_button",
                    onClick = { onPlayDotsConnect(GameMode.TIMED) }
                )
            }

            item {
                // Mode 2: Moves 30
                ModePlayCard(
                    title = "30 Langkah",
                    subtitle = "Strategi penuh ketelitian! Buat loop untuk skor maksimal.",
                    tag = "Strategi",
                    accentColor = DotGold,
                    icon = Icons.Default.Tune,
                    testTag = "play_moves_mode_button",
                    onClick = { onPlayDotsConnect(GameMode.MOVES) }
                )
            }

            item {
                // Mode 3: Zen
                ModePlayCard(
                    title = "Mode Zen Santai",
                    subtitle = "Tanpa timer & langkah. Nikmati alunan suara yang menenangkan.",
                    tag = "Relaks",
                    accentColor = DotMint,
                    icon = Icons.Default.SelfImprovement,
                    testTag = "play_zen_mode_button",
                    onClick = { onPlayDotsConnect(GameMode.ZEN) }
                )
            }

            // Section 2: Dots and Boxes
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "MINI-GAME: TITIK & KOTAK (DOTS & BOXES)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DotPurple,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Vs AI
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onPlayDotsAndBoxes(OpponentMode.VS_AI) }
                            .testTag("play_dab_vs_ai_button"),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DotPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = DotPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Lawan AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tanding vs Bot pintar",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // 2 Players Local
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onPlayDotsAndBoxes(OpponentMode.PASS_AND_PLAY) }
                            .testTag("play_dab_2_players_button"),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DotCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = DotCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "2 Pemain",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Main lokal bersama teman",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Quick Actions: Stats, How To Play, Audio Settings
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Trophy / Stats
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenStats() }
                            .testTag("open_stats_button"),
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = DotGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Skor & Rekor",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // How to Play
                    IconButton(
                        onClick = { showHowToPlay = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SlateCard)
                            .testTag("how_to_play_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Bantuan",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sound Toggle
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SlateCard)
                            .testTag("toggle_sound_button")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle Suara",
                            tint = if (soundEnabled) DotCyan else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Haptics Toggle
                    IconButton(
                        onClick = onToggleHaptics,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SlateCard)
                            .testTag("toggle_haptics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Toggle Getaran",
                            tint = if (hapticsEnabled) DotMint else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePlayCard(
    title: String,
    subtitle: String,
    tag: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Main",
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
