package com.example.mybusiness.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.api.OpenRouterMessage
import com.example.mybusiness.data.api.OpenRouterRequest
import com.example.mybusiness.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Esta clase guarda los mensajitos que vemos en la pantalla
data class MensajeChat(
    val quienEscribe: String, // Puede ser "user" (nosotros), "assistant" (la IA) o "system" (la configuración)
    val textoMensaje: String
)

class ChatViewModel : ViewModel() {

    // Esta es la llave secreta para entrar a OpenRouter. No deberíamos enseñarla mucho.
    private val CLAVE_API = "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"
    
    // Aquí elegimos qué cerebro de IA queremos usar. Owl Alpha es muy listo.
    private val ID_MODELO = "openrouter/owl-alpha"

    // Esta lista guarda los mensajes que se pintan en la pantalla del móvil
    private val _estadoMensajes = MutableStateFlow<List<MensajeChat>>(listOf(
        MensajeChat("assistant", "¡Hola! Soy tu asistente de myBusiness. ¿En qué puedo ayudarte hoy?")
    ))
    val estadoMensajes = _estadoMensajes.asStateFlow()

    // Esto nos dice si la IA está pensando ahora mismo para poner el circulito de carga
    private val _estaCargando = MutableStateFlow(false)
    val estaCargando = _estaCargando.asStateFlow()

    // Esta lista es secreta, es la que le mandamos a la IA para que sepa de qué estamos hablando
    private val historialDeMensajes = mutableListOf<OpenRouterMessage>()

    // Esta función se activa cuando pulsamos el botón de enviar
    fun enviarMensaje(textoDelUsuario: String, contextoNegocio: String = "") {
        // Si no han escrito nada o ya estamos esperando respuesta, no hacemos nada
        if (textoDelUsuario.isBlank() || _estaCargando.value) return

        // Si es el primer mensaje, le decimos a la IA quién es y qué datos tiene la empresa
        if (historialDeMensajes.isEmpty()) {
            historialDeMensajes.add(OpenRouterMessage(rol = "system", contenido = "Eres el asistente inteligente de myBusiness. Ayuda con contabilidad y gestión de forma breve. Responde siempre en español. Contexto actual: $contextoNegocio"))
        }

        // Si llevamos mucho rato hablando, borramos mensajes viejos para que la IA no se líe (y para que no nos cobren de más)
        if (historialDeMensajes.size > 10) {
            val configuracionSistema = historialDeMensajes.first()
            val mensajesRecientes = historialDeMensajes.takeLast(5)
            historialDeMensajes.clear()
            historialDeMensajes.add(configuracionSistema)
            historialDeMensajes.addAll(mensajesRecientes)
        }

        // Añadimos lo que ha escrito el usuario a la pantalla
        val listaActual = _estadoMensajes.value.toMutableList()
        listaActual.add(MensajeChat("user", textoDelUsuario))
        _estadoMensajes.value = listaActual
        
        // También lo guardamos en el historial secreto para la IA
        historialDeMensajes.add(OpenRouterMessage(rol = "user", contenido = textoDelUsuario))
        _estaCargando.value = true

        // Aquí le pedimos a Internet que le mande todo esto a la IA
        viewModelScope.launch {
            try {
                val peticion = OpenRouterRequest(
                    modelo = ID_MODELO,
                    mensajes = historialDeMensajes
                )
                // Usamos Retrofit para hacer la llamada a la API
                val respuesta = RetrofitClient.conexionApiChat.obtenerRespuestaDeLaIA(tokenAutorizacion = "Bearer $CLAVE_API", solicitud = peticion)
                
                _estaCargando.value = false
                // Miramos qué nos ha respondido la IA
                respuesta.opciones.firstOrNull()?.mensaje?.let { mensajeIA ->
                    historialDeMensajes.add(mensajeIA)
                    val listaActualizada = _estadoMensajes.value.toMutableList()
                    
                    // A veces la IA devuelve lo que piensa en 'razonamiento' y otras en 'contenido'
                    val textoParaMostrar = mensajeIA.razonamiento ?: mensajeIA.contenido ?: ""

                    listaActualizada.add(MensajeChat("assistant", textoParaMostrar))
                    _estadoMensajes.value = listaActualizada
                }
            } catch (error: Exception) {
                _estaCargando.value = false
                // Si algo sale mal (como que no haya Internet), ponemos un mensaje de aviso
                val detalleError = when (error) {
                    is retrofit2.HttpException -> {
                        if (error.code() == 429) "Servicio saturado. Por favor, espera 15 segundos."
                        else "Error ${error.code()}"
                    }
                    else -> "Error de conexión"
                }
                val listaConError = _estadoMensajes.value.toMutableList()
                listaConError.add(MensajeChat("assistant", "Sistema: $detalleError"))
                _estadoMensajes.value = listaConError
            }
        }
    }
}
