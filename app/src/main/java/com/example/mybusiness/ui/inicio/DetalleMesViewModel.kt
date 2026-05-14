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

class DetalleMesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BusinessRepository.getInstance(application)

    fun obtenerIngresosMes(mesId: Int): StateFlow<List<Ingreso>> {
        return repository.obtenerIngresosPorMes(mesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun obtenerGastosMes(mesId: Int): StateFlow<List<Gasto>> {
        return repository.obtenerGastosPorMes(mesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
