package com.example.mybusiness.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

// El Repositorio es como el encargado del almacén de datos de toda la aplicación.
// Se encarga de guardar y pedir información a la base de datos de manera organizada.
class BusinessRepository private constructor(context: Context) {
    
    // Conectamos con la base de datos y sus diferentes apartados (DAOs)
    private val database = AppDatabase.getDatabase(context)
    private val trabajadorDao = database.trabajadorDao()
    private val clienteDao = database.clienteDao()
    private val gastoDao = database.gastoDao()
    private val ingresoDao = database.ingresoDao()
    private val historialMesDao = database.historialMesDao()
    private val categoriaDao = database.categoriaDao()

    // --- TRABAJADORES ---
    // Nos da la lista de empleados que se actualiza sola si hay cambios
    val todosLosTrabajadores: Flow<List<Trabajador>> = trabajadorDao.obtenerTodos()
    // Guarda a alguien nuevo o actualiza uno que ya existe
    suspend fun insertarTrabajador(trabajador: Trabajador) = trabajadorDao.insertar(trabajador)
    // Quita a alguien de la lista para siempre
    suspend fun eliminarTrabajador(trabajador: Trabajador) = trabajadorDao.eliminar(trabajador)

    // --- CLIENTES ---
    // Nos da la lista de clientes completa
    val todosLosClientes: Flow<List<Cliente>> = clienteDao.obtenerTodos()
    suspend fun insertarCliente(cliente: Cliente) = clienteDao.insertar(cliente)
    suspend fun eliminarCliente(cliente: Cliente) = clienteDao.eliminar(cliente)

    // --- GASTOS (Dinero que sale) ---
    val todosLosGastos: Flow<List<Gasto>> = gastoDao.obtenerTodos()
    suspend fun insertarGasto(gasto: Gasto) = gastoDao.insertar(gasto)
    suspend fun eliminarGasto(gasto: Gasto) = gastoDao.eliminar(gasto)

    // --- INGRESOS (Dinero que entra) ---
    val todosLosIngresos: Flow<List<Ingreso>> = ingresoDao.obtenerTodos()
    suspend fun insertarIngreso(ingreso: Ingreso) = ingresoDao.insertar(ingreso)
    suspend fun eliminarIngreso(ingreso: Ingreso) = ingresoDao.eliminar(ingreso)

    // --- HISTORIAL Y RESÚMENES ---
    // Ver lo que pasó en meses anteriores
    val todoElHistorial: Flow<List<HistorialMes>> = historialMesDao.obtenerTodoElHistorial()

    fun obtenerIngresosPorMes(mesId: Int): Flow<List<Ingreso>> = ingresoDao.obtenerPorMes(mesId)
    fun obtenerGastosPorMes(mesId: Int): Flow<List<Gasto>> = gastoDao.obtenerPorMes(mesId)

    // --- CATEGORÍAS ---
    // Las etiquetas para clasificar gastos e ingresos (ej. "Comida", "Ventas")
    val todasLasCategorias: Flow<List<Categoria>> = categoriaDao.obtenerTodas()
    suspend fun insertarCategoria(categoria: Categoria) = categoriaDao.insertar(categoria)
    suspend fun eliminarCategoria(categoria: Categoria) = categoriaDao.eliminar(categoria)

    // Esta función sirve para cerrar las cuentas del mes y guardarlas en el historial
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

    // Calcula automáticamente los totales de ingresos y gastos antes de cerrar el mes
    suspend fun cerrarMesAutomaticamente(nombreMes: String) {
        val ingresosList = ingresoDao.obtenerTodosUnaVez()
        val gastosList = gastoDao.obtenerTodosUnaVez()
        val trabajadoresActivos = trabajadorDao.obtenerActivos()

        val salarioActivos = trabajadoresActivos.sumOf { it.salario }
        val totalIngresos = ingresosList.sumOf { it.cantidad }
        val totalGastos = gastosList.sumOf { it.cantidad } + salarioActivos

        iniciarNuevoMes(nombreMes, totalIngresos, totalGastos)
    }

    // Da un resumen rápido de cómo va el negocio ahora mismo
    suspend fun obtenerResumenActual(): Map<String, Any> {
        val ingresos = ingresoDao.obtenerTodosUnaVez().sumOf { it.cantidad }
        val gastos = gastoDao.obtenerTodosUnaVez().sumOf { it.cantidad }
        val numTrabajadores = trabajadorDao.obtenerActivos().size
        val numClientes = clienteDao.obtenerTodosUnaVez().size
        
        return mapOf(
            "ingresos" to ingresos,
            "gastos" to gastos,
            "trabajadores" to numTrabajadores,
            "clientes" to numClientes
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: BusinessRepository? = null

        // Esta parte asegura que solo haya UN encargado (repositorio) en toda la aplicación
        fun getInstance(context: Context): BusinessRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = BusinessRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}

