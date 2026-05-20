package com.example.mybusiness.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.api.OpenRouterMessage
import com.example.mybusiness.data.api.OpenRouterRequest
import com.example.mybusiness.data.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.mybusiness.BuildConfig

// Esta clase guarda los mensajitos que vemos en la pantalla
data class MensajeChat(
    val quienEscribe: String, // Puede ser "user" (nosotros), "assistant" (la IA) o "system" (la configuración)
    val textoMensaje: String,
    val esError: Boolean = false
)

class ChatViewModel : ViewModel() {

    // Esta es la llave secreta para entrar a OpenRouter. Ahora se obtiene de BuildConfig.
    private val CLAVE_API = BuildConfig.OPENROUTER_API_KEY
    
    // Aquí elegimos qué cerebro de IA queremos usar. Owl Alpha es muy listo.
    private val ID_MODELO = "openrouter/owl-alpha"

    // Esta lista guarda los mensajes que se pintan en la pantalla del móvil
    private val _estadoMensajes = MutableStateFlow<List<MensajeChat>>(emptyList())
    val estadoMensajes = _estadoMensajes.asStateFlow()

    // Esto nos dice si la IA está pensando ahora mismo para poner el circulito de carga
    private val _estaCargando = MutableStateFlow(false)
    val estaCargando = _estaCargando.asStateFlow()

    // Esta lista es secreta, es la que le mandamos a la IA para que sepa de qué estamos hablando
    private val historialDeMensajes = mutableListOf<OpenRouterMessage>()

    /**
     * Inicializa el chat con un mensaje de bienvenida si la lista está vacía.
     * Se llama desde la UI porque necesitamos el string traducido.
     */
    fun inicializarChat(mensajeBienvenida: String) {
        if (_estadoMensajes.value.isEmpty()) {
            _estadoMensajes.value = listOf(MensajeChat("assistant", mensajeBienvenida))
        }
    }

    // Esta función se activa cuando pulsamos el botón de enviar
    fun enviarMensaje(textoDelUsuario: String, contextoNegocio: String = "", promptSistema: String = "Eres Buzzy, el asistente inteligente de myBusiness. Ayuda con contabilidad y gestión de forma breve. IMPORTANTE:\n" +
            "Responde siempre en texto plano.\n" +
            "No uses Markdown.\n" +
            "No uses **negritas**, *, #, -, tablas con |, backticks\n" +
            "No formatees código.\n" +
            "Devuelve únicamente texto simple y limpio., puedes usar emojis si quieres, responde en el idioma que te pregunten") {
        // Si no han escrito nada o ya estamos esperando respuesta, no hacemos nada
        if (textoDelUsuario.isBlank() || _estaCargando.value) return

        // Si es el primer mensaje, le decimos a la IA quién es y qué datos tiene la empresa
        if (historialDeMensajes.isEmpty()) {
            historialDeMensajes.add(OpenRouterMessage(rol = "system", contenido = "$promptSistema Contexto actual: $contextoNegocio"))
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
        _estadoMensajes.update { it + MensajeChat("user", textoDelUsuario) }
        
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
                
                // Miramos qué nos ha respondido la IA
                respuesta.opciones.firstOrNull()?.mensaje?.let { mensajeIA ->
                    historialDeMensajes.add(mensajeIA)
                    
                    // A veces la IA devuelve lo que piensa en 'razonamiento' y otras en 'contenido'
                    val textoParaMostrar = mensajeIA.razonamiento ?: mensajeIA.contenido ?: ""

                    _estadoMensajes.update { it + MensajeChat("assistant", textoParaMostrar) }
                }
            } catch (error: Exception) {
                // Si algo sale mal (como que no haya Internet), ponemos un mensaje de aviso
                val detalleError = when (error) {
                    is retrofit2.HttpException -> {
                        if (error.code() == 429) "Servicio saturado. Por favor, espera 15 segundos."
                        else "Error ${error.code()}"
                    }
                    else -> "Error de conexión"
                }
                _estadoMensajes.update { it + MensajeChat("assistant", "Sistema: $detalleError", esError = true) }
            } finally {
                _estaCargando.value = false
            }
        }
    }
}
