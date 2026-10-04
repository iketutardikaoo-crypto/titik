package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DotsVoiceManager
import com.example.ai.GeminiAssistantService
import com.example.data.AppDatabase
import com.example.data.AssistantTask
import com.example.data.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AssistantTab {
    CHAT,
    TASKS,
    TOOLS,
    GAMES
}

data class AssistantUiState(
    val currentTab: AssistantTab = AssistantTab.CHAT,
    val messages: List<ChatMessage> = emptyList(),
    val tasks: List<AssistantTask> = emptyList(),
    val isGenerating: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null,
    val customApiKey: String = ""
)

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val chatDao = database.chatDao()
    private val taskDao = database.taskDao()

    val aiService = GeminiAssistantService(application)
    val voiceManager = DotsVoiceManager(application)

    private val _currentTab = MutableStateFlow(AssistantTab.CHAT)
    private val _isGenerating = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _customApiKey = MutableStateFlow("")

    val uiState: StateFlow<AssistantUiState> = combine(
        _currentTab,
        chatDao.getAllMessages(),
        taskDao.getAllTasks(),
        _isGenerating,
        voiceManager.isSpeaking
    ) { tab, msgs, tasks, generating, speaking ->
        AssistantUiState(
            currentTab = tab,
            messages = msgs,
            tasks = tasks,
            isGenerating = generating,
            isSpeaking = speaking,
            errorMessage = _errorMessage.value,
            customApiKey = _customApiKey.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AssistantUiState()
    )

    fun selectTab(tab: AssistantTab) {
        _currentTab.value = tab
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        aiService.customApiKey = key
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _errorMessage.value = null

            // 1. Insert user message
            val userMsg = ChatMessage(role = "USER", content = trimmed)
            chatDao.insertMessage(userMsg)

            _isGenerating.value = true

            // 2. Build history
            val history = uiState.value.messages.takeLast(6).map { it.role to it.content }

            // 3. Call AI
            val result = aiService.generateResponse(trimmed, history)
            _isGenerating.value = false

            result.onSuccess { responseText ->
                val assistantMsg = ChatMessage(role = "ASSISTANT", content = responseText)
                chatDao.insertMessage(assistantMsg)
            }.onFailure { ex ->
                val fallbackText = "Maaf, terjadi kendala koneksi ke server AI (${ex.localizedMessage ?: "Jaringan bermasalah"}). Coba periksa koneksi internet atau gunakan kunci API di Pengaturan."
                val assistantMsg = ChatMessage(role = "ASSISTANT", content = fallbackText)
                chatDao.insertMessage(assistantMsg)
                _errorMessage.value = ex.message
            }
        }
    }

    fun speakMessage(text: String) {
        if (voiceManager.isSpeaking.value) {
            voiceManager.stop()
        } else {
            voiceManager.speak(text)
        }
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    fun clearChat() {
        viewModelScope.launch {
            voiceManager.stop()
            chatDao.clearAllMessages()
        }
    }

    // Task Management
    fun addTask(title: String, category: String = "Umum", dueDate: String = "") {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(
                AssistantTask(
                    title = trimmed,
                    category = category,
                    dueDate = dueDate
                )
            )
        }
    }

    fun toggleTask(task: AssistantTask) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: AssistantTask) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            taskDao.clearCompletedTasks()
        }
    }

    // Quick Tool Prompts
    fun executeToolPrompt(toolType: String, input: String) {
        val prompt = when (toolType) {
            "SUMMARIZE" -> "Tolong buatkan ringkasan yang padat, jelas, dan berpoin dari teks berikut:\n\n$input"
            "DRAFT_EMAIL" -> "Tolong buatkan draf pesan/email yang sopan, rapi, dan profesional dengan topik/maksud berikut:\n\n$input"
            "BRAINSTORM" -> "Berikan 5 sampai 7 ide kreatif, unik, dan solutif untuk:\n\n$input"
            "TRANSLATE" -> "Terjemahkan teks berikut ke dalam Bahasa Inggris dan Bahasa Indonesia yang alami serta jelaskan nuansa artinya:\n\n$input"
            "FIX_GRAMMAR" -> "Koreksi tata bahasa, tanda baca, dan ejaan teks berikut agar lebih enak dibaca dan profesional:\n\n$input"
            else -> input
        }
        _currentTab.value = AssistantTab.CHAT
        sendMessage(prompt)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
