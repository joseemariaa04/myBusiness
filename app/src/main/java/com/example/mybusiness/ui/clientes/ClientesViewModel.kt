package com.example.mybusiness.ui.clientes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Cliente
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val clientes: StateFlow<List<Cliente>> = repository.todosLosClientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarCliente(nombre: String, empresa: String, telefono: String, email: String) {
        viewModelScope.launch {
            repository.insertarCliente(Cliente(nombre = nombre, empresa = empresa, telefono = telefono, email = email))
        }
    }

    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            repository.eliminarCliente(cliente)
        }
    }
}
