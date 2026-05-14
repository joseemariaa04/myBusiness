package com.example.mybusiness.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.api.OpenRouterMessage
import com.example.mybusiness.data.api.OpenRouterRequest
import com.example.mybusiness.data.api.ReasoningConfig
import com.example.mybusiness.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String, // "user", "assistant" or "system"
    val message: String
)

class ChatViewModel : ViewModel() {

    // NOTA: Genera una clave nueva si esta ha sido expuesta.
    private val API_KEY = "Bearer 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"
    
    // Modelo experimental con razonamiento (Reasoning)
    private val MODEL_ID = "inclusionai/ring-2.6-1t:free"

    private val _uiState = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "¡Hola! Soy tu asistente de myBusiness. ¿En qué puedo ayudarte hoy?")
    ))
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val messageHistory = mutableListOf(
        OpenRouterMessage("system", "Eres el asistente inteligente de myBusiness. Ayuda con contabilidad y gestión de forma breve. Responde siempre en español."),
        OpenRouterMessage("assistant", "¡Hola! Soy tu asistente de myBusiness. ¿En qué puedo ayudarte hoy?")
    )

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isLoading.value) return

        // Mantener el historial corto para evitar errores 429 por exceso de tokens
        if (messageHistory.size > 10) {
            val system = messageHistory.first()
            val recent = messageHistory.takeLast(5)
            messageHistory.clear()
            messageHistory.add(system)
            messageHistory.addAll(recent)
        }

        val currentUiMessages = _uiState.value.toMutableList()
        currentUiMessages.add(ChatMessage("user", userText))
        _uiState.value = currentUiMessages
        
        messageHistory.add(OpenRouterMessage("user", userText))
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val request = OpenRouterRequest(
                    model = MODEL_ID,
                    messages = messageHistory,
                    reasoning = ReasoningConfig(enabled = true)
                )
                val response = RetrofitClient.openRouterApi.getChatCompletion(token = API_KEY, request = request)
                
                _isLoading.value = false
                response.choices.firstOrNull()?.message?.let { 
                    messageHistory.add(it)
                    val updatedUiMessages = _uiState.value.toMutableList()
                    // Si content es nulo, usamos reasoning_details para la UI
                    val textToShow = it.content ?: it.reasoningDetails ?: ""
                    updatedUiMessages.add(ChatMessage("assistant", textToShow.toString()))
                    _uiState.value = updatedUiMessages
                }
            } catch (e: Exception) {
                _isLoading.value = false
                val errorDetail = when (e) {
                    is retrofit2.HttpException -> {
                        if (e.code() == 429) "Servicio saturado. Por favor, espera 15 segundos."
                        else "Error ${e.code()}"
                    }
                    else -> "Error de conexión"
                }
                val updated = _uiState.value.toMutableList()
                updated.add(ChatMessage("assistant", "Sistema: $errorDetail"))
                _uiState.value = updated
            }
        }
    }
}
