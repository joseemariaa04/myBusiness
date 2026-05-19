package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Esta clase define qué información guardamos de cada empleado
@Entity(tableName = "trabajadores")
data class Trabajador(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Un número único para cada empleado
    val nombre: String,
    val puesto: String,
    val telefono: String,
    val email: String,
    val salario: Double = 0.0,
    val activo: Boolean = true // Para saber si sigue trabajando con nosotros
)

