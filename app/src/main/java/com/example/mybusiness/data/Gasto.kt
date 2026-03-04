package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concepto: String,
    val cantidad: Double,
    val fecha: Long,
    val categoria: String
)
