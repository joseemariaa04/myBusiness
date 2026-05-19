package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// El DAO es como una lista de "instrucciones" que la base de datos sabe obedecer para manejar los INGRESOS.
@Dao
interface IngresoDao {
    // Busca todos los ingresos que hemos apuntado este mes (los que aún no están archivados).
    @Query("SELECT * FROM ingresos WHERE mesId IS NULL ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<Ingreso>>

    // Lo mismo que arriba, pero lo hace de una sola vez.
    @Query("SELECT * FROM ingresos WHERE mesId IS NULL")
    suspend fun obtenerTodosUnaVez(): List<Ingreso>

    // Busca los ingresos que pertenecen a un mes concreto del historial.
    @Query("SELECT * FROM ingresos WHERE mesId = :mesId ORDER BY fecha DESC")
    fun obtenerPorMes(mesId: Int): Flow<List<Ingreso>>

    // Cuando se acaba el mes, marcamos los ingresos variables con el ID del mes para guardarlos.
    @Query("UPDATE ingresos SET mesId = :mesId WHERE mesId IS NULL AND esFijo = 0")
    suspend fun asociarMesId(mesId: Int)

    // Busca un ingreso específico si sabemos su ID.
    @Query("SELECT * FROM ingresos WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Ingreso>

    // Guarda un ingreso nuevo o sobrescribe uno que ya exista.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ingreso: Ingreso)

    // Cambia los datos de un ingreso guardado.
    @Update
    suspend fun actualizar(ingreso: Ingreso)

    // Borra un ingreso de la base de datos para siempre.
    @Delete
    suspend fun eliminar(ingreso: Ingreso)
}
