package com.example.mybusiness.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.api.OpenRouterMessage
import com.example.mybusiness.data.api.OpenRouterRequest
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
    private val API_KEY = "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"
    
    // Modelo Owl Alpha para razonamiento avanzado
    private val MODEL_ID = "openrouter/owl-alpha"

    private val _uiState = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "¡Hola! Soy tu asistente de myBusiness. ¿En qué puedo ayudarte hoy?")
    ))
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val messageHistory = mutableListOf<OpenRouterMessage>()

    fun sendMessage(userText: String, context: String = "") {
        if (userText.isBlank() || _isLoading.value) return

        if (messageHistory.isEmpty()) {
            messageHistory.add(OpenRouterMessage("system", "Eres el asistente inteligente de myBusiness. Ayuda con contabilidad y gestión de forma breve. Responde siempre en español. Contexto actual: $context"))
        }

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
                    messages = messageHistory
                )
                val response = RetrofitClient.openRouterApi.getChatCompletion(token = "Bearer $API_KEY", request = request)
                
                _isLoading.value = false
                response.choices.firstOrNull()?.message?.let { msg ->
                    messageHistory.add(msg)
                    val updatedUiMessages = _uiState.value.toMutableList()
                    
                    // El modelo Owl Alpha puede devolver el razonamiento en el campo 'reasoning'
                    val textToShow = msg.reasoning ?: msg.content ?: ""

                    updatedUiMessages.add(ChatMessage("assistant", textToShow))
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
