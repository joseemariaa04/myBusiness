package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {
    @Query("SELECT * FROM ingresos WHERE mesId IS NULL ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<Ingreso>>

    @Query("SELECT * FROM ingresos WHERE mesId IS NULL")
    suspend fun obtenerTodosUnaVez(): List<Ingreso>

    @Query("SELECT * FROM ingresos WHERE mesId = :mesId ORDER BY fecha DESC")
    fun obtenerPorMes(mesId: Int): Flow<List<Ingreso>>

    @Query("UPDATE ingresos SET mesId = :mesId WHERE mesId IS NULL AND esFijo = 0")
    suspend fun asociarMesId(mesId: Int)

    @Query("SELECT * FROM ingresos WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Ingreso>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ingreso: Ingreso)

    @Update
    suspend fun actualizar(ingreso: Ingreso)

    @Delete
    suspend fun eliminar(ingreso: Ingreso)
}
