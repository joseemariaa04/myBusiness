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

    fun obtenerIngresosPorMes(mesId: Int): Flow<List<Ingreso>> = ingresoDao.obtenerPorMes(mesId)
    fun obtenerGastosPorMes(mesId: Int): Flow<List<Gasto>> = gastoDao.obtenerPorMes(mesId)

    // Categorías
    val todasLasCategorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()
    suspend fun insertarCategoria(categoria: Categoria) = categoriaDao.insertar(categoria)
    suspend fun eliminarCategoria(categoria: Categoria) = categoriaDao.eliminar(categoria)

    suspend fun iniciarNuevoMes(nombre: String, ingresos: Double, gastos: Double): Result<Unit> {
        return try {
            if (nombre.isBlank()) return Result.failure(Exception("El nombre del mes no puede estar vacío"))
            
            database.withTransaction {
                val historial = HistorialMes(
                    nombreMes = nombre,
                    ingresosTotales = ingresos,
                    gastosTotales = gastos,
                    beneficio = ingresos - gastos,
                    fecha = System.currentTimeMillis()
                )
                val mesId = historialMesDao.insertar(historial).toInt()
                
                // Asociamos los registros actuales NO FIJOS al mes que cerramos
                ingresoDao.asociarMesId(mesId)
                gastoDao.asociarMesId(mesId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cerrarMesAutomaticamente(nombreMes: String) {
        // Obtenemos los totales actuales directamente de la base de datos
        val ingresosList = ingresoDao.obtenerTodosUnaVez()
        val gastosList = gastoDao.obtenerTodosUnaVez()
        val trabajadores = trabajadorDao.obtenerTodosUnaVez()

        val salarioActivos = trabajadores.filter { it.activo }.sumOf { it.salario }
        val totalIngresos = ingresosList.sumOf { it.cantidad }
        val totalGastos = gastosList.sumOf { it.cantidad } + salarioActivos

        iniciarNuevoMes(nombreMes, totalIngresos, totalGastos)
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
