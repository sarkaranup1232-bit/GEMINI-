package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiNexusBrain
import com.example.ai.VoiceTtsManager
import com.example.data.FirestoreRepository
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.DynamicNeonGlowBorder
import com.example.ui.components.ThreeDotWavingLoader
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NexusCardBg
import com.example.ui.theme.NexusCardBorder
import com.example.ui.theme.NexusDarkSurface
import com.example.ui.theme.NexusTextMuted
import com.example.ui.theme.NexusTextPrimary
import com.example.ui.theme.NexusTextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatVoiceTab(
    currentUserId: String,
    repository: FirestoreRepository,
    brain: GeminiNexusBrain,
    voiceManager: VoiceTtsManager,
    isThinkingMode: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var activeSessionId by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var currentStreamingResponse by remember { mutableStateOf("") }

    val messages = remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val audioFrequency by voiceManager.audioFrequencyLevel.collectAsState()

    // Speech-to-Text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputText = spoken
            }
        }
    }

    // Initialize or load conversation session
    LaunchedEffect(currentUserId) {
        val newSession = ChatSession(
            userId = currentUserId,
            title = "Gemini Nexus对话",
            lastMessage = "ব্রেন প্রস্তুত রয়েছে"
        )
        val id = repository.saveChatSession(newSession)
        activeSessionId = id

        // Welcome message in Bengali
        val welcomeMsg = ChatMessage(
            chatId = id,
            userId = currentUserId,
            role = "model",
            content = "আমি Gemini Nexus Max। আপনার সার্বক্ষণিক ডিভাইস ব্রেন। বাংলা ভাষায় যেকোনো নির্দেশ দিন—ভিশন, ম্যাপস, লাইভ সার্চ কিংবা সিস্টেম কাজ করতে আমি সর্বদা প্রস্তুত।",
            modelUsed = "gemini-3.8-flash"
        )
        messages.value = listOf(welcomeMsg)
        repository.saveMessage(id, welcomeMsg)
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.value.size, currentStreamingResponse) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Active Status & Quick Prompts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isStreaming || isSpeaking) NeonCyan else NeonPurple)
                )
                Text(
                    text = if (isSpeaking) "ভয়েস স্পিচ চলছে..." else if (isStreaming) "চিন্তা করছে..." else "ব্রেন প্রস্তুত • বাংলা S2S",
                    color = NexusTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AudioWaveformVisualizer(
                frequencyLevel = audioFrequency,
                isSpeaking = isSpeaking
            )
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip(label = "🔍 আজকের তাজা খবর") {
                inputText = "আজকের প্রধান ও সর্বশেষ গুরুত্বপূর্ণ খবরগুলো কী কী?"
            }
            SuggestionChip(label = "🗺️ ঢাকার লাইভ ট্রাফিক") {
                inputText = "ঢাকার গুলশান থেকে মতিঝিল যাওয়ার ট্রাফিক ও সেরা রুট কোনটি?"
            }
            SuggestionChip(label = "🧬 কোয়ান্টাম ও বায়োসায়েন্স") {
                inputText = "বায়োসায়েন্সে CRISPR জিন এডিটিং প্রযুক্তি কীভাবে কাজ করে?"
            }
            SuggestionChip(label = "⚡ সিস্টেম ওভারভিউ") {
                inputText = "আমার ডিভাইসের ব্যাটারি ও মেমোরি স্ট্যাটাস চেক করো"
            }
        }

        // Chat Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages.value) { message ->
                MessageBubble(
                    message = message,
                    onPlayTts = { text ->
                        voiceManager.speak(text)
                    }
                )
            }

            // Real-time streaming response bubble
            if (isStreaming && currentStreamingResponse.isNotEmpty()) {
                item {
                    StreamingBubble(
                        content = currentStreamingResponse,
                        audioFrequency = audioFrequency
                    )
                }
            }

            // Searching / Researching Loader
            if (isSearching) {
                item {
                    ThreeDotWavingLoader(
                        statusText = "গুগল সার্চ ও লাইভ ডেটা অনুসন্ধান চলছে..."
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Message Input Box with Dynamic Neon Glow Border (Exact 24dp corners)
        DynamicNeonGlowBorder(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            cornerRadius = 24.dp,
            borderWidth = 2.dp,
            frequencyLevel = audioFrequency,
            isProcessing = isStreaming || isSpeaking
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexusCardBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Nexus Max শুনছে... বাংলায় বলুন")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            inputText = "আমার সাথে বাংলায় কথা বলুন"
                        }
                    },
                    modifier = Modifier.testTag("mic_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isSpeaking) NeonPink else NeonCyan
                    )
                }

                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "বাংলায় বলুন বা লিখুন...",
                            color = NexusTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = NexusTextPrimary,
                        unfocusedTextColor = NexusTextPrimary
                    ),
                    maxLines = 3
                )

                // Search Grounding Quick Action Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isStreaming) {
                            val query = inputText.trim()
                            inputText = ""
                            isSearching = true
                            scope.launch {
                                val userMsg = ChatMessage(
                                    chatId = activeSessionId ?: "",
                                    userId = currentUserId,
                                    role = "user",
                                    content = query,
                                    isSearchGrounding = true
                                )
                                messages.value = messages.value + userMsg
                                val groundedResult = brain.executeSearchGrounding(query)
                                isSearching = false

                                val aiMsg = ChatMessage(
                                    chatId = activeSessionId ?: "",
                                    userId = currentUserId,
                                    role = "model",
                                    content = groundedResult,
                                    isSearchGrounding = true
                                )
                                messages.value = messages.value + aiMsg
                                activeSessionId?.let { sId ->
                                    repository.saveMessage(sId, userMsg)
                                    repository.saveMessage(sId, aiMsg)
                                }
                                voiceManager.speak(groundedResult)
                            }
                        }
                    },
                    modifier = Modifier.testTag("search_grounding_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Grounding",
                        tint = NeonPurple
                    )
                }

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isStreaming) {
                            val userText = inputText.trim()
                            inputText = ""
                            isStreaming = true
                            currentStreamingResponse = ""

                            val userMsg = ChatMessage(
                                chatId = activeSessionId ?: "",
                                userId = currentUserId,
                                role = "user",
                                content = userText
                            )
                            messages.value = messages.value + userMsg

                            scope.launch {
                                activeSessionId?.let { sId ->
                                    repository.saveMessage(sId, userMsg)
                                }

                                val streamBuilder = StringBuilder()
                                brain.generateStreamingResponse(
                                    prompt = userText,
                                    conversationHistory = messages.value,
                                    isDeepThinking = isThinkingMode
                                ).collect { chunk ->
                                    streamBuilder.append(chunk)
                                    currentStreamingResponse = streamBuilder.toString()
                                }

                                isStreaming = false
                                val fullReply = streamBuilder.toString()
                                if (fullReply.isNotBlank()) {
                                    val modelMsg = ChatMessage(
                                        chatId = activeSessionId ?: "",
                                        userId = currentUserId,
                                        role = "model",
                                        content = fullReply,
                                        modelUsed = if (isThinkingMode) "gemini-4-argon" else "gemini-3.8-flash"
                                    )
                                    messages.value = messages.value + modelMsg
                                    activeSessionId?.let { sId ->
                                        repository.saveMessage(sId, modelMsg)
                                    }
                                    voiceManager.speak(fullReply)
                                }
                                currentStreamingResponse = ""
                            }
                        }
                    },
                    modifier = Modifier.testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) NeonCyan else NexusTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onPlayTts: (String) -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(NeonPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isUser) NeonBlue.copy(alpha = 0.25f) else NexusCardBg)
                .border(
                    width = 1.dp,
                    color = if (isUser) NeonBlue.copy(alpha = 0.5f) else NexusCardBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(if (isUser) 0.85f else 0.9f)
        ) {
            if (!isUser) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NEXUS MAX",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (message.isSearchGrounding) {
                            Text(
                                text = "• SEARCH GROUNDED",
                                color = NeonPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // TTS Speaker Button
                    IconButton(
                        onClick = { onPlayTts(message.content) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = message.content,
                color = NexusTextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun StreamingBubble(
    content: String,
    audioFrequency: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NeonPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = NeonPink,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        DynamicNeonGlowBorder(
            modifier = Modifier.fillMaxWidth(0.9f),
            cornerRadius = 20.dp,
            borderWidth = 1.5.dp,
            frequencyLevel = audioFrequency,
            isProcessing = true
        ) {
            Column(
                modifier = Modifier
                    .background(NexusCardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "NEXUS MAX • GENERATING...",
                    color = NeonPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = content,
                    color = NexusTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NexusDarkSurface)
            .border(1.dp, NexusCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = NexusTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
