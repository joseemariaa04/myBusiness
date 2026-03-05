package com.example.mybusiness.ui.inicio

import com.example.mybusiness.data.HistorialMes

data class EstadoInicio(
    val beneficioMensual: Double = 0.0,
    val ingresosTotales: Double = 0.0,
    val gastosTotales: Double = 0.0,
    val historial: List<HistorialMes> = emptyList()
)
