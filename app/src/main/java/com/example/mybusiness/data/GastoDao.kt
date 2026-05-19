package com.example.mybusiness.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// El DAO es como una lista de "instrucciones" que la base de datos sabe obedecer para manejar los GASTOS.
@Dao
interface GastoDao {
    // Busca todos los gastos que hemos apuntado este mes (los que aún no están archivados en el historial).
    @Query("SELECT * FROM gastos WHERE mesId IS NULL ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<Gasto>>

    // Lo mismo que arriba, pero lo hace de una sola vez en lugar de estar vigilando cambios.
    @Query("SELECT * FROM gastos WHERE mesId IS NULL")
    suspend fun obtenerTodosUnaVez(): List<Gasto>

    // Busca los gastos que pertenecen a un mes concreto que ya pasó, usando su número de identificación (ID).
    @Query("SELECT * FROM gastos WHERE mesId = :mesId ORDER BY fecha DESC")
    fun obtenerPorMes(mesId: Int): Flow<List<Gasto>>

    // Cuando se acaba el mes, marcamos todos los gastos variables con el ID del mes para guardarlos en el historial.
    @Query("UPDATE gastos SET mesId = :mesId WHERE mesId IS NULL AND esFijo = 0")
    suspend fun asociarMesId(mesId: Int)

    // Busca un gasto específico si sabemos su ID.
    @Query("SELECT * FROM gastos WHERE id = :id")
    fun obtenerPorId(id: Int): Flow<Gasto>

    // Guarda un gasto nuevo en la base de datos. Si ya existe, lo sobrescribe.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: Gasto)

    // Cambia los datos de un gasto que ya estaba guardado.
    @Update
    suspend fun actualizar(gasto: Gasto)

    // Borra un gasto de la base de datos para siempre.
    @Delete
    suspend fun eliminar(gasto: Gasto)
}
