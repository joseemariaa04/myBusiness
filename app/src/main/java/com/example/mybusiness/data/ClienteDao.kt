package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes")
    suspend fun obtenerTodosUnaVez(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Cliente>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: Cliente)

    @Update
    suspend fun actualizar(cliente: Cliente)

    @Delete
    suspend fun eliminar(cliente: Cliente)
}
