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

// Esta clase guarda toda la información que se muestra en la pantalla principal
data class EstadoInicio(
    val beneficioMensual: Double = 0.0,
    val ingresosTotales: Double = 0.0,
    val gastosTotales: Double = 0.0,
    val historial: List<HistorialMes> = emptyList(),
    val variacionBeneficio: Double? = null,
    val variacionIngresos: Double? = null,
    val variacionGastos: Double? = null,
    val desglosePorCategoriasIngresos: Map<String, Double> = emptyMap(),
    val desglosePorCategoriasGastos: Map<String, Double> = emptyMap()
)

class InicioViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    // El repositorio es como el bibliotecario que sabe dónde están guardados los datos en la base de datos
    private val repositorio = BusinessRepository.getInstance(aplicacion)
    // La cajita de preferencias para guardar cosas pequeñas como fechas
    private val cajitaDePreferencias = aplicacion.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Esto sirve para mandar avisos a la pantalla (como "Mes cerrado con éxito")
    private val _eventosDeAviso = MutableSharedFlow<String>()
    val eventosDeAviso = _eventosDeAviso.asSharedFlow()

    init {
        // Nada más empezar, miramos si toca cerrar el mes porque ha pasado el tiempo
        comprobarSiTocaCerrarElMes()
    }

    private var estaCerrandoElMesActualmente = false

    fun comprobarSiTocaCerrarElMes() {
        if (estaCerrandoElMesActualmente) return
        estaCerrandoElMesActualmente = true
        
        viewModelScope.launch {
            try {
                // Formatos para entender las fechas
                val formatoFechaGuardada = SimpleDateFormat("yyyy-MM", Locale.US)
                val formatoParaMostrarUsuario = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                
                val hoy = Calendar.getInstance()
                val mesActualTexto = formatoFechaGuardada.format(hoy.time)
                
                val ultimoMesQueCerramos = cajitaDePreferencias.getString("ultimo_mes_cerrado", "") ?: ""

                // Si es la primera vez que abres la app, simplemente anotamos que este es el mes actual
                if (ultimoMesQueCerramos.isEmpty()) {
                    cajitaDePreferencias.edit().putString("ultimo_mes_cerrado", mesActualTexto).apply()
                    return@launch
                }

                // Si el mes que tenemos guardado no es el mismo que el de hoy, es que hemos cambiado de mes
                if (ultimoMesQueCerramos != mesActualTexto) {
                    val nombreDelMesViejo = try {
                        val fechaLeida = formatoFechaGuardada.parse(ultimoMesQueCerramos)
                        fechaLeida?.let { 
                            formatoParaMostrarUsuario.format(it).replaceFirstChar { letra -> letra.uppercase() } 
                        } ?: ultimoMesQueCerramos
                    } catch (e: Exception) {
                        ultimoMesQueCerramos
                    }

                    // Le decimos al repositorio que guarde los datos del mes que acaba de terminar
                    repositorio.cerrarMesAutomaticamente(nombreDelMesViejo)
                    
                    // Apuntamos que ya hemos cerrado este mes para no volver a hacerlo hasta el que viene
                    cajitaDePreferencias.edit().putString("ultimo_mes_cerrado", mesActualTexto).apply()
                    // Avisamos al usuario con un mensajito
                    _eventosDeAviso.emit(getApplication<Application>().getString(R.string.mes_cerrado_exito, nombreDelMesViejo))
                }
            } finally {
                estaCerrandoElMesActualmente = false
            }
        }
    }

    // Esta es la parte más lista: junta todos los datos (ingresos, gastos, historial) y calcula los totales
    val estado: StateFlow<EstadoInicio> = combine(
        repositorio.todosLosIngresos,
        repositorio.todosLosGastos,
        repositorio.todoElHistorial,
        repositorio.todosLosTrabajadores
    ) { listaIngresos, listaGastos, listaHistorial, listaTrabajadores ->
        // Calculamos cuánto pagamos en sueldos sumando lo de los trabajadores activos
        val totalSueldosActivos = listaTrabajadores.filter { it.activo }.sumOf { it.salario }
        // Sumamos todos los ingresos del mes
        val sumaIngresos = listaIngresos.sumOf { it.cantidad }
        // Sumamos todos los gastos y le añadimos los sueldos
        val sumaGastos = listaGastos.sumOf { it.cantidad } + totalSueldosActivos
        // El beneficio es lo que queda después de pagar todo
        val beneficioNeto = sumaIngresos - sumaGastos

        // Buscamos el último mes cerrado para ver si hemos mejorado o empeorado
        val datosMesAnterior = listaHistorial.firstOrNull()
        
        // Esta función calcula el porcentaje de cambio entre este mes y el anterior
        fun calcularPorcentajeDeCambio(valorHoy: Double, valorAyer: Double?): Double? {
            if (valorAyer == null || valorAyer == 0.0) return null
            return ((valorHoy - valorAyer) / Math.abs(valorAyer)) * 100
        }

        // Agrupamos los ingresos por su categoría para saber de dónde viene el dinero
        val mapaIngresosCategorias: Map<String, Double> = listaIngresos.groupBy { it.categoria }
            .mapValues { grupo -> grupo.value.sumOf { it.cantidad } }
            .filter { it.value > 0.0 }

        // Hacemos lo mismo con los gastos
        val mapaGastosCategoriasTemporal: MutableMap<String, Double> = listaGastos.groupBy { it.categoria }
            .mapValues { grupo -> grupo.value.sumOf { it.cantidad } }
            .toMutableMap()
        
        // No nos olvidamos de meter los sueldos en el desglose de gastos
        if (totalSueldosActivos > 0.0) {
            val palabraSueldos = getApplication<Application>().getString(R.string.salario_mensual)
            mapaGastosCategoriasTemporal[palabraSueldos] = (mapaGastosCategoriasTemporal[palabraSueldos] ?: 0.0) + totalSueldosActivos
        }
        val mapaGastosFinal = mapaGastosCategoriasTemporal.filter { it.value > 0.0 }

        // Metemos todos los cálculos en el "paquete" de EstadoInicio para enviarlo a la pantalla
        EstadoInicio(
            beneficioMensual = beneficioNeto,
            ingresosTotales = sumaIngresos,
            gastosTotales = sumaGastos,
            historial = listaHistorial,
            variacionBeneficio = calcularPorcentajeDeCambio(beneficioNeto, datosMesAnterior?.beneficio),
            variacionIngresos = calcularPorcentajeDeCambio(sumaIngresos, datosMesAnterior?.ingresosTotales),
            variacionGastos = calcularPorcentajeDeCambio(sumaGastos, datosMesAnterior?.gastosTotales),
            desglosePorCategoriasIngresos = mapaIngresosCategorias,
            desglosePorCategoriasGastos = mapaGastosFinal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EstadoInicio()
    )
}
