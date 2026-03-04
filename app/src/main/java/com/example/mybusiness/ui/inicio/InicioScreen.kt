package com.example.mybusiness.ui.inicio

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybusiness.data.Categoria
import com.example.mybusiness.data.HistorialMes
import com.example.mybusiness.ui.categorias.CategoriasViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InicioScreen(
    viewModel: InicioViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    
    // Obtenemos el viewModel de categorías
    val catViewModel: CategoriasViewModel = viewModel()
    val listaCategorias by catViewModel.categorias.collectAsState()
    
    var mostrarDialogoNuevoMes by remember { mutableStateOf(false) }
    var mostrarDialogoCategorias by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de Beneficio Mensual Actual
        val esPositivo = estado.beneficioMensual >= 0
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
                    text = "€${String.format(Locale.getDefault(), "%,.2f", estado.beneficioMensual)}",
                    color = colorBeneficio,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                val ratio = if (estado.ingresosTotales > 0) {
                    (estado.gastosTotales / estado.ingresosTotales).toFloat().coerceIn(0f, 1f)
                } else if (estado.gastosTotales > 0) 1f else 0f
                
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
                monto = estado.ingresosTotales,
                icono = Icons.Default.ArrowUpward,
                colorIcono = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1.0f)
            )
            ResumenCard(
                titulo = "Gastos",
                monto = estado.gastosTotales,
                icono = Icons.Default.ArrowDownward,
                colorIcono = Color(0xFFFF5252),
                modifier = Modifier.weight(1.0f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botones de acción principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { mostrarDialogoNuevoMes = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("NUEVO MES", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { mostrarDialogoCategorias = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CATEGORÍAS", fontWeight = FontWeight.Bold)
            }
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
            items(estado.historial) { mes ->
                HistorialCard(mes)
            }
        }
    }

    // Diálogo para cerrar mes
    if (mostrarDialogoNuevoMes) {
        NuevoMesDialog(
            onDismiss = { mostrarDialogoNuevoMes = false },
            onConfirm = { nombre ->
                viewModel.cerrarMesActual(nombre)
                mostrarDialogoNuevoMes = false
            }
        )
    }

    // Diálogo para gestionar categorías
    if (mostrarDialogoCategorias) {
        DialogoGestionCategorias(
            categorias = listaCategorias,
            onDismiss = { mostrarDialogoCategorias = false },
            onAgregar = { nombre, esIngreso, icono ->
                catViewModel.agregarCategoria(nombre, esIngreso, icono)
            },
            onEliminar = { catViewModel.eliminarCategoria(it) }
        )
    }
}

@Composable
fun DialogoGestionCategorias(
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onAgregar: (String, Boolean, String) -> Unit,
    onEliminar: (Categoria) -> Unit
) {
    var nombreCat by remember { mutableStateOf("") }
    var tipoIngreso by remember { mutableStateOf(true) }
    var iconoSeleccionado by remember { mutableStateOf("Sell") }
    val contexto = LocalContext.current

    // Mapa de iconos para que el estudiante vea como se asocian
    val iconosTemplate = listOf(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestión de Categorías") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Nueva Categoría:", fontWeight = FontWeight.Bold)
                TextField(
                    value = nombreCat,
                    onValueChange = { nombreCat = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipoIngreso, onClick = { tipoIngreso = true })
                    Text("Ingreso")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = !tipoIngreso, onClick = { tipoIngreso = false })
                    Text("Gasto")
                }
                
                Text("Elige un icono:", modifier = Modifier.padding(top = 8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(iconosTemplate) { (nombre, icono) ->
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(if (iconoSeleccionado == nombre) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f))
                                .clickable { iconoSeleccionado = nombre },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icono,
                                contentDescription = null,
                                tint = if (iconoSeleccionado == nombre) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Button(
                    onClick = {
                        if (nombreCat.isBlank()) {
                            Toast.makeText(contexto, "Escribe un nombre para la categoría", Toast.LENGTH_SHORT).show()
                        } else {
                            onAgregar(nombreCat, tipoIngreso, iconoSeleccionado)
                            nombreCat = ""
                            Toast.makeText(contexto, "Categoría añadida", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Nueva")
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text("Categorías Guardadas:", fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.height(150.dp)) {
                    LazyColumn {
                        items(categorias) { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icono = iconosTemplate.find { it.first == cat.iconoNombre }?.second ?: Icons.Default.Category
                                    Icon(
                                        imageVector = icono,
                                        contentDescription = null,
                                        tint = if (cat.esIngreso) MaterialTheme.colorScheme.primary else Color(0xFFFF5252),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.nombre, fontSize = 14.sp)
                                }
                                IconButton(onClick = { onEliminar(cat) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
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
