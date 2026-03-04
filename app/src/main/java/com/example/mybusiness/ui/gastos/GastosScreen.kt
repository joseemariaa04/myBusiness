package com.example.mybusiness.ui.gastos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybusiness.data.Gasto
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GastosScreen(viewModel: GastosViewModel) {
    val gastos by viewModel.gastos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Gasto")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Gastos recientes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (gastos.isEmpty()) {
                EstadoVacio(
                    mensaje = "No hay gastos",
                    subMensaje = "Registra tu primer gasto pulsando el botón +",
                    icono = Icons.Default.MoneyOff
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(gastos) { index, gasto ->
                        AnimacionEntradaLista(index = index) {
                            GastoCard(
                                gasto = gasto,
                                onDelete = { viewModel.eliminarGasto(gasto) }
                            )
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            AgregarGastoDialog(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { concepto, monto, categoria ->
                    viewModel.agregarGasto(concepto, monto, System.currentTimeMillis(), categoria)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun GastoCard(gasto: Gasto, onDelete: () -> Unit) {
    val icono = when (gasto.categoria) {
        "Comida" -> Icons.Default.Restaurant
        "Transporte" -> Icons.Default.LocalGasStation
        "Entretenimiento" -> Icons.Default.Tv
        else -> Icons.Default.Restaurant
    }
    
    val colorIcono = when (gasto.categoria) {
        "Comida" -> Color(0xFFFB8C00)
        "Transporte" -> Color(0xFF1E88E5)
        "Entretenimiento" -> Color(0xFF8E24AA)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorIcono.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = colorIcono)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gasto.concepto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(gasto.fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-€${String.format(Locale.getDefault(), "%,.2f", gasto.monto)}",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = gasto.categoria,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun AgregarGastoDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var concepto by remember { mutableStateOf("") }
    var montoStr by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Comida") }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") })
                TextField(value = montoStr, onValueChange = { montoStr = it }, label = { Text("Monto") })
                Text("Categoría:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = categoria == "Comida", onClick = { categoria = "Comida" })
                    Text("Comida")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = categoria == "Transporte", onClick = { categoria = "Transporte" })
                    Text("Transporte")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = categoria == "Entretenimiento", onClick = { categoria = "Entretenimiento" })
                    Text("Entretenimiento")
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val monto = montoStr.toDoubleOrNull()
                if (concepto.isBlank() || montoStr.isBlank()) {
                    Toast.makeText(contexto, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                } else if (monto == null) {
                    Toast.makeText(contexto, "El monto debe ser un número válido", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(concepto, monto, categoria)
                }
            }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
