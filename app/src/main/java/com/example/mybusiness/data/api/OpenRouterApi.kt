package com.example.mybusiness.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// Esta interfaz define cómo nos comunicamos con la web de OpenRouter
interface OpenRouterApi {
    // Esta función envía nuestra pregunta a la IA y espera la respuesta
    @POST("chat/completions")
    suspend fun obtenerRespuestaDeLaIA(
        @Header("Authorization") tokenAutorizacion: String, // La llave de entrada
        @Header("HTTP-Referer") sitioWeb: String = "https://mybusiness.example.com",
        @Header("X-Title") nombreApp: String = "myBusiness App",
        @Body solicitud: OpenRouterRequest // Lo que le preguntamos
    ): OpenRouterResponse // Lo que nos responde
}
