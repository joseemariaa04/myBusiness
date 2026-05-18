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

// Este componente maneja todo el dinero que entra en el negocio
class IngresosViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    // El repositorio nos ayuda a guardar y leer los ingresos en la base de datos
    private val repositorio = BusinessRepository.getInstance(aplicacion)

    // Esta es la lista de todos los ingresos que hay ahora mismo
    val listaDeIngresos: StateFlow<List<Ingreso>> = repositorio.todosLosIngresos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para apuntar un nuevo dinero que ha entrado
    fun apuntarNuevoIngreso(concepto: String, cantidad: Double, fecha: Long, categoria: String, esFijo: Boolean = false) {
        viewModelScope.launch {
            repositorio.insertarIngreso(
                Ingreso(
                    concepto = concepto,
                    cantidad = cantidad,
                    fecha = fecha,
                    categoria = categoria,
                    esFijo = esFijo
                )
            )
        }
    }

    // Función para borrar un ingreso si nos hemos equivocado
    fun borrarIngreso(ingreso: Ingreso) {
        viewModelScope.launch {
            repositorio.eliminarIngreso(ingreso)
        }
    }
}
