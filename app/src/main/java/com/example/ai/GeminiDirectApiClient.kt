package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "GeminiDirectApi"

class GeminiDirectApiClient(
    private val apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Ultra-fast streaming response with SSE & Search Grounding
    fun generateStreamingResponse(
        prompt: String,
        conversationHistory: List<com.example.model.ChatMessage>,
        isThinkingMode: Boolean = false,
        enableSearchGrounding: Boolean = true
    ): Flow<String> = callbackFlow {
        val model = if (isThinkingMode) "gemini-4-argon" else "gemini-3.8-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"

        val contentsArray = JSONArray()
        conversationHistory.takeLast(6).forEach { msg ->
            val role = if (msg.role == "user") "user" else "model"
            contentsArray.put(
                JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                }
            )
        }
        contentsArray.put(
            JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }
        )

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put(
                "system_instruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            "You are Gemini Nexus Max (The Absolute Autonomous Brain). Tone: Natural, lightning-fast, conversational Bengali (Indian/Bangladeshi dialect). Eliminate all robotic preambles. Provide direct, empathetic, and ultra-high-speed accurate answers."
                        )
                    )
                )
            )
            if (enableSearchGrounding && !isThinkingMode) {
                put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
            }
            if (isThinkingMode) {
                put(
                    "generationConfig",
                    JSONObject().put(
                        "thinkingConfig",
                        JSONObject().put("thinkingLevel", "HIGH")
                    )
                )
            }
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Stream failed", e)
                trySend("\n[Nexus নেটওয়ার্ক ত্রুটি: ${e.message}]")
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string().orEmpty()
                    Log.e(TAG, "Stream error response HTTP ${response.code}: $errorBody")
                    trySend("\n[Nexus API ত্রুটি HTTP ${response.code}]")
                    close()
                    return
                }

                try {
                    val reader = BufferedReader(response.body?.charStream())
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: break
                        if (currentLine.startsWith("data: ")) {
                            val jsonString = currentLine.removePrefix("data: ").trim()
                            if (jsonString.isNotBlank() && jsonString != "[DONE]") {
                                try {
                                    val obj = JSONObject(jsonString)
                                    val candidates = obj.optJSONArray("candidates")
                                    if (candidates != null && candidates.length() > 0) {
                                        val candidate = candidates.getJSONObject(0)
                                        val content = candidate.optJSONObject("content")
                                        val parts = content?.optJSONArray("parts")
                                        if (parts != null) {
                                            for (i in 0 until parts.length()) {
                                                val part = parts.getJSONObject(i)
                                                val text = part.optString("text")
                                                if (text.isNotEmpty()) {
                                                    trySend(text)
                                                }
                                            }
                                        }
                                    }
                                } catch (parseErr: Exception) {
                                    Log.w(TAG, "Error parsing SSE chunk", parseErr)
                                }
                            }
                        }
                    }
                    close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading SSE stream", e)
                    close(e)
                }
            }
        })

        awaitClose {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    // Direct Image Analysis with Multimodal input
    suspend fun analyzeImage(
        bitmap: Bitmap,
        userQuery: String
    ): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val imageBytes = outputStream.toByteArray()
        val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val prompt = if (userQuery.isNotBlank()) userQuery else "এই ছবিতে কী কী আছে বিস্তারিত ও স্বাভাবিক বাংলায় ব্যাখ্যা করো।"

        val requestJson = JSONObject().apply {
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", "You are Gemini Nexus Max. Analyze this image thoroughly in natural Bengali:\n$prompt"))
                put(
                    JSONObject().put(
                        "inline_data",
                        JSONObject().apply {
                            put("mime_type", "image/jpeg")
                            put("data", base64Data)
                        }
                    )
                )
            }
            put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e(TAG, "Vision error: $bodyString")
                return@withContext "চিত্র বিশ্লেষণ ব্যর্থ হয়েছে (HTTP ${response.code})"
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    textBuilder.append(parts.getJSONObject(i).optString("text"))
                }
            }
            if (textBuilder.isNotEmpty()) textBuilder.toString() else "চিত্র বিশ্লেষণ সম্পন্ন হয়েছে।"
        } catch (e: Exception) {
            Log.e(TAG, "Vision API error", e)
            "ছবি বিশ্লেষণ ব্যর্থ হয়েছে: ${e.message}"
        }
    }

    // Google Search Grounding with Real-time Web Data
    suspend fun executeSearchGrounding(query: String): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", "Please research and provide real-time grounded facts in natural Bengali for: $query"))
                    )
                )
            )
            put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
            put(
                "system_instruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            "You are Gemini Nexus Max. Deliver verified Google Search grounded results in clear, crisp Bengali with zero preambles."
                        )
                    )
                )
            )
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e(TAG, "Search Grounding error: $bodyString")
                return@withContext "সার্চ গ্রাউন্ডিং ব্যর্থ হয়েছে (HTTP ${response.code})"
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    textBuilder.append(parts.getJSONObject(i).optString("text"))
                }
            }
            if (textBuilder.isNotEmpty()) textBuilder.toString() else "সার্চ ফলাফল প্রস্তুত করা সম্ভব হয়নি।"
        } catch (e: Exception) {
            Log.e(TAG, "Search API error", e)
            "লাইভ সার্চ ব্যর্থ হয়েছে: ${e.message}"
        }
    }
}
