package com.example.mybusiness.ui.trabajadores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Trabajador
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Este componente sirve para gestionar a las personas que trabajan en la empresa
class TrabajadoresViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    // El repositorio es como el archivo donde guardamos los contratos y datos de los empleados
    private val repositorio = BusinessRepository.getInstance(aplicacion)

    // Esta es la lista de todos los trabajadores que tenemos apuntados
    val listaDeTrabajadores: StateFlow<List<Trabajador>> = repositorio.todosLosTrabajadores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para contratar (añadir) a un nuevo trabajador
    fun contratarTrabajador(nombre: String, puesto: String, telefono: String, email: String, salario: Double, activo: Boolean) {
        viewModelScope.launch {
            repositorio.insertarTrabajador(
                Trabajador(
                    nombre = nombre,
                    puesto = puesto,
                    telefono = telefono,
                    email = email,
                    salario = salario,
                    activo = activo
                )
            )
        }
    }

    // Función para cambiar los datos de un trabajador (ej. subirle el sueldo o cambiar su puesto)
    fun actualizarDatosTrabajador(trabajador: Trabajador) {
        viewModelScope.launch {
            repositorio.insertarTrabajador(trabajador)
        }
    }

    // Función para despedir o borrar a un trabajador de la lista
    fun borrarTrabajador(trabajador: Trabajador) {
        viewModelScope.launch {
            repositorio.eliminarTrabajador(trabajador)
        }
    }
}
