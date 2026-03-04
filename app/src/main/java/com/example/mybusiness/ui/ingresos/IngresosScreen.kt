package com.example.mybusiness.ui.ingresos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoreHoriz
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
import com.example.mybusiness.data.Ingreso
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IngresosScreen(viewModel: IngresosViewModel) {
    val ingresos by viewModel.ingresos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Ingreso")
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
                text = "Ingresos recientes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (ingresos.isEmpty()) {
                EstadoVacio(
                    mensaje = "No hay ingresos",
                    subMensaje = "Registra tu primer ingreso pulsando el botón +",
                    icono = Icons.AutoMirrored.Filled.TrendingUp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(ingresos) { index, ingreso ->
                        AnimacionEntradaLista(index = index) {
                            IngresoCard(
                                ingreso = ingreso,
                                onDelete = { viewModel.eliminarIngreso(ingreso) }
                            )
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            AgregarIngresoDialog(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { concepto, monto, categoria ->
                    viewModel.agregarIngreso(concepto, monto, System.currentTimeMillis(), categoria)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun IngresoCard(ingreso: Ingreso, onDelete: () -> Unit) {
    val icono = when (ingreso.categoria) {
        "Venta" -> Icons.Default.Sell
        "Servicio" -> Icons.Default.Build
        else -> Icons.Default.MoreHoriz
    }
    
    val colorIcono = when (ingreso.categoria) {
        "Venta" -> Color(0xFF4CAF50)
        "Servicio" -> Color(0xFF2196F3)
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
                    text = ingreso.concepto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(ingreso.fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+€${String.format(Locale.getDefault(), "%,.2f", ingreso.monto)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = ingreso.categoria,
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
fun AgregarIngresoDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var concepto by remember { mutableStateOf("") }
    var montoStr by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Venta") }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Ingreso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") })
                TextField(value = montoStr, onValueChange = { montoStr = it }, label = { Text("Monto") })
                Text("Categoría:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = categoria == "Venta", onClick = { categoria = "Venta" })
                    Text("Venta")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = categoria == "Servicio", onClick = { categoria = "Servicio" })
                    Text("Servicio")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = categoria == "Otros", onClick = { categoria = "Otros" })
                    Text("Otros")
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
