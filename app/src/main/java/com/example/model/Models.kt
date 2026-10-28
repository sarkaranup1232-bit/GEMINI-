package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val lastMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val userId: String = "",
    val role: String = "user", // "user", "model", "system"
    val content: String = "",
    val modelUsed: String = "gemini-3.8-flash",
    val hasAudio: Boolean = false,
    val isSearchGrounding: Boolean = false,
    val isImagePrompt: Boolean = false,
    val imageBase64: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GeneratedMedia(
    @DocumentId val id: String = "",
    val userId: String = "",
    val type: String = "image", // "image", "music", "video", "code"
    val prompt: String = "",
    val aspectRatio: String = "1:1",
    val resolution: String = "1K",
    val mediaUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AutonomousTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actionType: String = "call", // "call", "sms", "whatsapp", "maps", "sandbox", "accessibility"
    val title: String = "",
    val status: String = "completed", // "completed", "executing", "queued", "failed"
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class ScreenTab {
    CHAT,
    VISION,
    IMAGEN,
    AUTONOMOUS,
    HISTORY
}

data class PythonExecutionResult(
    val code: String,
    val output: String,
    val chartData: List<Pair<String, Float>> = emptyList(),
    val executionTimeMs: Long = 120
)
