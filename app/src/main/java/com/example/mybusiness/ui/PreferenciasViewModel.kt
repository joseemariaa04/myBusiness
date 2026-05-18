package com.example.mybusiness.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

// Este componente se encarga de guardar y recordar las opciones que elige el usuario en los ajustes
class PreferenciasViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    // Es como una cajita donde guardamos datos que no queremos que se borren al cerrar la app
    private val cajitaDeDatos = aplicacion.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Aquí guardamos el nombre de la empresa. Si no hay ninguno, ponemos "My Business"
    var nombreEmpresa by mutableStateOf(cajitaDeDatos.getString("nombre_empresa", "My Business") ?: "My Business")
        private set

    // El dibujo de la moneda que usamos (como € o $)
    var simboloMoneda by mutableStateOf(cajitaDeDatos.getString("simbolo_moneda", "€") ?: "€")
        private set

    // Si la pantalla debe estar en modo oscuro o claro
    var modoOscuro by mutableStateOf(
        if (cajitaDeDatos.contains("modo_oscuro")) cajitaDeDatos.getBoolean("modo_oscuro", false) else null
    )
        private set

    // Si queremos que el mes se cierre solo cuando pase el tiempo
    var cierreAutomatico by mutableStateOf(cajitaDeDatos.getBoolean("cierre_automatico", false))
        private set

    // Una pequeña explicación de a qué se dedica la empresa
    var descripcionEmpresa by mutableStateOf(cajitaDeDatos.getString("descripcion_empresa", "") ?: "")
        private set

    // Función para cambiar el nombre de la empresa y que se quede guardado
    fun guardarNombreEmpresa(nuevoNombre: String) {
        nombreEmpresa = nuevoNombre
        cajitaDeDatos.edit().putString("nombre_empresa", nuevoNombre).apply()
    }

    // Función para cambiar el símbolo del dinero y que se guarde
    fun guardarSimboloMoneda(nuevoSimbolo: String) {
        simboloMoneda = nuevoSimbolo
        cajitaDeDatos.edit().putString("simbolo_moneda", nuevoSimbolo).apply()
    }

    // Función para poner o quitar el modo oscuro
    fun guardarModoOscuro(activado: Boolean?) {
        modoOscuro = activado
        val editor = cajitaDeDatos.edit()
        if (activado == null) {
            editor.remove("modo_oscuro") // Si es null, que el móvil decida solo
        } else {
            editor.putBoolean("modo_oscuro", activado)
        }
        editor.apply()
    }

    // Función para activar o desactivar que los meses se cierren solos
    fun guardarCierreAutomatico(activado: Boolean) {
        cierreAutomatico = activado
        cajitaDeDatos.edit().putBoolean("cierre_automatico", activado).apply()
    }

    // Función para guardar lo que hace la empresa
    fun guardarDescripcionEmpresa(nuevaDescripcion: String) {
        descripcionEmpresa = nuevaDescripcion
        cajitaDeDatos.edit().putString("descripcion_empresa", nuevaDescripcion).apply()
    }
}
