package com.example.mybusiness.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class PreferenciasViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var nombreEmpresa by mutableStateOf(prefs.getString("nombre_empresa", "My Business") ?: "My Business")
        private set

    var simboloMoneda by mutableStateOf(prefs.getString("simbolo_moneda", "€") ?: "€")
        private set

    var modoOscuro by mutableStateOf(
        if (prefs.contains("modo_oscuro")) prefs.getBoolean("modo_oscuro", false) else null
    )
        private set

    var cierreAutomatico by mutableStateOf(prefs.getBoolean("cierre_automatico", false))
        private set

    var descripcionEmpresa by mutableStateOf(prefs.getString("descripcion_empresa", "") ?: "")
        private set

    fun guardarNombreEmpresa(nuevoNombre: String) {
        nombreEmpresa = nuevoNombre
        prefs.edit().putString("nombre_empresa", nuevoNombre).apply()
    }

    fun guardarSimboloMoneda(nuevoSimbolo: String) {
        simboloMoneda = nuevoSimbolo
        prefs.edit().putString("simbolo_moneda", nuevoSimbolo).apply()
    }

    fun guardarModoOscuro(oscuro: Boolean?) {
        modoOscuro = oscuro
        val editor = prefs.edit()
        if (oscuro == null) {
            editor.remove("modo_oscuro")
        } else {
            editor.putBoolean("modo_oscuro", oscuro)
        }
        editor.apply()
    }

    fun guardarCierreAutomatico(activado: Boolean) {
        cierreAutomatico = activado
        prefs.edit().putBoolean("cierre_automatico", activado).apply()
    }

    fun guardarDescripcionEmpresa(nuevaDescripcion: String) {
        descripcionEmpresa = nuevaDescripcion
        prefs.edit().putString("descripcion_empresa", nuevaDescripcion).apply()
    }
}
