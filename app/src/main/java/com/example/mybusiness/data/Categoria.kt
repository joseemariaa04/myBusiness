package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val esIngreso: Boolean, // true para Ingreso, false para Gasto
    val iconoNombre: String // Nombre del icono para identificarlo
)
