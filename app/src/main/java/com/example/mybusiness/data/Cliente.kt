package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Esta clase define qué información guardamos de cada cliente
@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Un número único para cada cliente
    val nombre: String, // Nombre de la persona de contacto
    val empresa: String,
    val telefono: String,
    val email: String
)

