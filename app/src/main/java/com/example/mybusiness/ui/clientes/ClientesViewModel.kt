package com.example.mybusiness.ui.clientes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Cliente
import com.example.mybusiness.data.ClienteConTotal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Este controlador se encarga de conectar la pantalla de clientes con la base de datos
class ClientesViewModel(application: Application) : AndroidViewModel(application) {

    // Accedemos al almacén de datos (repositorio)
    private val repository = BusinessRepository.getInstance(application)

    // Aquí guardamos la lista de todos los clientes para que la pantalla la vea
    val clientes: StateFlow<List<Cliente>> = repository.todosLosClientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nueva lista que incluye cuánto dinero ha gastado cada cliente en total
    val clientesConTotal: StateFlow<List<ClienteConTotal>> = repository.todosLosClientesConTotal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para guardar un nuevo cliente en la base de datos
    fun agregarCliente(nombre: String, empresa: String, telefono: String, email: String) {
        viewModelScope.launch {
            repository.insertarCliente(Cliente(nombre = nombre, empresa = empresa, telefono = telefono, email = email))
        }
    }

    // Función para quitar a un cliente de nuestra lista
    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            repository.eliminarCliente(cliente)
        }
    }
}

