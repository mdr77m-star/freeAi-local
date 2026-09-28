package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCodeBackground
import com.example.ui.theme.DarkCodeHeader
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekCyan
import com.example.ui.theme.DeepThinkBackground
import com.example.ui.theme.DeepThinkBorder
import com.example.ui.theme.DeepThinkText

@Composable
fun UserMessageItem(
    content: String,
    isWebSearch: Boolean
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (isWebSearch) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = DeepSeekBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بحث الويب مفعّل",
                        fontSize = 11.sp,
                        color = DeepSeekBlue
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkOutline, RoundedCornerShape(16.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("User message", content))
                        Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("user_message_bubble")
            ) {
                Text(
                    text = content,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun AssistantMessageItem(
    id: Long,
    content: String,
    thoughtProcess: String?,
    thoughtDurationSeconds: Int,
    isStreaming: Boolean,
    isSpeakingThis: Boolean,
    onSpeakClick: () -> Unit,
    onRegenerateClick: () -> Unit
) {
    val context = LocalContext.current
    var isThinkingExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("assistant_message_$id")
    ) {
        // Assistant Header (DeepSeek Qwengram Logo + Model Name)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DeepSeekBlue, DeepSeekCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Q",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Qwengram-0.8B",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "DeepSeek UI",
                fontSize = 10.sp,
                color = DeepSeekCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(DeepSeekCyan.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // DeepThink (R1) Collapsible Reasoning Accordion
        if (!thoughtProcess.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = DeepThinkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, DeepThinkBorder, RoundedCornerShape(12.dp))
                    .testTag("deepthink_card_$id")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isThinkingExpanded = !isThinkingExpanded }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = DeepSeekCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (thoughtDurationSeconds > 0)
                                "تم التفكير في $thoughtDurationSeconds ثوانٍ (DeepThink)"
                            else "عملية التفكير والتحليل (DeepThink)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepThinkText
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (isThinkingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isThinkingExpanded) "طي" else "توسيع",
                            tint = DeepThinkText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isThinkingExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(DeepThinkBorder)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = thoughtProcess,
                                color = DeepThinkText.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Response Body with Rich Markdown rendering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            RichMarkdownContent(content = content, isStreaming = isStreaming)
        }

        // Action Toolbar (Copy, Voice TTS, Share, Regenerate)
        if (!isStreaming && content.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Assistant response", content))
                        Toast.makeText(context, "تم نسخ الرد", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ الرد",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // TTS Speaker Button
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.Default.VolumeUp,
                        contentDescription = if (isSpeakingThis) "إيقاف القراءة" else "قراءة صوتية",
                        tint = if (isSpeakingThis) DeepSeekCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة رد Qwengram"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Regenerate Button
                IconButton(
                    onClick = onRegenerateClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة التوليد",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RichMarkdownContent(content: String, isStreaming: Boolean) {
    val context = LocalContext.current
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\n([\\s\\S]*?)```")

    // Split text into regular markdown and code segments
    val segments = remember(content) {
        val list = mutableListOf<Segment>()
        var lastIndex = 0

        for (match in codeBlockRegex.findAll(content)) {
            val range = match.range
            if (range.first > lastIndex) {
                list.add(Segment.Text(content.substring(lastIndex, range.first)))
            }
            val lang = match.groupValues[1].ifBlank { "code" }
            val code = match.groupValues[2]
            list.add(Segment.Code(lang, code))
            lastIndex = range.last + 1
        }

        if (lastIndex < content.length) {
            list.add(Segment.Text(content.substring(lastIndex)))
        }
        list
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        for (segment in segments) {
            when (segment) {
                is Segment.Text -> {
                    Text(
                        text = segment.text.trim(),
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is Segment.Code -> {
                    var copied by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCodeBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, DarkOutline, RoundedCornerShape(8.dp))
                    ) {
                        Column {
                            // Code block header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkCodeHeader)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = segment.language.lowercase(),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("code", segment.code))
                                            copied = true
                                            Toast.makeText(context, "تم نسخ الكود", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "نسخ الكود",
                                        tint = if (copied) DeepSeekCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (copied) "تم النسخ" else "نسخ",
                                        fontSize = 11.sp,
                                        color = if (copied) DeepSeekCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Code content
                            Text(
                                text = segment.code.trim(),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Streaming blinking cursor
        if (isStreaming) {
            val infiniteTransition = rememberInfiniteTransition(label = "cursor")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cursorAlpha"
            )

            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DeepSeekBlue.copy(alpha = alpha))
            )
        }
    }
}

sealed class Segment {
    data class Text(val text: String) : Segment()
    data class Code(val language: String, val code: String) : Segment()
}
