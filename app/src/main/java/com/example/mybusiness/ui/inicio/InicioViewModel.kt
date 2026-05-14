package com.example.mybusiness.ui.inicio

import android.app.Application
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

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos.asSharedFlow()

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
            // Generar nombre automático (ej. Enero 2024)
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val nombre = sdf.format(Date()).replaceFirstChar { it.uppercase() }

            val resultado = repository.iniciarNuevoMes(
                nombre = nombre,
                ingresos = estadoActual.ingresosTotales,
                gastos = estadoActual.gastosTotales
            )
            if (resultado.isSuccess) {
                _eventos.emit(getApplication<Application>().getString(R.string.month_closed_success, nombre))
            } else {
                _eventos.emit(getApplication<Application>().getString(R.string.month_close_error, resultado.exceptionOrNull()?.message ?: ""))
            }
        }
    }
}
