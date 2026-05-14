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

class TrabajadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val trabajadores: StateFlow<List<Trabajador>> = repository.todosLosTrabajadores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarTrabajador(nombre: String, puesto: String, telefono: String, email: String, salario: Double, activo: Boolean) {
        viewModelScope.launch {
            repository.insertarTrabajador(
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

    fun actualizarTrabajador(trabajador: Trabajador) {
        viewModelScope.launch {
            repository.insertarTrabajador(trabajador)
        }
    }

    fun eliminarTrabajador(trabajador: Trabajador) {
        viewModelScope.launch {
            repository.eliminarTrabajador(trabajador)
        }
    }
}
