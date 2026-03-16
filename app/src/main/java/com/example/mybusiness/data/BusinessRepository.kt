package com.example.mybusiness.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class BusinessRepository private constructor(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val trabajadorDao = database.trabajadorDao()
    private val clienteDao = database.clienteDao()
    private val gastoDao = database.gastoDao()
    private val ingresoDao = database.ingresoDao()
    private val historialMesDao = database.historialMesDao()
    private val categoriaDao = database.categoriaDao()

    // Trabajadores
    val todosLosTrabajadores: Flow<List<Trabajador>> = trabajadorDao.obtenerTodos()
    suspend fun insertarTrabajador(trabajador: Trabajador) = trabajadorDao.insertar(trabajador)
    suspend fun eliminarTrabajador(trabajador: Trabajador) = trabajadorDao.eliminar(trabajador)

    // Clientes
    val todosLosClientes: Flow<List<Cliente>> = clienteDao.obtenerTodos()
    suspend fun insertarCliente(cliente: Cliente) = clienteDao.insertar(cliente)
    suspend fun eliminarCliente(cliente: Cliente) = clienteDao.eliminar(cliente)

    // Gastos
    val todosLosGastos: Flow<List<Gasto>> = gastoDao.obtenerTodos()
    suspend fun insertarGasto(gasto: Gasto) = gastoDao.insertar(gasto)
    suspend fun eliminarGasto(gasto: Gasto) = gastoDao.eliminar(gasto)

    // Ingresos
    val todosLosIngresos: Flow<List<Ingreso>> = ingresoDao.obtenerTodos()
    suspend fun insertarIngreso(ingreso: Ingreso) = ingresoDao.insertar(ingreso)
    suspend fun eliminarIngreso(ingreso: Ingreso) = ingresoDao.eliminar(ingreso)

    // Historial
    val todoElHistorial: Flow<List<HistorialMes>> = historialMesDao.obtenerTodoElHistorial()

    // Categorías
    val todasLasCategorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()
    suspend fun insertarCategoria(categoria: Categoria) = categoriaDao.insertar(categoria)
    suspend fun eliminarCategoria(categoria: Categoria) = categoriaDao.eliminar(categoria)

    suspend fun iniciarNuevoMes(nombre: String, ingresos: Double, gastos: Double) {
        database.withTransaction {
            val historial = HistorialMes(
                nombreMes = nombre,
                ingresosTotales = ingresos,
                gastosTotales = gastos,
                beneficio = ingresos - gastos,
                fecha = System.currentTimeMillis()
            )
            historialMesDao.insertar(historial)
            //Sentencias SQL manuales para limpiar las tablas
            database.openHelper.writableDatabase.execSQL("DELETE FROM trabajadores")
            database.openHelper.writableDatabase.execSQL("DELETE FROM clientes")
            database.openHelper.writableDatabase.execSQL("DELETE FROM gastos")
            database.openHelper.writableDatabase.execSQL("DELETE FROM ingresos")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BusinessRepository? = null

        fun getInstance(context: Context): BusinessRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = BusinessRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
