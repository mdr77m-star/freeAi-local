package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.model.InferenceMode
import com.example.data.model.ModelSettings
import com.example.data.remote.HuggingFaceClient
import com.example.engine.GenerationChunk
import com.example.engine.QwengramLocalEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao,
    private val hfClient: HuggingFaceClient = HuggingFaceClient(),
    private val localEngine: QwengramLocalEngine = QwengramLocalEngine()
) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String = "محادثة جديدة"): Long = withContext(Dispatchers.IO) {
        val session = ChatSessionEntity(title = title)
        chatDao.insertSession(session)
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) = withContext(Dispatchers.IO) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun deleteAllSessions() = withContext(Dispatchers.IO) {
        chatDao.deleteAllSessions()
    }

    suspend fun saveUserMessage(
        sessionId: Long,
        content: String,
        isWebSearch: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = content,
            isWebSearch = isWebSearch
        )
        val msgId = chatDao.insertMessage(userMsg)
        
        // Auto-update session title if it's still default
        val session = chatDao.getSessionById(sessionId)
        if (session != null && (session.title == "محادثة جديدة" || session.title.isBlank())) {
            val titleSnippet = content.trim().take(30).replace("\n", " ")
            chatDao.updateSessionTitle(sessionId, titleSnippet)
        }
        msgId
    }

    suspend fun saveAssistantMessage(
        sessionId: Long,
        content: String,
        thoughtProcess: String?,
        durationSeconds: Int
    ): Long = withContext(Dispatchers.IO) {
        val assistantMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "assistant",
            content = content,
            thoughtProcess = thoughtProcess,
            thoughtDurationSeconds = durationSeconds
        )
        chatDao.insertMessage(assistantMsg)
    }

    suspend fun streamGenerate(
        sessionId: Long,
        userPrompt: String,
        settings: ModelSettings,
        onChunk: suspend (GenerationChunk) -> Unit
    ) = withContext(Dispatchers.IO) {
        // Collect conversation context
        val previousMessages = chatDao.getMessagesForSession(sessionId).first()
        val history = previousMessages.map { it.role to it.content }

        if (settings.inferenceMode == InferenceMode.HUGGING_FACE_API) {
            // Online Cloud mode using Hugging Face Inference API
            val formattedPrompt = localEngine.formatPrompt(history, userPrompt, settings)
            val startTime = System.currentTimeMillis()
            
            onChunk(
                GenerationChunk(
                    thinkingText = "• [Hugging Face Cloud]: جارٍ إرسال الطلب إلى نموذج ${settings.modelRepoId}...",
                    responseText = "",
                    isThinking = true,
                    isFinished = false,
                    thinkingDurationSeconds = 1
                )
            )

            val result = hfClient.generateText(formattedPrompt, settings)
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt().coerceAtLeast(1)

            if (result.isSuccess) {
                val fullResponse = result.getOrThrow()
                // Parse optional <think> tag if present in model output
                val (thinking, cleanResponse) = parseThinkingFromText(fullResponse)
                onChunk(
                    GenerationChunk(
                        thinkingText = thinking,
                        responseText = cleanResponse,
                        isThinking = false,
                        isFinished = true,
                        thinkingDurationSeconds = elapsedSec
                    )
                )
            } else {
                // In case of error (e.g. no internet or rate limit), explain and fall back seamlessly to local engine
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "فشل الاتصال بـ Hugging Face"
                onChunk(
                    GenerationChunk(
                        thinkingText = "• تنبيه: تعذر استدعاء واجهة Hugging Face ($errorMsg).\n• التحويل التلقائي إلى محرك Qwengram المحلي على الجهاز...",
                        responseText = "",
                        isThinking = true,
                        isFinished = false,
                        thinkingDurationSeconds = 2
                    )
                )
                // Run local engine as fallback
                localEngine.streamGenerate(userPrompt, settings, history).collect { chunk ->
                    onChunk(chunk)
                }
            }
        } else {
            // Local Offline on-device Engine
            localEngine.streamGenerate(userPrompt, settings, history).collect { chunk ->
                onChunk(chunk)
            }
        }
    }

    private fun parseThinkingFromText(text: String): Pair<String, String> {
        val thinkRegex = Regex("<think>([\\s\\S]*?)</think>", RegexOption.IGNORE_CASE)
        val match = thinkRegex.find(text)
        return if (match != null) {
            val thinking = match.groupValues[1].trim()
            val clean = text.replace(match.value, "").trim()
            Pair(thinking, clean)
        } else {
            Pair("", text.trim())
        }
    }
}
