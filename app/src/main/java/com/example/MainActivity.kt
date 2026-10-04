package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameMode
import com.example.model.OpponentMode
import com.example.ui.screens.AssistantChatScreen
import com.example.ui.screens.AssistantSettingsDialog
import com.example.ui.screens.AssistantTasksScreen
import com.example.ui.screens.AssistantToolsScreen
import com.example.ui.screens.DotsAndBoxesScreen
import com.example.ui.screens.DotsConnectScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.DotCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark
import com.example.viewmodel.AssistantTab
import com.example.viewmodel.AssistantViewModel
import com.example.viewmodel.DotsAndBoxesViewModel
import com.example.viewmodel.DotsConnectViewModel
import com.example.viewmodel.StatsViewModel

enum class SecondaryScreen {
    NONE,
    DOTS_CONNECT,
    DOTS_AND_BOXES,
    STATS
}

class MainActivity : ComponentActivity() {

    private val assistantViewModel: AssistantViewModel by viewModels()
    private val dotsConnectViewModel: DotsConnectViewModel by viewModels()
    private val dotsAndBoxesViewModel: DotsAndBoxesViewModel by viewModels()
    private val statsViewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val assistantState by assistantViewModel.uiState.collectAsStateWithLifecycle()
                var secondaryScreen by remember { mutableStateOf(SecondaryScreen.NONE) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                var soundEnabled by remember { mutableStateOf(true) }
                var hapticsEnabled by remember { mutableStateOf(true) }

                if (showSettingsDialog) {
                    AssistantSettingsDialog(
                        currentApiKey = assistantState.customApiKey,
                        onSaveApiKey = { key -> assistantViewModel.setCustomApiKey(key) },
                        onDismiss = { showSettingsDialog = false }
                    )
                }

                // If currently playing sub-game
                when (secondaryScreen) {
                    SecondaryScreen.DOTS_CONNECT -> {
                        DotsConnectScreen(
                            viewModel = dotsConnectViewModel,
                            onNavigateBack = { secondaryScreen = SecondaryScreen.NONE }
                        )
                    }

                    SecondaryScreen.DOTS_AND_BOXES -> {
                        DotsAndBoxesScreen(
                            viewModel = dotsAndBoxesViewModel,
                            onNavigateBack = { secondaryScreen = SecondaryScreen.NONE }
                        )
                    }

                    SecondaryScreen.STATS -> {
                        StatsScreen(
                            viewModel = statsViewModel,
                            onNavigateBack = { secondaryScreen = SecondaryScreen.NONE }
                        )
                    }

                    SecondaryScreen.NONE -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                NavigationBar(
                                    containerColor = SlateCard,
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .testTag("main_bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = assistantState.currentTab == AssistantTab.CHAT,
                                        onClick = { assistantViewModel.selectTab(AssistantTab.CHAT) },
                                        icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Asisten") },
                                        label = { Text("Asisten", fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = Color.White,
                                            indicatorColor = Color(0xFF6366F1),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = assistantState.currentTab == AssistantTab.TASKS,
                                        onClick = { assistantViewModel.selectTab(AssistantTab.TASKS) },
                                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Tugas") },
                                        label = { Text("Tugas", fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = Color.White,
                                            indicatorColor = Color(0xFF6366F1),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = assistantState.currentTab == AssistantTab.TOOLS,
                                        onClick = { assistantViewModel.selectTab(AssistantTab.TOOLS) },
                                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Alat") },
                                        label = { Text("Alat AI", fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = Color.White,
                                            indicatorColor = Color(0xFF6366F1),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = assistantState.currentTab == AssistantTab.GAMES,
                                        onClick = { assistantViewModel.selectTab(AssistantTab.GAMES) },
                                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Game") },
                                        label = { Text("Hiburan", fontSize = 11.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            selectedTextColor = Color.White,
                                            indicatorColor = Color(0xFF6366F1),
                                            unselectedIconColor = Color(0xFF94A3B8),
                                            unselectedTextColor = Color(0xFF94A3B8)
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SlateDark)
                                    .padding(innerPadding)
                            ) {
                                when (assistantState.currentTab) {
                                    AssistantTab.CHAT -> {
                                        AssistantChatScreen(
                                            viewModel = assistantViewModel,
                                            onOpenSettings = { showSettingsDialog = true }
                                        )
                                    }

                                    AssistantTab.TASKS -> {
                                        AssistantTasksScreen(
                                            viewModel = assistantViewModel
                                        )
                                    }

                                    AssistantTab.TOOLS -> {
                                        AssistantToolsScreen(
                                            viewModel = assistantViewModel
                                        )
                                    }

                                    AssistantTab.GAMES -> {
                                        MainMenuScreen(
                                            onPlayDotsConnect = { mode ->
                                                dotsConnectViewModel.startNewGame(mode)
                                                secondaryScreen = SecondaryScreen.DOTS_CONNECT
                                            },
                                            onPlayDotsAndBoxes = { mode ->
                                                dotsAndBoxesViewModel.startNewGame(mode = mode)
                                                secondaryScreen = SecondaryScreen.DOTS_AND_BOXES
                                            },
                                            onOpenStats = {
                                                secondaryScreen = SecondaryScreen.STATS
                                            },
                                            soundEnabled = soundEnabled,
                                            onToggleSound = {
                                                soundEnabled = !soundEnabled
                                                dotsConnectViewModel.soundManager.soundEnabled = soundEnabled
                                                dotsAndBoxesViewModel.soundManager.soundEnabled = soundEnabled
                                            },
                                            hapticsEnabled = hapticsEnabled,
                                            onToggleHaptics = {
                                                hapticsEnabled = !hapticsEnabled
                                                dotsConnectViewModel.soundManager.hapticsEnabled = hapticsEnabled
                                                dotsAndBoxesViewModel.soundManager.hapticsEnabled = hapticsEnabled
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
