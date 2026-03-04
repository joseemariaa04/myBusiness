package com.example.mybusiness.ui.gastos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybusiness.data.Gasto
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio
import com.example.mybusiness.ui.categorias.CategoriasViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GastosScreen(viewModel: GastosViewModel) {
    val gastos by viewModel.gastos.collectAsState()
    val catViewModel: CategoriasViewModel = viewModel()
    val categorias by catViewModel.categorias.collectAsState()
    
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
            val gastosCategorias = categorias.filter { !it.esIngreso }
            AgregarGastoDialog(
                categoriasDisponibles = if (gastosCategorias.isEmpty()) listOf("Varios") else gastosCategorias.map { it.nombre },
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
    // Mapa de iconos básico
    val iconosTemplate = mapOf(
        "Sell" to Icons.Default.Sell,
        "Build" to Icons.Default.Build,
        "Restaurant" to Icons.Default.Restaurant,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "Tv" to Icons.Default.Tv,
        "Work" to Icons.Default.Work,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Payments" to Icons.Default.Payments,
        "Home" to Icons.Default.Home
    )

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
                    .background(Color(0xFFFF5252).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFFF5252))
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
                    text = "-€${String.format(Locale.getDefault(), "%,.2f", gasto.cantidad)}",
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
fun AgregarGastoDialog(
    categoriasDisponibles: List<String>,
    onDismiss: () -> Unit, 
    onConfirm: (String, Double, String) -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var cantidadStr by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(categoriasDisponibles.first()) }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") })
                TextField(value = cantidadStr, onValueChange = { cantidadStr = it }, label = { Text("Monto") })
                
                Text("Selecciona Categoría:", fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoriasDisponibles) { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val cantidad = cantidadStr.toDoubleOrNull()
                if (concepto.isBlank() || cantidadStr.isBlank()) {
                    Toast.makeText(contexto, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                } else if (cantidad == null) {
                    Toast.makeText(contexto, "El monto debe ser un número válido", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(concepto, cantidad, categoriaSeleccionada)
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
