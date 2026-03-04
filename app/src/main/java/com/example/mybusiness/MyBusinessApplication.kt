package com.example.mybusiness

import android.app.Application
import com.example.mybusiness.data.BusinessRepository

class MyBusinessApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializamos el repositorio como un singleton al arrancar la app
        BusinessRepository.getInstance(this)
    }
}
