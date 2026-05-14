package com.example.mybusiness.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("https://openrouter.ai/api/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") token: String,
        @Header("HTTP-Referer") referer: String = "https://mybusiness.example.com",
        @Header("X-Title") title: String = "myBusiness App",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}
