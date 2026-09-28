package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InferenceMode
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepSeekBlue
import com.example.ui.theme.DeepSeekCyan

@Composable
fun DeepSeekTopBar(
    currentMode: InferenceMode,
    selectedGgufFilename: String,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onModelInfoClick: () -> Unit,
    onGgufManagerClick: () -> Unit,
    onSelectMode: (InferenceMode) -> Unit
) {
    var modeMenuExpanded by remember { mutableStateOf(false) }

    val shortQuant = remember(selectedGgufFilename) {
        val upper = selectedGgufFilename.uppercase()
        when {
            upper.contains("Q4_K_M") -> "Q4_K_M"
            upper.contains("Q6_K") -> "Q6_K"
            upper.contains("Q8_0") -> "Q8_0"
            upper.contains("BF16") -> "BF16"
            upper.contains("FP16") -> "FP16"
            else -> "GGUF"
        }
    }

    Surface(
        color = DarkSurface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "فتح القائمة الجانبية",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Model Selector Pill (DeepSeek Style)
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { modeMenuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("model_selector_pill"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (currentMode == InferenceMode.LOCAL_OFFLINE) DeepSeekCyan else DeepSeekBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Qwengram-0.8B",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentMode == InferenceMode.LOCAL_OFFLINE) shortQuant else "سحابي",
                        fontSize = 11.sp,
                        color = if (currentMode == InferenceMode.LOCAL_OFFLINE) DeepSeekCyan else DeepSeekBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                (if (currentMode == InferenceMode.LOCAL_OFFLINE) DeepSeekCyan else DeepSeekBlue)
                                    .copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                DropdownMenu(
                    expanded = modeMenuExpanded,
                    onDismissRequest = { modeMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(DeepSeekCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("محرك GGUF المحلي ($shortQuant)")
                            }
                        },
                        onClick = {
                            onSelectMode(InferenceMode.LOCAL_OFFLINE)
                            modeMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(DeepSeekBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("سحابة Hugging Face API")
                            }
                        },
                        onClick = {
                            onSelectMode(InferenceMode.HUGGING_FACE_API)
                            modeMenuExpanded = false
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = DeepSeekCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إدارة نماذج Assets (GGUF)...")
                            }
                        },
                        onClick = {
                            modeMenuExpanded = false
                            onGgufManagerClick()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onGgufManagerClick,
                modifier = Modifier.testTag("open_gguf_manager_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = "نماذج Assets",
                    tint = DeepSeekCyan
                )
            }

            IconButton(
                onClick = onModelInfoClick,
                modifier = Modifier.testTag("model_info_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "معلومات النموذج",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier.testTag("new_chat_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "محادثة جديدة",
                    tint = DeepSeekBlue
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_top_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "الإعدادات",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
