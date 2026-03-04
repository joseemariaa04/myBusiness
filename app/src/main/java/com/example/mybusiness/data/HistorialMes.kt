package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_meses")
data class HistorialMes(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreMes: String,
    val ingresosTotales: Double,
    val gastosTotales: Double,
    val beneficio: Double,
    val fecha: Long
)
