package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiNexusBrain
import com.example.ai.VoiceTtsManager
import com.example.data.AuthManager
import com.example.data.FirestoreRepository
import com.example.model.ScreenTab
import com.example.ui.components.DynamicNeonGlowBorder
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NexusBlack
import com.example.ui.theme.NexusCardBg
import com.example.ui.theme.NexusCardBorder
import com.example.ui.theme.NexusDarkSurface
import com.example.ui.theme.NexusTextPrimary
import com.example.ui.theme.NexusTextSecondary

@Composable
fun NexusMainScreen(
    currentUserId: String,
    authManager: AuthManager,
    repository: FirestoreRepository,
    brain: GeminiNexusBrain,
    voiceManager: VoiceTtsManager,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf(ScreenTab.CHAT) }
    var isThinkingMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NexusBlack),
        containerColor = NexusBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexusBlack)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo & Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DynamicNeonGlowBorder(
                            modifier = Modifier.size(38.dp),
                            cornerRadius = 19.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(NexusCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Nexus Max",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "NEXUS MAX",
                                    color = NexusTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
                                )
                            }
                            Text(
                                text = "বাংলা S2S • ডিভাইস ব্রেন",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Deep Thinking Mode Switch (Gemini 4 Argon)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isThinkingMode) "THINKING (ARGON)" else "FLASH 3.8",
                            color = if (isThinkingMode) NeonPink else NexusTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isThinkingMode,
                            onCheckedChange = { isThinkingMode = it },
                            modifier = Modifier
                                .testTag("thinking_mode_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonPink,
                                checkedTrackColor = NeonPink.copy(alpha = 0.3f),
                                uncheckedThumbColor = NexusTextSecondary,
                                uncheckedTrackColor = NexusDarkSurface
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = NexusDarkSurface,
                contentColor = NexusTextPrimary
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.CHAT,
                    onClick = { currentTab = ScreenTab.CHAT },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("চ্যাট ও ভয়েস", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_chat_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.2f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.VISION,
                    onClick = { currentTab = ScreenTab.VISION },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = "Vision") },
                    label = { Text("ভিশন আই", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_vision_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPurple,
                        selectedTextColor = NeonPurple,
                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.IMAGEN,
                    onClick = { currentTab = ScreenTab.IMAGEN },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Imagen") },
                    label = { Text("ইমেজিন ৩", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_imagen_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPink,
                        selectedTextColor = NeonPink,
                        indicatorColor = NeonPink.copy(alpha = 0.2f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.AUTONOMOUS,
                    onClick = { currentTab = ScreenTab.AUTONOMOUS },
                    icon = { Icon(Icons.Default.ElectricBolt, contentDescription = "Autonomous") },
                    label = { Text("অটোমেশন", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_autonomous_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonGreen,
                        selectedTextColor = NeonGreen,
                        indicatorColor = NeonGreen.copy(alpha = 0.2f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.HISTORY,
                    onClick = { currentTab = ScreenTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("ক্লাউড", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_history_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonBlue,
                        selectedTextColor = NeonBlue,
                        indicatorColor = NeonBlue.copy(alpha = 0.2f),
                        unselectedIconColor = NexusTextSecondary,
                        unselectedTextColor = NexusTextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.CHAT -> ChatVoiceTab(
                    currentUserId = currentUserId,
                    repository = repository,
                    brain = brain,
                    voiceManager = voiceManager,
                    isThinkingMode = isThinkingMode
                )
                ScreenTab.VISION -> VisionTab(
                    brain = brain,
                    voiceManager = voiceManager
                )
                ScreenTab.IMAGEN -> ImagenTab(
                    currentUserId = currentUserId,
                    repository = repository
                )
                ScreenTab.AUTONOMOUS -> AutonomousTab(
                    currentUserId = currentUserId,
                    repository = repository,
                    brain = brain,
                    voiceManager = voiceManager
                )
                ScreenTab.HISTORY -> HistoryTab(
                    currentUserId = currentUserId,
                    authManager = authManager,
                    repository = repository,
                    onSignOut = onSignOut
                )
            }
        }
    }
}
