package com.example.mybusiness

import android.app.Application
import com.example.mybusiness.data.BusinessRepository
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.mybusiness.workers.CierreMesWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyBusinessApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializamos el repositorio como un singleton al arrancar la app
        BusinessRepository.getInstance(this)
        configurarCierreAutomatico()
    }

    private fun configurarCierreAutomatico() {
        // Programamos una tarea periódica que se ejecute cada día
        // El Worker mismo verificará si es el día 1 y si la opción está activa
        val request = PeriodicWorkRequestBuilder<CierreMesWorker>(1, TimeUnit.DAYS)
            .addTag("cierre_mes_job")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CierreMesAutomatico",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
