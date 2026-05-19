package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// El DAO para gestionar a los TRABAJADORES.
@Dao
interface TrabajadorDao {
    // Lista a todos los empleados de la empresa por orden alfabético.
    @Query("SELECT * FROM trabajadores ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Trabajador>>

    // Saca la lista completa de trabajadores una sola vez.
    @Query("SELECT * FROM trabajadores")
    suspend fun obtenerTodosUnaVez(): List<Trabajador>

    // Busca un trabajador específico usando su ID.
    @Query("SELECT * FROM trabajadores WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Trabajador>

    // Saca solo a los trabajadores que están marcados como "en activo".
    @Query("SELECT * FROM trabajadores WHERE activo = 1")
    suspend fun obtenerActivos(): List<Trabajador>

    // Añade un nuevo trabajador a la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(trabajador: Trabajador)

    // Modifica los datos (como el sueldo o el teléfono) de un trabajador.
    @Update
    suspend fun actualizar(trabajador: Trabajador)

    // Borra a un trabajador de la base de datos.
    @Delete
    suspend fun eliminar(trabajador: Trabajador)
}
