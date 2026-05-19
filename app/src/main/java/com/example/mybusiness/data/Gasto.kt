package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Esta clase define qué información guardamos de cada gasto (dinero que sale)
@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = HistorialMes::class,
            parentColumns = ["id"],
            childColumns = ["mesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mesId")]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concepto: String, // ¿En qué hemos gastado el dinero?
    val cantidad: Double, // ¿Cuánto dinero ha sido?
    val fecha: Long, // ¿Cuándo ocurrió?
    val categoria: String, // ¿Qué tipo de gasto es? (ej. Luz, Alquiler)
    val mesId: Int? = null, // Para saber a qué mes del historial pertenece
    val esFijo: Boolean = false // Si es un gasto que se repite todos los meses
)

