package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceManager
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.GgufAssetScanner
import com.example.data.model.GgufModelVariant
import com.example.data.model.InferenceMode
import com.example.data.model.ModelSettings
import com.example.data.repository.ChatRepository
import com.example.engine.GenerationChunk
import android.net.Uri
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database.chatDao())
    val voiceManager = VoiceManager(application)
    private val scanner = GgufAssetScanner(application)

    private val _detectedModels = MutableStateFlow<List<GgufModelVariant>>(emptyList())
    val detectedModels: StateFlow<List<GgufModelVariant>> = _detectedModels.asStateFlow()

    private val _isScanningModels = MutableStateFlow(false)
    val isScanningModels: StateFlow<Boolean> = _isScanningModels.asStateFlow()

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingChunk = MutableStateFlow<GenerationChunk?>(null)
    val streamingChunk: StateFlow<GenerationChunk?> = _streamingChunk.asStateFlow()

    private val _modelSettings = MutableStateFlow(ModelSettings())
    val modelSettings: StateFlow<ModelSettings> = _modelSettings.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var currentJob: Job? = null
    private var observeMessagesJob: Job? = null

    init {
        scanModels()

        viewModelScope.launch {
            sessions.collect { list ->
                if (_currentSessionId.value == null && list.isNotEmpty()) {
                    selectSession(list.first().id)
                } else if (_currentSessionId.value == null && list.isEmpty()) {
                    val newId = repository.createNewSession()
                    selectSession(newId)
                }
            }
        }
    }

    fun scanModels() {
        viewModelScope.launch {
            _isScanningModels.value = true
            val list = scanner.scanModels()
            _detectedModels.value = list
            _isScanningModels.value = false

            // If an available variant is detected, and current selected model is not available, switch to available
            val available = list.firstOrNull { it.isAvailable }
            if (available != null && list.none { it.filename == _modelSettings.value.selectedGgufFilename && it.isAvailable }) {
                _modelSettings.value = _modelSettings.value.copy(selectedGgufFilename = available.filename)
            }
        }
    }

    fun selectGgufVariant(variant: GgufModelVariant) {
        _modelSettings.value = _modelSettings.value.copy(
            selectedGgufFilename = variant.filename,
            inferenceMode = InferenceMode.LOCAL_OFFLINE
        )
    }

    fun importGgufFile(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isScanningModels.value = true
            val result = scanner.importGgufFromUri(uri, fileName)
            if (result.isSuccess) {
                scanModels()
            }
            _isScanningModels.value = false
        }
    }

    fun selectSession(sessionId: Long) {
        if (_currentSessionId.value == sessionId && _messages.value.isNotEmpty()) return
        _currentSessionId.value = sessionId
        stopGeneration()
        voiceManager.stopSpeaking()

        observeMessagesJob?.cancel()
        observeMessagesJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            stopGeneration()
            voiceManager.stopSpeaking()
            val newId = repository.createNewSession()
            selectSession(newId)
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            if (newTitle.isNotBlank()) {
                repository.updateSessionTitle(sessionId, newTitle.trim())
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            repository.deleteAllSessions()
            startNewSession()
        }
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_modelSettings.value.isAppLockEnabled && _modelSettings.value.appLockPin.isNotBlank()) {
            _isAppLocked.value = true
        }
    }

    fun toggleIncognitoMode() {
        _modelSettings.value = _modelSettings.value.copy(
            isIncognitoMode = !_modelSettings.value.isIncognitoMode
        )
    }

    fun toggleDeepThink() {
        _modelSettings.value = _modelSettings.value.copy(
            isDeepThinkEnabled = !_modelSettings.value.isDeepThinkEnabled
        )
    }

    fun toggleWebSearch() {
        _modelSettings.value = _modelSettings.value.copy(
            isWebSearchEnabled = !_modelSettings.value.isWebSearchEnabled
        )
    }

    fun updateSettings(settings: ModelSettings) {
        _modelSettings.value = settings
        if (!settings.isAppLockEnabled || settings.appLockPin.isBlank()) {
            _isAppLocked.value = false
        }
    }

    fun setInferenceMode(mode: InferenceMode) {
        _modelSettings.value = _modelSettings.value.copy(inferenceMode = mode)
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val sId = _currentSessionId.value ?: return
        val currentSettings = _modelSettings.value

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isGenerating.value = true
            _streamingChunk.value = null

            // 1. Save user message to database
            repository.saveUserMessage(
                sessionId = sId,
                content = trimmed,
                isWebSearch = currentSettings.isWebSearchEnabled
            )

            var lastChunk: GenerationChunk? = null

            // 2. Stream AI generation
            try {
                repository.streamGenerate(
                    sessionId = sId,
                    userPrompt = trimmed,
                    settings = currentSettings
                ) { chunk ->
                    lastChunk = chunk
                    _streamingChunk.value = chunk
                }

                // 3. Save final assistant message to database
                lastChunk?.let { chunk ->
                    repository.saveAssistantMessage(
                        sessionId = sId,
                        content = chunk.responseText,
                        thoughtProcess = if (chunk.thinkingText.isNotBlank()) chunk.thinkingText else null,
                        durationSeconds = chunk.thinkingDurationSeconds
                    )
                }
            } catch (e: Exception) {
                val errorMsg = "حدث خطأ أثناء معالجة الرد: ${e.localizedMessage}"
                repository.saveAssistantMessage(
                    sessionId = sId,
                    content = errorMsg,
                    thoughtProcess = null,
                    durationSeconds = 0
                )
            } finally {
                _isGenerating.value = false
                _streamingChunk.value = null
            }
        }
    }

    fun stopGeneration() {
        if (_isGenerating.value) {
            val chunk = _streamingChunk.value
            val sId = _currentSessionId.value
            currentJob?.cancel()
            _isGenerating.value = false

            if (chunk != null && sId != null && chunk.responseText.isNotBlank()) {
                viewModelScope.launch {
                    repository.saveAssistantMessage(
                        sessionId = sId,
                        content = chunk.responseText + " [تم الإيقاف]",
                        thoughtProcess = if (chunk.thinkingText.isNotBlank()) chunk.thinkingText else null,
                        durationSeconds = chunk.thinkingDurationSeconds
                    )
                }
            }
            _streamingChunk.value = null
        }
    }

    fun regenerateLastResponse() {
        val lastUserMsg = _messages.value.lastOrNull { it.role == "user" }
        if (lastUserMsg != null && !_isGenerating.value) {
            sendMessage(lastUserMsg.content)
        }
    }

    fun speakMessage(message: ChatMessageEntity) {
        val settings = _modelSettings.value
        voiceManager.speak(
            text = message.content,
            messageId = message.id,
            speed = settings.voiceSpeed,
            pitch = settings.voicePitch,
            language = settings.voiceLanguage
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
