package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekCyan

@Composable
fun DeepSeekInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isGenerating: Boolean,
    isDeepThinkEnabled: Boolean,
    isWebSearchEnabled: Boolean,
    onToggleDeepThink: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onMicClick: () -> Unit
) {
    Surface(
        color = DarkSurface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Mode Toggles Row (DeepThink R1 + Web Search)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DeepThink R1 Button
                val dtBorderColor = if (isDeepThinkEnabled) DeepSeekBlue else DarkOutline
                val dtBgColor = if (isDeepThinkEnabled) DeepSeekBlue.copy(alpha = 0.15f) else Color.Transparent
                val dtContentColor = if (isDeepThinkEnabled) DeepSeekCyan else MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, dtBorderColor, RoundedCornerShape(16.dp))
                        .background(dtBgColor)
                        .clickable { onToggleDeepThink() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("toggle_deepthink_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = dtContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DeepThink (R1)",
                        fontSize = 12.sp,
                        fontWeight = if (isDeepThinkEnabled) FontWeight.Bold else FontWeight.Normal,
                        color = dtContentColor
                    )
                }

                // Web Search Button
                val wsBorderColor = if (isWebSearchEnabled) DeepSeekBlue else DarkOutline
                val wsBgColor = if (isWebSearchEnabled) DeepSeekBlue.copy(alpha = 0.15f) else Color.Transparent
                val wsContentColor = if (isWebSearchEnabled) DeepSeekBlue else MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, wsBorderColor, RoundedCornerShape(16.dp))
                        .background(wsBgColor)
                        .clickable { onToggleWebSearch() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("toggle_web_search_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = wsContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "بحث الويب",
                        fontSize = 12.sp,
                        fontWeight = if (isWebSearchEnabled) FontWeight.Bold else FontWeight.Normal,
                        color = wsContentColor
                    )
                }
            }

            // Input Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkOutline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Voice Mic Button
                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("voice_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "التحدث بالصوت",
                            tint = DeepSeekBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Text Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 24.dp, max = 120.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "اسأل Qwengram أي شيء...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputTextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_input_text_field"),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(DeepSeekBlue)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send or Stop Button
                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .clickable { onStopGeneration() }
                                .testTag("stop_generation_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "إيقاف التوليد",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        val canSend = inputText.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (canSend) DeepSeekBlue else DarkOutline)
                                .clickable(enabled = canSend) {
                                    onSendMessage(inputText)
                                }
                                .testTag("send_message_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال الرسالة",
                                tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
