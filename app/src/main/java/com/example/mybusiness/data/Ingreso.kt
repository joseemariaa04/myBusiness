package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingresos",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = HistorialMes::class,
            parentColumns = ["id"],
            childColumns = ["mesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId"), Index("mesId")]
)
data class Ingreso(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concepto: String,
    val cantidad: Double,
    val fecha: Long,
    val categoria: String = "Venta",
    val clienteId: Int? = null,
    val mesId: Int? = null,
    val esFijo: Boolean = false
)
