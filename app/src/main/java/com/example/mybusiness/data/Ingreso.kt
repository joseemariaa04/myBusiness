package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Esta clase define qué información guardamos de cada ingreso (dinero que entra)
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
    val concepto: String, // ¿Por qué ha entrado el dinero?
    val cantidad: Double, // ¿Cuánto dinero ha sido?
    val fecha: Long, // ¿Cuándo ocurrió?
    val categoria: String = "Venta", // ¿Qué tipo de ingreso es?
    val clienteId: Int? = null, // Si lo ha pagado un cliente específico
    val mesId: Int? = null, // Para saber a qué mes del historial pertenece
    val esFijo: Boolean = false // Si es un ingreso que se repite todos los meses
)

