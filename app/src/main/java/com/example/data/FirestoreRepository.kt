package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.AutonomousTask
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.model.GeneratedMedia
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRepo"

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // Observe Sessions
    fun observeChatSessions(): Flow<List<ChatSession>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("chats")
            .orderBy("updatedAt", Query.Direction.DESCENDING)

        val flow = ref.snapshots().map { snapshot ->
            snapshot.toObjects(ChatSession::class.java)
        }.catch { e ->
            Log.e(TAG, "Error observing chat sessions", e)
            emit(emptyList())
        }
        flow.collect { emit(it) }
    }

    // Create or Update Session
    suspend fun saveChatSession(session: ChatSession): String {
        val uid = requireUserId()
        val chatsRef = db.collection("users").document(uid).collection("chats")
        val docRef = if (session.id.isNotBlank()) {
            chatsRef.document(session.id)
        } else {
            chatsRef.document()
        }

        val data = mapOf(
            "id" to docRef.id,
            "userId" to uid,
            "title" to session.title,
            "lastMessage" to session.lastMessage,
            "createdAt" to (session.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        return docRef.id
    }

    // Observe Messages in a session
    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid)
            .collection("chats").document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val flow = ref.snapshots().map { snapshot ->
            snapshot.toObjects(ChatMessage::class.java)
        }.catch { e ->
            Log.e(TAG, "Error observing messages for chat $chatId", e)
            emit(emptyList())
        }
        flow.collect { emit(it) }
    }

    // Save message
    suspend fun saveMessage(chatId: String, message: ChatMessage) {
        val uid = requireUserId()
        val messagesRef = db.collection("users").document(uid)
            .collection("chats").document(chatId)
            .collection("messages")
        val docRef = if (message.id.isNotBlank()) messagesRef.document(message.id) else messagesRef.document()

        val data = mapOf(
            "id" to docRef.id,
            "chatId" to chatId,
            "userId" to uid,
            "role" to message.role,
            "content" to message.content,
            "modelUsed" to message.modelUsed,
            "hasAudio" to message.hasAudio,
            "isSearchGrounding" to message.isSearchGrounding,
            "isImagePrompt" to message.isImagePrompt,
            "imageBase64" to (message.imageBase64 ?: ""),
            "createdAt" to (message.createdAt ?: FieldValue.serverTimestamp())
        )
        docRef.set(data).await()

        // Update parent chat session's lastMessage
        db.collection("users").document(uid).collection("chats").document(chatId)
            .update(
                mapOf(
                    "lastMessage" to message.content.take(80),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    // Observe Saved Media
    fun observeMedia(): Flow<List<GeneratedMedia>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("media")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val flow = ref.snapshots().map { snapshot ->
            snapshot.toObjects(GeneratedMedia::class.java)
        }.catch { e ->
            Log.e(TAG, "Error observing media", e)
            emit(emptyList())
        }
        flow.collect { emit(it) }
    }

    suspend fun saveMedia(media: GeneratedMedia): String {
        val uid = requireUserId()
        val mediaRef = db.collection("users").document(uid).collection("media")
        val docRef = if (media.id.isNotBlank()) mediaRef.document(media.id) else mediaRef.document()
        val data = mapOf(
            "id" to docRef.id,
            "userId" to uid,
            "type" to media.type,
            "prompt" to media.prompt,
            "aspectRatio" to media.aspectRatio,
            "resolution" to media.resolution,
            "mediaUrl" to media.mediaUrl,
            "createdAt" to (media.createdAt ?: FieldValue.serverTimestamp())
        )
        docRef.set(data).await()
        return docRef.id
    }

    // Observe Autonomous Tasks
    fun observeTasks(): Flow<List<AutonomousTask>> = flow {
        val uid = requireUserId()
        val ref = db.collection("users").document(uid).collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val flow = ref.snapshots().map { snapshot ->
            snapshot.toObjects(AutonomousTask::class.java)
        }.catch { e ->
            Log.e(TAG, "Error observing tasks", e)
            emit(emptyList())
        }
        flow.collect { emit(it) }
    }

    suspend fun recordTask(task: AutonomousTask) {
        val uid = requireUserId()
        val tasksRef = db.collection("users").document(uid).collection("tasks")
        val docRef = if (task.id.isNotBlank()) tasksRef.document(task.id) else tasksRef.document()
        val data = mapOf(
            "id" to docRef.id,
            "userId" to uid,
            "actionType" to task.actionType,
            "title" to task.title,
            "status" to task.status,
            "details" to task.details,
            "createdAt" to (task.createdAt ?: FieldValue.serverTimestamp())
        )
        docRef.set(data).await()
    }
}
