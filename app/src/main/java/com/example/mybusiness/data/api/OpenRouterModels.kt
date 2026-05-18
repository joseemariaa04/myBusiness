package com.example.mybusiness.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val reasoning: ReasoningConfig? = null
)

@JsonClass(generateAdapter = true)
data class ReasoningConfig(
    val enabled: Boolean
)

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    val role: String,
    val content: String? = null,
    val reasoning: String? = null,
    @Json(name = "reasoning_details") val reasoningDetails: List<ReasoningDetail>? = null
)

@JsonClass(generateAdapter = true)
data class ReasoningDetail(
    val type: String?,
    val text: String?
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    val message: OpenRouterMessage
)
