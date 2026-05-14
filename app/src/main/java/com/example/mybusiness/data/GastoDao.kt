package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gastos WHERE mesId IS NULL ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<Gasto>>

    @Query("SELECT * FROM gastos WHERE mesId IS NULL")
    suspend fun obtenerTodosUnaVez(): List<Gasto>

    @Query("SELECT * FROM gastos WHERE mesId = :mesId ORDER BY fecha DESC")
    fun obtenerPorMes(mesId: Int): Flow<List<Gasto>>

    @Query("UPDATE gastos SET mesId = :mesId WHERE mesId IS NULL AND esFijo = 0")
    suspend fun asociarMesId(mesId: Int)

    @Query("SELECT * FROM gastos WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Gasto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: Gasto)

    @Update
    suspend fun actualizar(gasto: Gasto)

    @Delete
    suspend fun eliminar(gasto: Gasto)
}
