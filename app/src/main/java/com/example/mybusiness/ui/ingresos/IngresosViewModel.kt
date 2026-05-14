package com.example.mybusiness.ui.ingresos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Ingreso
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IngresosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val ingresos: StateFlow<List<Ingreso>> = repository.todosLosIngresos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarIngreso(concepto: String, monto: Double, fecha: Long, categoria: String, esFijo: Boolean = false) {
        viewModelScope.launch {
            repository.insertarIngreso(
                Ingreso(
                    concepto = concepto,
                    cantidad = monto,
                    fecha = fecha,
                    categoria = categoria,
                    esFijo = esFijo
                )
            )
        }
    }

    fun eliminarIngreso(ingreso: Ingreso) {
        viewModelScope.launch {
            repository.eliminarIngreso(ingreso)
        }
    }
}
