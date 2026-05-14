package com.example.mybusiness.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialMesDao {
    @Query("SELECT * FROM historial_meses ORDER BY fecha DESC")
    fun obtenerTodoElHistorial(): Flow<List<HistorialMes>>

    @Insert
    suspend fun insertar(historialMes: HistorialMes): Long
}
