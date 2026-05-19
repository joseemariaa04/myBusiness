package com.example.mybusiness.ui.categorias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import com.example.mybusiness.data.Categoria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Este controlador gestiona las etiquetas o categorías (ej. "Comida", "Venta")
class CategoriasViewModel(application: Application) : AndroidViewModel(application) {

    // El repositorio es nuestro almacén de datos
    private val repository = BusinessRepository.getInstance(application)

    // Lista de todas las categorías disponibles para usar
    val categorias: StateFlow<List<Categoria>> = repository.todasLasCategorias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para crear una nueva etiqueta (ej. "Transporte")
    fun agregarCategoria(nombre: String, esIngreso: Boolean, iconoNombre: String) {
        viewModelScope.launch {
            repository.insertarCategoria(Categoria(nombre = nombre, esIngreso = esIngreso, iconoNombre = iconoNombre))
        }
    }

    // Función para borrar una etiqueta que ya no necesitemos
    fun eliminarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            repository.eliminarCategoria(categoria)
        }
    }
}

