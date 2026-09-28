package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDialog(
    isListening: Boolean,
    spokenText: String,
    rmsLevel: Float,
    speechError: String?,
    onStartListening: (String) -> Unit,
    onStopListening: () -> Unit,
    onSendText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("ar-SA") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onStartListening(selectedLanguage)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && !isListening) {
            onStartListening(selectedLanguage)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            onStopListening()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "محادثة صوتية مع Qwengram",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "تحدث مباشرة باللغة العربية أو الإنجليزية",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Language Selector Pills
            Row(
                modifier = Modifier.padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LanguageChip(
                    label = "العربية",
                    isSelected = selectedLanguage == "ar-SA",
                    onClick = {
                        selectedLanguage = "ar-SA"
                        if (isListening) {
                            onStopListening()
                            onStartListening("ar-SA")
                        }
                    }
                )

                LanguageChip(
                    label = "English",
                    isSelected = selectedLanguage == "en-US",
                    onClick = {
                        selectedLanguage = "en-US"
                        if (isListening) {
                            onStopListening()
                            onStartListening("en-US")
                        }
                    }
                )
            }

            // Pulsing Audio Visualizer Wave Circle
            val animatedScale by animateFloatAsState(
                targetValue = if (isListening) 1f + (rmsLevel * 0.4f) else 1f,
                animationSpec = tween(150),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(animatedScale)
                            .clip(CircleShape)
                            .background(DeepSeekCyan.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .size(95.dp)
                            .scale(animatedScale * 0.9f)
                            .clip(CircleShape)
                            .background(DeepSeekBlue.copy(alpha = 0.35f))
                    )
                }

                // Inner Mic Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isListening) listOf(DeepSeekBlue, DeepSeekCyan)
                                else listOf(MaterialTheme.colorScheme.surfaceVariant, DarkOutline)
                            )
                        )
                        .clickable {
                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else if (isListening) {
                                onStopListening()
                            } else {
                                onStartListening(selectedLanguage)
                            }
                        }
                        .testTag("voice_record_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "تسجيل الصوت",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State label
            Text(
                text = when {
                    !hasPermission -> "اضغط لمنح إذن الميكروفون"
                    isListening -> "جاري الاستماع... تفضل بالتحدث"
                    speechError != null -> speechError
                    spokenText.isNotBlank() -> "تم التقاط الصوت"
                    else -> "اضغط على الميكروفون لبدء التحدث"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (speechError != null) MaterialTheme.colorScheme.error else DeepSeekCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-time transcribed text display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkOutline, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (spokenText.isBlank()) {
                    Text(
                        text = "سيظهر النص المنطوق هنا في الوقت الفعلي...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = spokenText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onStopListening()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء")
                }

                Button(
                    onClick = {
                        if (spokenText.isNotBlank()) {
                            onStopListening()
                            onSendText(spokenText)
                            onDismiss()
                        }
                    },
                    enabled = spokenText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepSeekBlue)
                ) {
                    Text("إرسال للشات")
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) DeepSeekBlue else DarkSurfaceCard
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) DeepSeekBlue else DarkOutline

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}
