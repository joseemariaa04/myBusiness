package com.example.mybusiness.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// Este objeto es como el "cartero" que se encarga de enviar y recibir cosas de internet
object RetrofitClient {
    // La dirección de la página web a la que vamos a llamar
    private const val DIRECCION_BASE = "https://openrouter.ai/api/v1/"

    // Esta función es un poco especial: sirve para que el móvil confíe en la conexión
    // aunque sea un poco "insegura" (se usa mucho cuando estamos probando cosas)
    private fun configurarClienteSeguro(): OkHttpClient {
        val interceptorLog = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val gestorConfianza = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val contextoSSL = SSLContext.getInstance("TLS")
        contextoSSL.init(null, gestorConfianza, SecureRandom())
        
        return OkHttpClient.Builder()
            .sslSocketFactory(contextoSSL.socketFactory, gestorConfianza[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(interceptorLog)
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    // Moshi es una herramienta que traduce el texto raro que manda la web (JSON) a cosas que entiende Kotlin
    private val traductorMoshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Configuramos Retrofit, que es el motor principal para las llamadas a internet
    private val motorRetrofit = Retrofit.Builder()
        .baseUrl(DIRECCION_BASE)
        .client(configurarClienteSeguro())
        .addConverterFactory(MoshiConverterFactory.create(traductorMoshi))
        .build()

    // Aquí creamos la conexión final con la API de OpenRouter
    val conexionApiChat: OpenRouterApi = motorRetrofit.create(OpenRouterApi::class.java)
}
