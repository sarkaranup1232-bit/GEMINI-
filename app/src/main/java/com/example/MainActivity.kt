package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ai.GeminiNexusBrain
import com.example.ai.VoiceTtsManager
import com.example.data.AuthManager
import com.example.data.FirestoreRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.NexusMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NexusBlack
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var nexusBrain: GeminiNexusBrain
    private lateinit var voiceManager: VoiceTtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        // STEP 2: Initialize managers and repositories
        authManager = AuthManager(this)
        firestoreRepository = FirestoreRepository(this)
        nexusBrain = GeminiNexusBrain()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NexusBlack
                ) {
                    val scope = rememberCoroutineScope()
                    val voice = remember { VoiceTtsManager(this@MainActivity, scope) }
                    voiceManager = voice

                    AppRoot(
                        authManager = authManager,
                        repository = firestoreRepository,
                        brain = nexusBrain,
                        voiceManager = voice
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::voiceManager.isInitialized) {
            voiceManager.destroy()
        }
    }
}

@Composable
fun AppRoot(
    authManager: AuthManager,
    repository: FirestoreRepository,
    brain: GeminiNexusBrain,
    voiceManager: VoiceTtsManager
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            authManager = authManager,
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        NexusMainScreen(
            currentUserId = currentUser!!.uid,
            authManager = authManager,
            repository = repository,
            brain = brain,
            voiceManager = voiceManager,
            onSignOut = {
                currentUser = null
            }
        )
    }
}
