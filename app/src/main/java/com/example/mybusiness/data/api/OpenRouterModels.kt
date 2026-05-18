package com.example.mybusiness.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Esta clase es para enviarle los datos a la IA cuando le pedimos algo
@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    @Json(name = "model") val modelo: String,
    @Json(name = "messages") val mensajes: List<OpenRouterMessage>,
    @Json(name = "reasoning") val razonamiento: ReasoningConfig? = null
)

// Configuración para que la IA piense un poco más antes de responder
@JsonClass(generateAdapter = true)
data class ReasoningConfig(
    @Json(name = "enabled") val activado: Boolean
)

// Cómo se ve un mensaje suelto (quién lo manda y qué dice)
@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    @Json(name = "role") val rol: String,
    @Json(name = "content") val contenido: String? = null,
    @Json(name = "reasoning") val razonamiento: String? = null,
    @Json(name = "reasoning_details") val detallesRazonamiento: List<ReasoningDetail>? = null
)

// Detalles de cómo ha ido pensando la IA paso a paso
@JsonClass(generateAdapter = true)
data class ReasoningDetail(
    @Json(name = "type") val tipo: String?,
    @Json(name = "text") val texto: String?
)

// Lo que nos devuelve la página de OpenRouter
@JsonClass(generateAdapter = true)
data class OpenRouterResponse(
    @Json(name = "choices") val opciones: List<OpenRouterChoice>
)

// Una de las posibles respuestas que nos da la IA
@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    @Json(name = "message") val mensaje: OpenRouterMessage
)
