package com.example.mybusiness.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val concepto: String,
    val cantidad: Double,
    val fecha: Long,
    val categoria: String,
    val mesId: Int? = null,
    val esFijo: Boolean = false
)
