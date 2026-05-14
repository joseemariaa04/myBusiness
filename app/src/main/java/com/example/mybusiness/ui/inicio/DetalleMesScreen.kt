package com.example.mybusiness.ui.inicio

import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesScreen(
    mesId: Int,
    nombreMes: String,
    viewModel: DetalleMesViewModel,
    onVolver: () -> Unit,
    prefViewModel: PreferenciasViewModel = viewModel()
) {
    val ingresos by viewModel.obtenerIngresosMes(mesId).collectAsState()
    val gastos by viewModel.obtenerGastosMes(mesId).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.month_detail_title, nombreMes)) },
                navigationIcon = {
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

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(stringResource(R.string.income), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
                if (ingresos.isEmpty()) {
                    item { Text(stringResource(R.string.no_income_this_month), fontSize = 14.sp, color = Color.Gray) }
                } else {
                    items(ingresos) { ingreso ->
                        MovimientoItem(
                            concepto = ingreso.concepto,
                            monto = ingreso.cantidad,
                            fecha = ingreso.fecha,
                            esIngreso = true
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.expenses), fontWeight = FontWeight.SemiBold, color = Color(0xFFFF5252))
                }
                if (gastos.isEmpty()) {
                    item { Text(stringResource(R.string.no_expenses_this_month), fontSize = 14.sp, color = Color.Gray) }
                } else {
                    items(gastos) { gasto ->
                        MovimientoItem(
                            concepto = gasto.concepto,
                            monto = gasto.cantidad,
                            fecha = gasto.fecha,
                            esIngreso = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovimientoItem(concepto: String, monto: Double, fecha: Long, esIngreso: Boolean, prefViewModel: PreferenciasViewModel = viewModel()) {
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
                Text(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "${if (esIngreso) "+" else "-"}${prefViewModel.simboloMoneda}${String.format("%.2f", monto)}",
                color = if (esIngreso) MaterialTheme.colorScheme.primary else Color(0xFFFF5252),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
