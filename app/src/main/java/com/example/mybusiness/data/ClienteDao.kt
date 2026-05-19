package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// El DAO para gestionar los CLIENTES.
@Dao
interface ClienteDao {
    // Saca todos nuestros clientes ordenados por su nombre (de la A a la Z).
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Cliente>>

    // Saca todos los clientes de golpe sin quedarse vigilando cambios.
    @Query("SELECT * FROM clientes")
    suspend fun obtenerTodosUnaVez(): List<Cliente>

    // Busca un cliente concreto por su número de ID.
    @Query("SELECT * FROM clientes WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Cliente>

    // Guarda los datos de un cliente nuevo.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: Cliente)

    // Actualiza la información de un cliente que ya teníamos.
    @Update
    suspend fun actualizar(cliente: Cliente)

    // Borra a un cliente de nuestra lista.
    @Delete
    suspend fun eliminar(cliente: Cliente)
}
