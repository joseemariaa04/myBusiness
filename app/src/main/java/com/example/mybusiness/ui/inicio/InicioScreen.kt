package com.example.mybusiness.ui.inicio

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybusiness.data.HistorialMes
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InicioScreen(
    viewModel: InicioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoNuevoMes by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de Beneficio Mensual
        val esPositivo = uiState.beneficioMensual >= 0
        val colorBeneficio = if (esPositivo) MaterialTheme.colorScheme.primary else Color(0xFFFF5252)
        val iconoBeneficio = if (esPositivo) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Beneficio Mensual Actual",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = iconoBeneficio,
                        contentDescription = null,
                        tint = colorBeneficio
                    )
                }
                Text(
                    text = "€${String.format(Locale.getDefault(), "%,.2f", uiState.beneficioMensual)}",
                    color = colorBeneficio,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                val ratio = if (uiState.ingresosTotales > 0) {
                    (uiState.gastosTotales / uiState.ingresosTotales).toFloat().coerceIn(0f, 1f)
                } else if (uiState.gastosTotales > 0) 1f else 0f
                
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFFF5252),
                    trackColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResumenCard(
                titulo = "Ingresos",
                monto = uiState.ingresosTotales,
                icono = Icons.Default.ArrowUpward,
                colorIcono = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1.0f)
            )
            ResumenCard(
                titulo = "Gastos",
                monto = uiState.gastosTotales,
                icono = Icons.Default.ArrowDownward,
                colorIcono = Color(0xFFFF5252),
                modifier = Modifier.weight(1.0f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón NUEVO MES
        Button(
            onClick = { mostrarDialogoNuevoMes = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("NUEVO MES", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "HISTORIAL",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.historial) { mes ->
                HistorialCard(mes)
            }
        }
    }

    if (mostrarDialogoNuevoMes) {
        NuevoMesDialog(
            onDismiss = { mostrarDialogoNuevoMes = false },
            onConfirm = { nombre ->
                viewModel.cerrarMesActual(nombre)
                mostrarDialogoNuevoMes = false
            }
        )
    }
}

@Composable
fun HistorialCard(mes: HistorialMes) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mes.nombreMes,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(mes.fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "€${String.format(Locale.getDefault(), "%,.2f", mes.beneficio)}",
                    color = if (mes.beneficio >= 0) MaterialTheme.colorScheme.primary else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "I: €${String.format(Locale.getDefault(), "%,.0f", mes.ingresosTotales)} | G: €${String.format(Locale.getDefault(), "%,.0f", mes.gastosTotales)}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NuevoMesDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Mes Actual") },
        text = {
            Column {
                Text("Introduce el nombre del mes (ej. Enero 2024). Esto borrará todos los datos actuales y los guardará en el historial.")
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Mes") },
                    placeholder = { Text("Enero 2024") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (nombre.isBlank()) {
                    Toast.makeText(contexto, "El nombre del mes es obligatorio", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(nombre)
                }
            }) {
                Text("Confirmar y Reiniciar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ResumenCard(
    titulo: String,
    monto: Double,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titulo, color = Color.Gray, fontSize = 12.sp)
                Icon(imageVector = icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(16.dp))
            }
            Text(
                text = "€${String.format(Locale.getDefault(), "%,.2f", monto)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
