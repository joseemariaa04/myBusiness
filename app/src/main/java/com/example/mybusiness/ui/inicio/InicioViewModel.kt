package com.example.mybusiness.ui.inicio

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.mybusiness.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

import com.example.mybusiness.data.HistorialMes

data class EstadoInicio(
    val beneficioMensual: Double = 0.0,
    val ingresosTotales: Double = 0.0,
    val gastosTotales: Double = 0.0,
    val historial: List<HistorialMes> = emptyList()
)

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos.asSharedFlow()

    init {
        comprobarCierreMesAutomatico()
    }

    private var procesandoCierre = false

    fun comprobarCierreMesAutomatico() {
        if (procesandoCierre) return
        procesandoCierre = true
        
        viewModelScope.launch {
            try {
                val sdfStorage = SimpleDateFormat("yyyy-MM", Locale.US)
                val sdfDisplay = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                
                val hoy = Calendar.getInstance()
                val mesActualStorage = sdfStorage.format(hoy.time)
                
                val ultimoMesCerradoStorage = prefs.getString("ultimo_mes_cerrado", "") ?: ""

                // Si es la primera vez que se usa la app, marcamos el mes actual como "visto"
                if (ultimoMesCerradoStorage.isEmpty()) {
                    prefs.edit().putString("ultimo_mes_cerrado", mesActualStorage).apply()
                    return@launch
                }

                // Si el mes guardado es distinto al actual (incluye cambio de año), cerramos el periodo anterior
                if (ultimoMesCerradoStorage != mesActualStorage) {
                    val nombreMesACerrar = try {
                        val fechaGuardada = sdfStorage.parse(ultimoMesCerradoStorage)
                        fechaGuardada?.let { 
                            sdfDisplay.format(it).replaceFirstChar { c -> c.uppercase() } 
                        } ?: ultimoMesCerradoStorage
                    } catch (e: Exception) {
                        ultimoMesCerradoStorage
                    }

                    // Ejecutamos el cierre con los datos acumulados hasta el momento
                    repository.cerrarMesAutomaticamente(nombreMesACerrar)
                    
                    // Actualizamos a la marca del mes actual para que no vuelva a saltar hasta el próximo mes
                    prefs.edit().putString("ultimo_mes_cerrado", mesActualStorage).apply()
                    _eventos.emit(getApplication<Application>().getString(R.string.month_closed_success, nombreMesACerrar))
                }
            } finally {
                procesandoCierre = false
            }
        }
    }

    val estado: StateFlow<EstadoInicio> = combine(
        repository.todosLosIngresos,
        repository.todosLosGastos,
        repository.todoElHistorial,
        repository.todosLosTrabajadores
    ) { ingresos, gastos, historial, trabajadores ->
        val salarioActivos = trabajadores.filter { it.activo }.sumOf { it.salario }
        val totalIngresos = ingresos.sumOf { it.cantidad }
        val totalGastos = gastos.sumOf { it.cantidad } + salarioActivos

        EstadoInicio(
            beneficioMensual = totalIngresos - totalGastos,
            ingresosTotales = totalIngresos,
            gastosTotales = totalGastos,
            historial = historial
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EstadoInicio()
    )
}
