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

class CategoriasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)

    val categorias: StateFlow<List<Categoria>> = repository.todasLasCategorias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarCategoria(nombre: String, esIngreso: Boolean, iconoNombre: String) {
        viewModelScope.launch {
            repository.insertarCategoria(Categoria(nombre = nombre, esIngreso = esIngreso, iconoNombre = iconoNombre))
        }
    }

    fun eliminarCategoria(categoria: Categoria) {
        viewModelScope.launch {
            repository.eliminarCategoria(categoria)
        }
    }
}
