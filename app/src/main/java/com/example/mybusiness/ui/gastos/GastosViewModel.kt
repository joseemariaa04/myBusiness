package com.example.mybusiness.ui.gastos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Gasto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GastosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val gastos: StateFlow<List<Gasto>> = repository.todosLosGastos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarGasto(concepto: String, monto: Double, fecha: Long, categoria: String) {
        viewModelScope.launch {
            repository.insertarGasto(Gasto(concepto = concepto, monto = monto, fecha = fecha, categoria = categoria))
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            repository.eliminarGasto(gasto)
        }
    }
}
