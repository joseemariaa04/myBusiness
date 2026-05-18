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

// Este componente controla todo el dinero que sale del negocio (compras, facturas, etc.)
class GastosViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    // El repositorio es el que sabe cómo guardar y borrar los gastos en la base de datos
    private val repositorio = BusinessRepository.getInstance(aplicacion)

    // Esta es la lista de todos los gastos que hemos apuntado
    val listaDeGastos: StateFlow<List<Gasto>> = repositorio.todosLosGastos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para añadir un nuevo gasto a la lista
    fun apuntarNuevoGasto(concepto: String, cantidad: Double, fecha: Long, categoria: String, esFijo: Boolean = false) {
        viewModelScope.launch {
            repositorio.insertarGasto(
                Gasto(
                    concepto = concepto,
                    cantidad = cantidad,
                    fecha = fecha,
                    categoria = categoria,
                    esFijo = esFijo
                )
            )
        }
    }

    // Función para borrar un gasto si ya no lo queremos
    fun borrarGasto(gasto: Gasto) {
        viewModelScope.launch {
            repositorio.eliminarGasto(gasto)
        }
    }
}
