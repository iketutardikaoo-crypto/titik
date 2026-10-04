package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameMode
import com.example.model.PowerUpType
import com.example.ui.components.DotsBoardCanvas
import com.example.ui.theme.DotCoral
import com.example.ui.theme.DotCyan
import com.example.ui.theme.DotGold
import com.example.ui.theme.DotMint
import com.example.ui.theme.DotPurple
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.viewmodel.DotsConnectUiState
import com.example.viewmodel.DotsConnectViewModel

@Composable
fun DotsConnectScreen(
    viewModel: DotsConnectViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        if (state.isPlaying && !state.isGameOver) {
            viewModel.togglePause()
        } else {
            onNavigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Back, Mode Badge, Pause Button
            TopGameHeader(
                gameMode = state.gameMode,
                onBack = {
                    if (state.isPlaying && !state.isGameOver) {
                        viewModel.togglePause()
                    } else {
                        onNavigateBack()
                    }
                },
                onPause = { viewModel.togglePause() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Stats Dashboard: Score & Objective (Timer or Moves)
            StatsDashboard(state = state)

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Loop indicator banner (when closed loop / square is formed!)
            AnimatedVisibility(
                visible = state.isLoopFormed,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                LoopFormedBanner()
            }

            // Eraser Active Indicator
            if (state.activePowerUp == PowerUpType.ERASER) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DotCoral.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mode Hapus: Ketuk satu titik untuk menghapusnya",
                        color = DotCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Main Board Canvas
            DotsBoardCanvas(
                board = state.board,
                currentChain = state.currentChain,
                isLoopFormed = state.isLoopFormed,
                onDotTouchStart = { r, c -> viewModel.onDotTouchStart(r, c) },
                onDotTouchDrag = { r, c -> viewModel.onDotTouchDrag(r, c) },
                onDotTouchEnd = { viewModel.onDotTouchEnd() },
                onDotTapped = { r, c ->
                    if (state.activePowerUp == PowerUpType.ERASER) {
                        viewModel.onDotTouchStart(r, c)
                    }
                },
                modifier = Modifier.weight(8f, fill = false)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 5. Power-ups Tool Bar
            PowerUpsBar(
                state = state,
                onActivatePowerUp = { viewModel.activatePowerUp(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Pause Dialog
        if (state.isPaused && !state.isGameOver) {
            PauseModal(
                onResume = { viewModel.togglePause() },
                onRestart = { viewModel.startNewGame(state.gameMode) },
                onExit = onNavigateBack
            )
        }

        // Game Over Dialog
        if (state.isGameOver) {
            GameOverModal(
                state = state,
                onRestart = { viewModel.startNewGame(state.gameMode) },
                onExit = onNavigateBack
            )
        }
    }
}

@Composable
private fun TopGameHeader(
    gameMode: GameMode,
    onBack: () -> Unit,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SlateCard)
                .testTag("game_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White
            )
        }

        // Game Mode Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCard)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = gameMode.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        IconButton(
            onClick = onPause,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SlateCard)
                .testTag("game_pause_button")
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Jeda",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun StatsDashboard(state: DotsConnectUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Score Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SKOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.score}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                if (state.bestScore > 0) {
                    Text(
                        text = "Terbaik: ${state.bestScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DotGold
                    )
                }
            }
        }

        // Objective Card (Time or Moves)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state.gameMode) {
                    GameMode.TIMED -> {
                        Text(
                            text = "SISA WAKTU",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.timeLeftSec}s",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (state.timeLeftSec <= 10) DotCoral else DotCyan
                        )
                        LinearProgressIndicator(
                            progress = { (state.timeLeftSec / 60f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (state.timeLeftSec <= 10) DotCoral else DotCyan,
                            trackColor = SlateSurface
                        )
                    }
                    GameMode.MOVES -> {
                        Text(
                            text = "SISA LANGKAH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.movesLeft}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (state.movesLeft <= 5) DotCoral else DotGold
                        )
                        Text(
                            text = "dari 30 langkah",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    GameMode.ZEN -> {
                        Text(
                            text = "TITIK DIHAPUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.dotsCleared}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = DotMint
                        )
                        Text(
                            text = "Mode Santai",
                            style = MaterialTheme.typography.labelSmall,
                            color = DotMint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoopFormedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DotGold)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Loop,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KOTAK TERTUTUP! Lepaskan untuk hapus semua!",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PowerUpsBar(
    state: DotsConnectUiState,
    onActivatePowerUp: (PowerUpType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SlateCard)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Eraser Power-up
        PowerUpButton(
            icon = Icons.Default.CleaningServices,
            label = "Hapus",
            count = state.erasersLeft,
            isActive = state.activePowerUp == PowerUpType.ERASER,
            accentColor = DotCoral,
            testTag = "powerup_eraser_button",
            onClick = { onActivatePowerUp(PowerUpType.ERASER) }
        )

        // Color Bomb Power-up
        PowerUpButton(
            icon = Icons.Default.Flare,
            label = "Bom Warna",
            count = state.colorBombsLeft,
            isActive = false,
            accentColor = DotGold,
            testTag = "powerup_bomb_button",
            onClick = { onActivatePowerUp(PowerUpType.COLOR_BOMB) }
        )

        // Shuffle Power-up
        PowerUpButton(
            icon = Icons.Default.Shuffle,
            label = "Acak",
            count = state.shufflesLeft,
            isActive = false,
            accentColor = DotMint,
            testTag = "powerup_shuffle_button",
            onClick = { onActivatePowerUp(PowerUpType.SHUFFLE) }
        )
    }
}

@Composable
private fun PowerUpButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    isActive: Boolean,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val isEnabled = count > 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) accentColor else if (isEnabled) SlateSurface else SlateSurface.copy(alpha = 0.4f)
                )
                .border(
                    width = if (isActive) 2.dp else 0.dp,
                    color = Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else if (isEnabled) accentColor else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            // Count badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$count",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isEnabled) Color(0xFFCBD5E1) else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PauseModal(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCard,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permainan Dijeda",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_game_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lanjutkan", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restart_from_pause_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Ulang", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("exit_to_menu_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Kembali ke Menu", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun GameOverModal(
    state: DotsConnectUiState,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onExit) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCard,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(12.dp)
                .testTag("game_over_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy or Congrats Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DotGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = DotGold,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Waktu Habis / Selesai!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (state.score >= state.bestScore && state.score > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DotGold.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🎉 REKOR SKOR BARU! 🎉",
                            color = DotGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Score Display
                Text(
                    text = "${state.score}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Poin Akhir",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Details grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.dotsCleared}", fontWeight = FontWeight.Bold, color = DotCyan, fontSize = 20.sp)
                        Text(text = "Titik", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.squaresFormed}", fontWeight = FontWeight.Bold, color = DotGold, fontSize = 20.sp)
                        Text(text = "Kotak/Loop", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.maxChainLength}", fontWeight = FontWeight.Bold, color = DotMint, fontSize = 20.sp)
                        Text(text = "Rantai Maks", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("play_again_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Main Lagi", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("return_home_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Kembali ke Menu", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
