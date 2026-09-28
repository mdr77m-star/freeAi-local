package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InferenceMode
import com.example.data.model.ModelSettings
import com.example.data.model.OFFICIAL_QWENGRAM_VARIANTS
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekCyan

@Composable
fun SettingsDialog(
    currentSettings: ModelSettings,
    onSaveSettings: (ModelSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(currentSettings.inferenceMode) }
    var selectedGguf by remember { mutableStateOf(currentSettings.selectedGgufFilename) }
    var token by remember { mutableStateOf(currentSettings.hfApiToken) }
    var temperature by remember { mutableFloatStateOf(currentSettings.temperature) }
    var topP by remember { mutableFloatStateOf(currentSettings.topP) }
    var voiceSpeed by remember { mutableFloatStateOf(currentSettings.voiceSpeed) }
    var voicePitch by remember { mutableFloatStateOf(currentSettings.voicePitch) }
    var voiceLanguage by remember { mutableStateOf(currentSettings.voiceLanguage) }
    var systemPrompt by remember { mutableStateOf(currentSettings.systemPrompt) }

    // Security & Production Controls
    var isAppLockEnabled by remember { mutableStateOf(currentSettings.isAppLockEnabled) }
    var appLockPin by remember { mutableStateOf(currentSettings.appLockPin) }
    var maskInternalDetails by remember { mutableStateOf(currentSettings.maskInternalDetails) }
    var isIncognitoMode by remember { mutableStateOf(currentSettings.isIncognitoMode) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DeepSeekCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "إعدادات التطبيق والإنتاج",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Section: Production Security & Confidentiality
                Text(
                    text = "🛡️ وضع الإنتاج والأمان المشفر:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepSeekCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                // App Lock PIN Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "قفل التطبيق برمز PIN",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "منع أي شخص من الدخول ورؤية المحادثات والنماذج",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = { isAppLockEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepSeekCyan, checkedTrackColor = DeepSeekBlue)
                    )
                }

                if (isAppLockEnabled) {
                    OutlinedTextField(
                        value = appLockPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) appLockPin = it },
                        label = { Text("رمز PIN (4 أرقام)") },
                        placeholder = { Text("1234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Mask Internal Details Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إخفاء البنية والمسارات الداخلية",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "حماية الفكرة من النسخ والتطفل في وضع الإنتاج",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = maskInternalDetails,
                        onCheckedChange = { maskInternalDetails = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepSeekCyan, checkedTrackColor = DeepSeekBlue)
                    )
                }

                // Incognito Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "المحادثة المتخفية (Incognito)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "عدم تسجيل أي أثر في السجل المحلي",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isIncognitoMode,
                        onCheckedChange = { isIncognitoMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepSeekCyan, checkedTrackColor = DeepSeekBlue)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkOutline)

                Text(
                    text = "وضع التشغيل:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = mode == InferenceMode.LOCAL_OFFLINE,
                        onClick = { mode = InferenceMode.LOCAL_OFFLINE }
                    )
                    Text(
                        text = "محرك GGUF المحلي (Assets / الجهاز)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = mode == InferenceMode.HUGGING_FACE_API,
                        onClick = { mode = InferenceMode.HUGGING_FACE_API }
                    )
                    Text(
                        text = "سحابة Hugging Face API",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkOutline)

                // GGUF Model Variant Selection
                Text(
                    text = "أوزان QwenGram-0.8B المفضلة في Assets:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                for (variant in OFFICIAL_QWENGRAM_VARIANTS) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedGguf = variant.filename }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedGguf == variant.filename,
                            onClick = { selectedGguf = variant.filename }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${variant.bits} (${variant.quantization})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedGguf == variant.filename) DeepSeekCyan else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = variant.expectedSize,
                                    fontSize = 11.sp,
                                    color = DeepSeekBlue
                                )
                            }
                            Text(
                                text = variant.filename,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkOutline)

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("رمز Hugging Face API Token (اختياري)") },
                    placeholder = { Text("hf_xxxxxxxxxxxxx") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Temperature Slider
                Text(
                    text = "درجة الإبداع (Temperature): ${String.format("%.1f", temperature)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0.1f..1.5f,
                    steps = 13
                )

                // Top-P Slider
                Text(
                    text = "دقة الاختيار (Top-P): ${String.format("%.2f", topP)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = topP,
                    onValueChange = { topP = it },
                    valueRange = 0.1f..1.0f,
                    steps = 8
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "إعدادات الصوت والنطق (TTS):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Voice Speed Slider
                Text(
                    text = "سرعة القراءة: ${String.format("%.1fx", voiceSpeed)}",
                    fontSize = 12.sp
                )
                Slider(
                    value = voiceSpeed,
                    onValueChange = { voiceSpeed = it },
                    valueRange = 0.5f..2.0f,
                    steps = 14
                )

                // Voice Language toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("لغة القراءة:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(
                        selected = voiceLanguage == "ar",
                        onClick = { voiceLanguage = "ar" }
                    )
                    Text("العربية", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(
                        selected = voiceLanguage == "en",
                        onClick = { voiceLanguage = "en" }
                    )
                    Text("English", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // System Prompt
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("التعليمات التوجيهية للنموذج (System Prompt)") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSettings(
                        currentSettings.copy(
                            inferenceMode = mode,
                            selectedGgufFilename = selectedGguf,
                            hfApiToken = token.trim(),
                            temperature = temperature,
                            topP = topP,
                            voiceSpeed = voiceSpeed,
                            voicePitch = voicePitch,
                            voiceLanguage = voiceLanguage,
                            systemPrompt = systemPrompt.trim(),
                            isAppLockEnabled = isAppLockEnabled,
                            appLockPin = if (isAppLockEnabled && appLockPin.length == 4) appLockPin else currentSettings.appLockPin,
                            maskInternalDetails = maskInternalDetails,
                            isIncognitoMode = isIncognitoMode
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepSeekBlue)
            ) {
                Text("حفظ التغييرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
