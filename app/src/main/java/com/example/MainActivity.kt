package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppLockScreen
import com.example.ui.components.AssistantMessageItem
import com.example.ui.components.DeepSeekDrawer
import com.example.ui.components.DeepSeekInputBar
import com.example.ui.components.DeepSeekTopBar
import com.example.ui.components.EmptyChatView
import com.example.ui.components.GgufModelManagerDialog
import com.example.ui.components.ModelInfoDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.UserMessageItem
import com.example.ui.components.VoiceDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
                val modelSettings by viewModel.modelSettings.collectAsStateWithLifecycle()

                if (isAppLocked && modelSettings.isAppLockEnabled && modelSettings.appLockPin.isNotBlank()) {
                    AppLockScreen(
                        correctPin = modelSettings.appLockPin,
                        onUnlocked = { viewModel.unlockApp() }
                    )
                } else {
                    MainChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainChatScreen(viewModel: ChatViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val streamingChunk by viewModel.streamingChunk.collectAsStateWithLifecycle()
    val modelSettings by viewModel.modelSettings.collectAsStateWithLifecycle()
    val detectedModels by viewModel.detectedModels.collectAsStateWithLifecycle()
    val isScanningModels by viewModel.isScanningModels.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
    val spokenText by viewModel.voiceManager.spokenText.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsStateWithLifecycle()
    val speechError by viewModel.voiceManager.speechError.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val currentSpeakingMessageId by viewModel.voiceManager.currentSpeakingMessageId.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showModelInfoDialog by remember { mutableStateOf(false) }
    var showGgufDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive or while streaming
    LaunchedEffect(messages.size, streamingChunk?.responseText) {
        val totalItems = messages.size + if (streamingChunk != null) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    // Handle back button for drawer
    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DeepSeekDrawer(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onSelectSession = { id ->
                    viewModel.selectSession(id)
                    coroutineScope.launch { drawerState.close() }
                },
                onNewChat = {
                    viewModel.startNewSession()
                    coroutineScope.launch { drawerState.close() }
                },
                onRenameSession = { id, title ->
                    viewModel.renameSession(id, title)
                },
                onDeleteSession = { id ->
                    viewModel.deleteSession(id)
                },
                onClearAll = {
                    viewModel.clearAllSessions()
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenSettings = {
                    coroutineScope.launch { drawerState.close() }
                    showSettingsDialog = true
                },
                onOpenModelInfo = {
                    coroutineScope.launch { drawerState.close() }
                    showModelInfoDialog = true
                },
                onOpenGgufManager = {
                    coroutineScope.launch { drawerState.close() }
                    showGgufDialog = true
                },
                isAppLockEnabled = modelSettings.isAppLockEnabled,
                onLockApp = {
                    coroutineScope.launch { drawerState.close() }
                    viewModel.lockApp()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                DeepSeekTopBar(
                    currentMode = modelSettings.inferenceMode,
                    selectedGgufFilename = modelSettings.selectedGgufFilename,
                    onMenuClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onNewChatClick = {
                        viewModel.startNewSession()
                    },
                    onSettingsClick = {
                        showSettingsDialog = true
                    },
                    onModelInfoClick = {
                        showModelInfoDialog = true
                    },
                    onGgufManagerClick = {
                        showGgufDialog = true
                    },
                    onSelectMode = { mode ->
                        viewModel.setInferenceMode(mode)
                    }
                )
            },
            bottomBar = {
                Box(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    DeepSeekInputBar(
                        inputText = inputText,
                        onInputTextChange = { inputText = it },
                        isGenerating = isGenerating,
                        isDeepThinkEnabled = modelSettings.isDeepThinkEnabled,
                        isWebSearchEnabled = modelSettings.isWebSearchEnabled,
                        onToggleDeepThink = { viewModel.toggleDeepThink() },
                        onToggleWebSearch = { viewModel.toggleWebSearch() },
                        onSendMessage = { text ->
                            viewModel.sendMessage(text)
                            inputText = ""
                        },
                        onStopGeneration = {
                            viewModel.stopGeneration()
                        },
                        onMicClick = {
                            showVoiceDialog = true
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBackground)
            ) {
                if (messages.isEmpty() && streamingChunk == null) {
                    EmptyChatView(
                        onSelectPrompt = { promptText ->
                            inputText = promptText
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { message ->
                            if (message.role == "user") {
                                UserMessageItem(
                                    content = message.content,
                                    isWebSearch = message.isWebSearch
                                )
                            } else {
                                AssistantMessageItem(
                                    id = message.id,
                                    content = message.content,
                                    thoughtProcess = message.thoughtProcess,
                                    thoughtDurationSeconds = message.thoughtDurationSeconds,
                                    isStreaming = false,
                                    isSpeakingThis = isSpeaking && currentSpeakingMessageId == message.id,
                                    onSpeakClick = {
                                        viewModel.speakMessage(message)
                                    },
                                    onRegenerateClick = {
                                        viewModel.regenerateLastResponse()
                                    }
                                )
                            }
                        }

                        // Active streaming response
                        streamingChunk?.let { chunk ->
                            item {
                                AssistantMessageItem(
                                    id = -1L,
                                    content = chunk.responseText,
                                    thoughtProcess = chunk.thinkingText.ifBlank { null },
                                    thoughtDurationSeconds = chunk.thinkingDurationSeconds,
                                    isStreaming = true,
                                    isSpeakingThis = false,
                                    onSpeakClick = {},
                                    onRegenerateClick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // GGUF Models Manager Dialog
    if (showGgufDialog) {
        GgufModelManagerDialog(
            detectedModels = detectedModels,
            selectedFilename = modelSettings.selectedGgufFilename,
            isScanning = isScanningModels,
            onSelectVariant = { variant ->
                viewModel.selectGgufVariant(variant)
                showGgufDialog = false
            },
            onScanClick = {
                viewModel.scanModels()
            },
            onImportGguf = { uri, name ->
                viewModel.importGgufFile(uri, name)
            },
            onDismiss = {
                showGgufDialog = false
            }
        )
    }

    // Voice Dialog (Speech-to-Text modal)
    if (showVoiceDialog) {
        VoiceDialog(
            isListening = isListening,
            spokenText = spokenText,
            rmsLevel = rmsLevel,
            speechError = speechError,
            onStartListening = { lang ->
                viewModel.voiceManager.startListening(lang)
            },
            onStopListening = {
                viewModel.voiceManager.stopListening()
            },
            onSendText = { text ->
                viewModel.sendMessage(text)
                showVoiceDialog = false
            },
            onDismiss = {
                showVoiceDialog = false
            }
        )
    }

    // Model Info Dialog
    if (showModelInfoDialog) {
        ModelInfoDialog(
            onDismiss = { showModelInfoDialog = false }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = modelSettings,
            onSaveSettings = { newSettings ->
                viewModel.updateSettings(newSettings)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
