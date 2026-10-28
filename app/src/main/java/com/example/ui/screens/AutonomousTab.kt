package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiNexusBrain
import com.example.ai.VoiceTtsManager
import com.example.data.FirestoreRepository
import com.example.model.AutonomousTask
import com.example.model.PythonExecutionResult
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
import com.example.ui.theme.NexusTextMuted
import com.example.ui.theme.NexusTextPrimary
import com.example.ui.theme.NexusTextSecondary
import kotlinx.coroutines.launch

@Composable
fun AutonomousTab(
    currentUserId: String,
    repository: FirestoreRepository,
    brain: GeminiNexusBrain,
    voiceManager: VoiceTtsManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var destinationInput by remember { mutableStateOf("গুলশান ২, ঢাকা") }
    var mapsResult by remember { mutableStateOf<String?>(null) }
    var isMapsCalculating by remember { mutableStateOf(false) }

    var pythonCode by remember {
        mutableStateOf(
            """
            import math
            import matplotlib.pyplot as plt

            # Model loss optimization trajectory
            loss_history = [24.5, 48.2, 68.7, 85.1, 97.4]
            plt.plot(loss_history)
            print("Optimal convergence reached: 97.4%")
            """.trimIndent()
        )
    }
    var pythonResult by remember { mutableStateOf<PythonExecutionResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Power & Priority Execution Status Banner
        DynamicNeonGlowBorder(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexusCardBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "হাই-প্রায়োরিটি ফোরগ্রাউন্ড এক্সিকিউশন সক্রিয়",
                        color = NeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "জিরো ব্যাকগ্রাউন্ড স্লিপ • থার্মাল অপটিমাইজেশন • রুট লেভেল ডিভাইস ব্রেন",
                        color = NexusTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Section 1: Communications Master
        Text(
            text = "টোটাল কমিউনিকেশনস অটোমেশন",
            color = NexusTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NexusCardBg)
                .border(1.dp, NexusCardBorder, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = {
                    Text("ফোন নম্বর বা পরিচিতির নাম...", color = NexusTextMuted, fontSize = 13.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_number_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = NexusCardBorder,
                    focusedTextColor = NexusTextPrimary,
                    unfocusedTextColor = NexusTextPrimary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call trigger
                Button(
                    onClick = {
                        val num = phoneNumber.ifBlank { "+8801700000000" }
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                        context.startActivity(dialIntent)
                        scope.launch {
                            repository.recordTask(
                                AutonomousTask(
                                    userId = currentUserId,
                                    actionType = "call",
                                    title = "ডায়াল কল: $num",
                                    status = "completed",
                                    details = "স্বয়ংক্রিয়ভাবে ডায়ালার ট্রিগার করা হয়েছে"
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কল ডায়াল")
                }

                // SMS trigger
                Button(
                    onClick = {
                        val num = phoneNumber.ifBlank { "+8801700000000" }
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$num")).apply {
                            putExtra("sms_body", "নমস্কার, আমি Gemini Nexus Max ব্রেন থেকে যোগাযোগ করছি।")
                        }
                        context.startActivity(smsIntent)
                        scope.launch {
                            repository.recordTask(
                                AutonomousTask(
                                    userId = currentUserId,
                                    actionType = "sms",
                                    title = "SMS বার্তা: $num",
                                    status = "completed",
                                    details = "স্বয়ংক্রিয় ড্রাফট প্রস্তুত"
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SMS বার্তা")
                }
            }
        }

        // Section 2: Google Maps & Navigation Grounding
        Text(
            text = "গুগল ম্যাপস ও নেভিগেশন গ্রাউন্ডিং",
            color = NexusTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NexusCardBg)
                .border(1.dp, NexusCardBorder, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = destinationInput,
                onValueChange = { destinationInput = it },
                placeholder = { Text("গন্তব্য স্থানের নাম লিখুন...", color = NexusTextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("maps_destination_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = NexusCardBorder,
                    focusedTextColor = NexusTextPrimary,
                    unfocusedTextColor = NexusTextPrimary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (destinationInput.isNotBlank() && !isMapsCalculating) {
                            isMapsCalculating = true
                            scope.launch {
                                val result = brain.executeMapsGrounding(destinationInput)
                                mapsResult = result
                                isMapsCalculating = false
                                voiceManager.speak(result)

                                repository.recordTask(
                                    AutonomousTask(
                                        userId = currentUserId,
                                        actionType = "maps",
                                        title = "নেভিগেশন রুট: $destinationInput",
                                        status = "completed",
                                        details = result
                                    )
                                )
                            }
                        }
                    },
                    enabled = !isMapsCalculating,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White)
                ) {
                    if (isMapsCalculating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রুট হিসাব করুন")
                    }
                }

                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(destinationInput)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("ম্যাপস খুলুন")
                }
            }

            if (mapsResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NexusDarkSurface)
                        .padding(12.dp)
                ) {
                    Text(text = mapsResult!!, color = NexusTextPrimary, fontSize = 13.sp, lineHeight = 20.sp)
                }
            }
        }

        // Section 3: Native Python Sandbox & Visual Graph
        Text(
            text = "নেটিভ পাইথন স্যান্ডবক্স ও ডেটা ভিজ্যুয়ালাইজেশন",
            color = NexusTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NexusCardBg)
                .border(1.dp, NexusCardBorder, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = pythonCode,
                onValueChange = { pythonCode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("python_code_editor"),
                shape = RoundedCornerShape(16.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = NeonCyan
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = NexusCardBorder,
                    focusedContainerColor = NexusBlack,
                    unfocusedContainerColor = NexusBlack
                )
            )

            Button(
                onClick = {
                    val result = brain.executePythonSandbox(pythonCode)
                    pythonResult = result
                    scope.launch {
                        repository.recordTask(
                            AutonomousTask(
                                userId = currentUserId,
                                actionType = "sandbox",
                                title = "পাইথন রানটাইম এক্সিকিউশন",
                                status = "completed",
                                details = result.output
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("execute_python_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("স্যান্ডবক্সে কোড রান করুন", fontWeight = FontWeight.Bold)
            }

            if (pythonResult != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NexusBlack)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = pythonResult!!.output,
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // Render dynamic vector chart canvas
                    if (pythonResult!!.chartData.isNotEmpty()) {
                        Text(
                            text = "লাইভ ভেক্টর ম্যাট্রিক্স গ্রাফ:",
                            color = NexusTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexusDarkSurface)
                                .padding(8.dp)
                        ) {
                            val data = pythonResult!!.chartData
                            val maxVal = data.maxOfOrNull { it.second } ?: 100f
                            val barWidth = size.width / (data.size * 1.6f)
                            val spacing = barWidth * 0.6f

                            data.forEachIndexed { i, entry ->
                                val barHeight = (entry.second / maxVal) * (size.height - 20f)
                                val x = i * (barWidth + spacing) + spacing

                                drawRoundRect(
                                    color = if (i % 2 == 0) NeonCyan else NeonPurple,
                                    topLeft = Offset(x, size.height - barHeight),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
