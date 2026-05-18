package com.example.mybusiness.ui.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.res.stringResource
import com.example.mybusiness.R
import com.example.mybusiness.ui.PreferenciasViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Esta pantalla muestra todos los movimientos (dinero que entra y sale) de un mes pasado
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesScreen(
    mesId: Int,
    nombreMes: String,
    controladorDetalle: DetalleMesViewModel,
    onVolver: () -> Unit,
    controladorDePreferencias: PreferenciasViewModel = viewModel()
) {
    // Sacamos las listas de ingresos y gastos de ese mes
    val listaIngresos by controladorDetalle.obtenerIngresosMes(mesId).collectAsState()
    val listaGastos by controladorDetalle.obtenerGastosMes(mesId).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.month_detail_title, nombreMes)) },
                navigationIcon = {
                    // Botón para ir atrás y volver a la pantalla de inicio
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.movements_summary), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Lista con todos los movimientos del mes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(stringResource(R.string.income), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
                // Si no hubo ingresos, avisamos
                if (listaIngresos.isEmpty()) {
                    item { Text(stringResource(R.string.no_income_this_month), fontSize = 14.sp, color = Color.Gray) }
                } else {
                    // Pintamos cada ingreso
                    items(listaIngresos) { ingreso ->
                        TarjetaMovimiento(
                            concepto = ingreso.concepto,
                            cantidad = ingreso.cantidad,
                            fecha = ingreso.fecha,
                            esUnIngreso = true,
                            pref = controladorDePreferencias
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.expenses), fontWeight = FontWeight.SemiBold, color = Color(0xFFFF5252))
                }
                // Si no hubo gastos, avisamos
                if (listaGastos.isEmpty()) {
                    item { Text(stringResource(R.string.no_expenses_this_month), fontSize = 14.sp, color = Color.Gray) }
                } else {
                    // Pintamos cada gasto
                    items(listaGastos) { gasto ->
                        TarjetaMovimiento(
                            concepto = gasto.concepto,
                            cantidad = gasto.cantidad,
                            fecha = gasto.fecha,
                            esUnIngreso = false,
                            pref = controladorDePreferencias
                        )
                    }
                }
            }
        }
    }
}

// Así es como se ve una tarjetita de un movimiento (un ingreso o un gasto)
@Composable
fun TarjetaMovimiento(
    concepto: String, 
    cantidad: Double, 
    fecha: Long, 
    esUnIngreso: Boolean, 
    pref: PreferenciasViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(concepto, fontWeight = FontWeight.Medium)
                // Ponemos la fecha en formato día/mes/año
                Text(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            // El dinero se ve en verde con un "+" si entra, y en rojo con un "-" si sale
            Text(
                text = "${if (esUnIngreso) "+" else "-"}${pref.simboloMoneda}${String.format("%.2f", cantidad)}",
                color = if (esUnIngreso) MaterialTheme.colorScheme.primary else Color(0xFFFF5252),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
