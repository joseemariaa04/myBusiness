package com.example.mybusiness.ui.inicio

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybusiness.data.BusinessRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.mybusiness.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

import com.example.mybusiness.data.HistorialMes

data class EstadoInicio(
    val beneficioMensual: Double = 0.0,
    val ingresosTotales: Double = 0.0,
    val gastosTotales: Double = 0.0,
    val historial: List<HistorialMes> = emptyList(),
    val varBeneficio: Double? = null,
    val varIngresos: Double? = null,
    val varGastos: Double? = null,
    val desgloseIngresos: Map<String, Double> = emptyMap(),
    val desgloseGastos: Map<String, Double> = emptyMap()
)

class InicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository.getInstance(application)
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos.asSharedFlow()

    init {
        comprobarCierreMesAutomatico()
    }

    private var procesandoCierre = false

    fun comprobarCierreMesAutomatico() {
        if (procesandoCierre) return
        procesandoCierre = true
        
        viewModelScope.launch {
            try {
                val sdfStorage = SimpleDateFormat("yyyy-MM", Locale.US)
                val sdfDisplay = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                
                val hoy = Calendar.getInstance()
                val mesActualStorage = sdfStorage.format(hoy.time)
                
                val ultimoMesCerradoStorage = prefs.getString("ultimo_mes_cerrado", "") ?: ""

                // Si es la primera vez que se usa la app, marcamos el mes actual como "visto"
                if (ultimoMesCerradoStorage.isEmpty()) {
                    prefs.edit().putString("ultimo_mes_cerrado", mesActualStorage).apply()
                    return@launch
                }

                // Si el mes guardado es distinto al actual (incluye cambio de año), cerramos el periodo anterior
                if (ultimoMesCerradoStorage != mesActualStorage) {
                    val nombreMesACerrar = try {
                        val fechaGuardada = sdfStorage.parse(ultimoMesCerradoStorage)
                        fechaGuardada?.let { 
                            sdfDisplay.format(it).replaceFirstChar { c -> c.uppercase() } 
                        } ?: ultimoMesCerradoStorage
                    } catch (e: Exception) {
                        ultimoMesCerradoStorage
                    }

                    // Ejecutamos el cierre con los datos acumulados hasta el momento
                    repository.cerrarMesAutomaticamente(nombreMesACerrar)
                    
                    // Actualizamos a la marca del mes actual para que no vuelva a saltar hasta el próximo mes
                    prefs.edit().putString("ultimo_mes_cerrado", mesActualStorage).apply()
                    _eventos.emit(getApplication<Application>().getString(R.string.month_closed_success, nombreMesACerrar))
                }
            } finally {
                procesandoCierre = false
            }
        }
    }

    val estado: StateFlow<EstadoInicio> = combine(
        repository.todosLosIngresos,
        repository.todosLosGastos,
        repository.todoElHistorial,
        repository.todosLosTrabajadores
    ) { ingresos, gastos, historial, trabajadores ->
        val salarioActivos = trabajadores.filter { it.activo }.sumOf { it.salario }
        val totalIngresos = ingresos.sumOf { it.cantidad }
        val totalGastos = gastos.sumOf { it.cantidad } + salarioActivos
        val beneficioActual = totalIngresos - totalGastos

        // Tomamos el mes anterior (el último cerrado) para comparar
        // El historial suele venir ordenado por fecha desc o asc, supongamos desc o que el primero es el más reciente
        // Según el código previo en InicioScreen: historial.reversed() se usa para la gráfica (orden cronológico)
        // Entonces historial.firstOrNull() debería ser el mes más reciente cerrado.
        val ultimoMes = historial.firstOrNull()
        
        fun calcularVariacion(actual: Double, anterior: Double?): Double? {
            if (anterior == null || anterior == 0.0) return null
            // Variación porcentual: ((actual - anterior) / anterior) * 100
            // Si el actual es 0 y el anterior es 100, la variación es -100%
            // Si el actual es 500 y el anterior es 100, la variación es +400%
            // Si el actual es 20 y el anterior es 100, la variación es -80%
            return ((actual - anterior) / Math.abs(anterior)) * 100
        }

        val desgloseIngresos: Map<String, Double> = ingresos.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.cantidad } }
            .filter { it.value > 0.0 }

        val desgloseGastosBase: MutableMap<String, Double> = gastos.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.cantidad } }
            .toMutableMap()
        
        if (salarioActivos > 0.0) {
            val etiquetaSueldos = getApplication<Application>().getString(R.string.salario)
            desgloseGastosBase[etiquetaSueldos] = (desgloseGastosBase[etiquetaSueldos] ?: 0.0) + salarioActivos
        }
        val desgloseGastos: Map<String, Double> = desgloseGastosBase.filter { it.value > 0.0 }

        EstadoInicio(
            beneficioMensual = beneficioActual,
            ingresosTotales = totalIngresos,
            gastosTotales = totalGastos,
            historial = historial,
            varBeneficio = calcularVariacion(beneficioActual, ultimoMes?.beneficio),
            varIngresos = calcularVariacion(totalIngresos, ultimoMes?.ingresosTotales),
            varGastos = calcularVariacion(totalGastos, ultimoMes?.gastosTotales),
            desgloseIngresos = desgloseIngresos,
            desgloseGastos = desgloseGastos
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EstadoInicio()
    )
}
