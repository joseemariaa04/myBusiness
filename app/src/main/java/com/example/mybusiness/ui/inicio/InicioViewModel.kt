package com.example.mybusiness.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val estado: StateFlow<EstadoInicio> = combine(
        repository.todosLosIngresos,
        repository.todosLosGastos,
        repository.todoElHistorial
    ) { ingresos, gastos, historial ->
        val totalIngresos = ingresos.sumOf { it.cantidad }
        val totalGastos = gastos.sumOf { it.cantidad }
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

    fun cerrarMesActual(nombre: String) {
        val estadoActual = estado.value
        viewModelScope.launch {
            repository.iniciarNuevoMes(
                nombre = nombre,
                ingresos = estadoActual.ingresosTotales,
                gastos = estadoActual.gastosTotales
            )
        }
    }
}
