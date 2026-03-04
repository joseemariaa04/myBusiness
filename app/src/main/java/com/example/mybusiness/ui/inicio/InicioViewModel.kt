package com.example.mybusiness.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.HistorialMes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InicioUiState(
    val beneficioMensual: Double = 0.0,
    val ingresosTotales: Double = 0.0,
    val gastosTotales: Double = 0.0,
    val historial: List<HistorialMes> = emptyList()
)

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val uiState: StateFlow<InicioUiState> = combine(
        repository.todosLosIngresos,
        repository.todosLosGastos,
        repository.todoElHistorial
    ) { ingresos, gastos, historial ->
        val totalIngresos = ingresos.sumOf { it.cantidad }
        val totalGastos = gastos.sumOf { it.cantidad }
        InicioUiState(
            beneficioMensual = totalIngresos - totalGastos,
            ingresosTotales = totalIngresos,
            gastosTotales = totalGastos,
            historial = historial
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InicioUiState()
    )

    fun cerrarMesActual(nombre: String) {
        val currentState = uiState.value
        viewModelScope.launch {
            repository.iniciarNuevoMes(
                nombre = nombre,
                ingresos = currentState.ingresosTotales,
                gastos = currentState.gastosTotales
            )
        }
    }
}
