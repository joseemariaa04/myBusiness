package com.example.mybusiness.ui.inicio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Gasto
import com.example.mybusiness.data.Ingreso
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Este componente se encarga de buscar los detalles de un mes concreto que ya pasó
class DetalleMesViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    // El repositorio nos ayuda a sacar los datos de la base de datos
    private val repositorio = BusinessRepository.getInstance(aplicacion)

    // Buscamos todos los ingresos que hubo en ese mes usando su ID
    fun obtenerIngresosMes(mesId: Int): StateFlow<List<Ingreso>> {
        return repositorio.obtenerIngresosPorMes(mesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Buscamos todos los gastos que hubo en ese mes usando su ID
    fun obtenerGastosMes(mesId: Int): StateFlow<List<Gasto>> {
        return repositorio.obtenerGastosPorMes(mesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
