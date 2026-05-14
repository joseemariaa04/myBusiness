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

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos.asSharedFlow()

    init {
        comprobarCierreMesAutomatico()
    }

    fun comprobarCierreMesAutomatico() {
        if (!prefs.getBoolean("cierre_automatico", false)) return

        viewModelScope.launch {
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

    fun cerrarMesActual() {
        val estadoActual = estado.value
        viewModelScope.launch {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val nombre = sdf.format(Date()).replaceFirstChar { it.uppercase() }

            val resultado = repository.iniciarNuevoMes(
                nombre = nombre,
                ingresos = estadoActual.ingresosTotales,
                gastos = estadoActual.gastosTotales
            )
            if (resultado.isSuccess) {
                // Al cerrar manualmente, también actualizamos la marca de tiempo para evitar que el auto-cierre salte
                val sdfStorage = SimpleDateFormat("yyyy-MM", Locale.US)
                prefs.edit().putString("ultimo_mes_cerrado", sdfStorage.format(Date())).apply()

                _eventos.emit(getApplication<Application>().getString(R.string.month_closed_success, nombre))
            } else {
                _eventos.emit(getApplication<Application>().getString(R.string.month_close_error, resultado.exceptionOrNull()?.message ?: ""))
            }
        }
    }
}
