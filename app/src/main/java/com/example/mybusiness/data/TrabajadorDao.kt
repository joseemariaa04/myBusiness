package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrabajadorDao {
    @Query("SELECT * FROM trabajadores ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Trabajador>>

    @Query("SELECT * FROM trabajadores WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Trabajador>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(trabajador: Trabajador)

    @Update
    suspend fun actualizar(trabajador: Trabajador)

    @Delete
    suspend fun eliminar(trabajador: Trabajador)
}
