package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FirestoreRepository
import com.example.model.GeneratedMedia
import com.example.ui.components.DynamicNeonGlowBorder
import com.example.ui.components.WavyImagePlaceholder
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImagenTab(
    currentUserId: String,
    repository: FirestoreRepository
) {
    val scope = rememberCoroutineScope()
    var promptInput by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedResolution by remember { mutableStateOf("1K") }
    var isGenerating by remember { mutableStateOf(false) }

    val savedMediaList by repository.observeMedia().collectAsState(initial = emptyList())

    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:2", "21:9")
    val resolutions = listOf("1K", "2K", "4K")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonPink.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonPink,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "ইমেজিন ৩ আল্ট্রা স্টুডিও",
                    color = NexusTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "উচ্চ-রেজোলিউশন ইমেজ জেনারেশন ও অ্যাসপেক্ট রেশিও কন্ট্রোল",
                    color = NexusTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Prompt Input
        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            placeholder = {
                Text(
                    text = "বাংলা বা ইংরেজিতে ছবির প্রম্পট লিখুন (যেমন: সাইবারপাঙ্ক ঢাকা ২০৮০, নিওন লাইট)...",
                    color = NexusTextMuted,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("imagen_prompt_input"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPink,
                unfocusedBorderColor = NexusCardBorder,
                focusedContainerColor = NexusCardBg,
                unfocusedContainerColor = NexusCardBg,
                focusedTextColor = NexusTextPrimary,
                unfocusedTextColor = NexusTextPrimary
            ),
            maxLines = 3
        )

        // Aspect Ratio Selector Chips
        Text(
            text = "অ্যাসপেক্ট রেশিও নির্বাচন:",
            color = NexusTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            aspectRatios.forEach { ratio ->
                val isSelected = selectedAspectRatio == ratio
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeonPink.copy(alpha = 0.25f) else NexusDarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonPink else NexusCardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedAspectRatio = ratio }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = ratio,
                        color = if (isSelected) NeonPink else NexusTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Resolution Selector Chips
        Text(
            text = "রেজোলিউশন কোয়ালিটি:",
            color = NexusTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            resolutions.forEach { res ->
                val isSelected = selectedResolution == res
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else NexusDarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonCyan else NexusCardBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedResolution = res }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = res,
                        color = if (isSelected) NeonCyan else NexusTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Generate Button
        Button(
            onClick = {
                if (promptInput.isNotBlank() && !isGenerating) {
                    isGenerating = true
                    val currentPrompt = promptInput.trim()
                    val ratio = selectedAspectRatio
                    val res = selectedResolution

                    scope.launch {
                        // Undulating wavy neon animation plays during generation
                        delay(2800)

                        // Save created media record to Firestore
                        val mediaItem = GeneratedMedia(
                            userId = currentUserId,
                            type = "image",
                            prompt = currentPrompt,
                            aspectRatio = ratio,
                            resolution = res,
                            mediaUrl = "android.resource://com.aistudio.nexusmax.kpxvq/drawable/nexus_launcher_icon_1793120823020"
                        )
                        repository.saveMedia(mediaItem)
                        isGenerating = false
                    }
                }
            },
            enabled = promptInput.isNotBlank() && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_image_button"),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonPink,
                contentColor = Color.White
            )
        ) {
            Text("ইমেজ জেনারেট করুন (ইমেজিন ৩ আল্ট্রা)", fontWeight = FontWeight.Bold)
        }

        // Generation State / Wavy Border Placeholder
        if (isGenerating) {
            WavyImagePlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                prompt = promptInput
            )
        }

        // Gallery of Saved Generated Images
        Text(
            text = "সংরক্ষিত ইমেজ গ্যালারি:",
            color = NexusTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (savedMediaList.isEmpty() && !isGenerating) {
                item {
                    Text(
                        text = "এখনও কোনও ছবি তৈরি করা হয়নি। উপরে প্রম্পট লিখে জেনারেট করুন।",
                        color = NexusTextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            items(savedMediaList) { media ->
                DynamicNeonGlowBorder(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    isProcessing = false
                ) {
                    Column(
                        modifier = Modifier
                            .background(NexusCardBg)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PROMPT: ${media.prompt}",
                                color = NexusTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RATIO: ${media.aspectRatio}",
                                color = NeonCyan,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "QUALITY: ${media.resolution}",
                                color = NeonPurple,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NexusDarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = media.mediaUrl,
                                contentDescription = media.prompt,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
