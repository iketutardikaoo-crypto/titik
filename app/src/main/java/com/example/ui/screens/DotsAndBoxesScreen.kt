package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AIDifficulty
import com.example.model.DABPlayer
import com.example.model.OpponentMode
import com.example.ui.components.DotsAndBoxesBoard
import com.example.ui.theme.DotGold
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateSurface
import com.example.viewmodel.DotsAndBoxesUiState
import com.example.viewmodel.DotsAndBoxesViewModel

@Composable
fun DotsAndBoxesScreen(
    viewModel: DotsAndBoxesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        onNavigateBack()
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SlateCard)
                        .testTag("dab_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SlateCard)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = state.opponentMode.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.startNewGame() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SlateCard)
                        .testTag("dab_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Mulai Ulang",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Scoreboard
            DABScoreboard(state = state)

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Turn Status Indicator
            TurnIndicator(state = state)

            Spacer(modifier = Modifier.weight(1f))

            // 4. Game Board
            DotsAndBoxesBoard(
                gridSize = state.gridSize,
                horizontalEdges = state.horizontalEdges,
                verticalEdges = state.verticalEdges,
                boxes = state.boxes,
                onEdgeTapped = { isH, r, c -> viewModel.onEdgeTapped(isH, r, c) },
                modifier = Modifier.weight(8f, fill = false)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 5. Controls: Grid size & AI difficulty
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SlateCard)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Grid Size toggles (3x3 vs 4x4)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ukuran:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(3 to "3x3", 4 to "4x4").forEach { (size, label) ->
                        val isSelected = state.gridSize == size
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF6366F1) else SlateSurface)
                                .clickable {
                                    if (!isSelected) {
                                        viewModel.startNewGame(gridSize = size)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                // AI Difficulty (only when vs AI)
                if (state.opponentMode == OpponentMode.VS_AI) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI:",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AIDifficulty.entries.forEach { diff ->
                            val isSelected = state.aiDifficulty == diff
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Player2Color else SlateSurface)
                                    .clickable {
                                        if (!isSelected) {
                                            viewModel.startNewGame(difficulty = diff)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = diff.title,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Winner / Game Over Modal
        if (state.isGameOver) {
            DABGameOverModal(
                state = state,
                onRestart = { viewModel.startNewGame() },
                onExit = onNavigateBack
            )
        }
    }
}

@Composable
private fun DABScoreboard(state: DotsAndBoxesUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Player 1 Card
        val isP1Turn = state.currentPlayer == DABPlayer.PLAYER_1
        Card(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = if (isP1Turn && !state.isGameOver) 2.dp else 0.dp,
                    color = if (isP1Turn && !state.isGameOver) Player1Color else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Player1Color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Player1Color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Pemain 1",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.player1Score} Kotak",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Player1Color
                    )
                }
            }
        }

        // Player 2 / AI Card
        val isP2Turn = state.currentPlayer == DABPlayer.PLAYER_2 || state.currentPlayer == DABPlayer.AI
        Card(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = if (isP2Turn && !state.isGameOver) 2.dp else 0.dp,
                    color = if (isP2Turn && !state.isGameOver) Player2Color else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Player2Color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.opponentMode == OpponentMode.VS_AI) Icons.Default.SmartToy else Icons.Default.Person,
                        contentDescription = null,
                        tint = Player2Color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (state.opponentMode == OpponentMode.VS_AI) "Komputer" else "Pemain 2",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${state.player2Score} Kotak",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Player2Color
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnIndicator(state: DotsAndBoxesUiState) {
    val activeColor = if (state.currentPlayer == DABPlayer.PLAYER_1) Player1Color else Player2Color

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(activeColor.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.isAiThinking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Player2Color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Komputer sedang menganalisis garis...",
                    color = Player2Color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            } else {
                val label = when (state.currentPlayer) {
                    DABPlayer.PLAYER_1 -> "Giliran Anda (P1)"
                    DABPlayer.PLAYER_2 -> "Giliran Pemain 2 (P2)"
                    DABPlayer.AI -> "Giliran Komputer..."
                }
                Text(
                    text = "▶ $label • Hubungkan 2 titik untuk membuat garis!",
                    color = activeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DABGameOverModal(
    state: DotsAndBoxesUiState,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onExit) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCard,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                val resultTitle = when {
                    state.isDraw -> "Hasil Imbang!"
                    state.winner == DABPlayer.PLAYER_1 -> "Selamat, Anda Menang!"
                    state.winner == DABPlayer.AI -> "Komputer Memenangkan Laga!"
                    else -> "Pemain 2 Menang!"
                }

                val titleColor = when {
                    state.isDraw -> Color.White
                    state.winner == DABPlayer.PLAYER_1 -> Player1Color
                    else -> Player2Color
                }

                Text(
                    text = resultTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.player1Score}", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Player1Color)
                        Text(text = "P1 Kotak", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                    Text(text = "-", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Color.White)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${state.player2Score}", fontWeight = FontWeight.Black, fontSize = 28.sp, color = Player2Color)
                        Text(
                            text = if (state.opponentMode == OpponentMode.VS_AI) "Bot Kotak" else "P2 Kotak",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dab_play_again_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tanding Ulang", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dab_return_home_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Kembali ke Menu", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
