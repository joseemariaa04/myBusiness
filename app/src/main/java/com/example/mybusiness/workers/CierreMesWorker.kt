package com.example.mybusiness.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mybusiness.data.BusinessRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CierreMesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val autoCierre = prefs.getBoolean("cierre_automatico", false)

        if (!autoCierre) return Result.success()

        // Solo actuamos si es el día 1 del mes
        val calendarActual = java.util.Calendar.getInstance()
        if (calendarActual.get(java.util.Calendar.DAY_OF_MONTH) != 1) {
            return Result.success()
        }

        val repository = BusinessRepository.getInstance(applicationContext)
        
        // Obtenemos los datos actuales (esto es simplificado, en un escenario real 
        // necesitaríamos acceder al estado actual o calcularlo desde la DB directamente)
        // Pero para el worker, necesitamos los totales del mes que termina.
        
        // Calculamos el nombre del mes que acaba de terminar (el anterior al día 1 actual)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, -1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val nombreMes = sdf.format(calendar.time).replaceFirstChar { it.uppercase() }

        // En el worker no tenemos acceso al StateFlow del ViewModel, así que lo calculamos
        // de forma independiente o lo pasamos como input (pero el auto-cierre es desatendido).
        // Lo ideal es que el repositorio tenga un método para cerrar el mes calculando él mismo los totales.
        
        return try {
            repository.cerrarMesAutomaticamente(nombreMes)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
