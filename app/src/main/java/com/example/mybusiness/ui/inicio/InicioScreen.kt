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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.stringResource
import com.example.mybusiness.R
import com.example.mybusiness.data.Categoria
import com.example.mybusiness.data.HistorialMes
import com.example.mybusiness.ui.categorias.CategoriasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

import com.example.mybusiness.ui.IconosCategoria
import com.example.mybusiness.ui.PreferenciasViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.max

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun InicioScreen(
    viewModel: InicioViewModel,
    onVerDetalleMes: (Int, String) -> Unit,
    prefViewModel: PreferenciasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Detectar cambios de fecha al volver a la aplicación (útil si el usuario cambia la fecha en ajustes)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.comprobarCierreMesAutomatico()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { mensaje ->
            Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show()
        }
    }
    
    // Obtenemos el viewModel de categorías
    val catViewModel: CategoriasViewModel = viewModel()
    val listaCategorias by catViewModel.categorias.collectAsState()
    
    var mostrarDialogoCategorias by remember { mutableStateOf(false) }

    val beneficioMensual = estado.beneficioMensual
    val ingresosTotales = estado.ingresosTotales
    val gastosTotales = estado.gastosTotales
    val historial = estado.historial

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Beneficio Mensual Actual
            val esPositivo = beneficioMensual >= 0
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
                            text = stringResource(R.string.current_monthly_profit),
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
                        text = "${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", beneficioMensual)}",
                        color = colorBeneficio,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val ratio = if (ingresosTotales > 0) {
                        (gastosTotales / ingresosTotales).toFloat().coerceIn(0f, 1f)
                    } else if (gastosTotales > 0) 1f else 0f
                    
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
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ResumenCard(
                    titulo = stringResource(R.string.income),
                    monto = ingresosTotales,
                    icono = Icons.Default.ArrowUpward,
                    colorIcono = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.0f)
                )
                ResumenCard(
                    titulo = stringResource(R.string.expenses),
                    monto = gastosTotales,
                    icono = Icons.Default.ArrowDownward,
                    colorIcono = Color(0xFFFF5252),
                    modifier = Modifier.weight(1.0f)
                )
            }
        }

        item {
            Button(
                onClick = { mostrarDialogoCategorias = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Category, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.categories), fontWeight = FontWeight.Bold)
            }
        }

        item {
            Column {
                Text(
                    text = stringResource(R.string.annual_performance),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                GraficaBeneficios(historial = historial)
            }
        }

        item {
            Text(
                text = stringResource(R.string.history),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(historial) { mes ->
            HistorialCard(mes, onClick = { onVerDetalleMes(mes.id, mes.nombreMes) })
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
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

    // Mapa de iconos centralizado
    val iconosTemplate = IconosCategoria.mapa.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_categories)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.new_category), fontWeight = FontWeight.Bold)
                TextField(
                    value = nombreCat,
                    onValueChange = { nombreCat = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipoIngreso, onClick = { tipoIngreso = true })
                    Text(stringResource(R.string.income))
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = !tipoIngreso, onClick = { tipoIngreso = false })
                    Text(stringResource(R.string.expenses))
                }
                
                Text(stringResource(R.string.choose_icon), modifier = Modifier.padding(top = 8.dp))
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
                            Toast.makeText(contexto, contexto.getString(R.string.category_name_required), Toast.LENGTH_SHORT).show()
                        } else {
                            onAgregar(nombreCat, tipoIngreso, iconoSeleccionado)
                            nombreCat = ""
                            Toast.makeText(contexto, contexto.getString(R.string.category_added), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_new))
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text(stringResource(R.string.saved_categories), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.height(150.dp)) {
                    LazyColumn {
                        items(categorias) { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icono = IconosCategoria.obtenerIcono(cat.iconoNombre)
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
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun HistorialCard(mes: HistorialMes, onClick: () -> Unit, prefViewModel: PreferenciasViewModel = viewModel()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    text = "${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", mes.beneficio)}",
                    color = if (mes.beneficio >= 0) MaterialTheme.colorScheme.primary else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "I: ${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.0f", mes.ingresosTotales)} | G: ${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.0f", mes.gastosTotales)}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GraficaBeneficiosPreview() {
    val historialFicticio = listOf(
        HistorialMes(1, "Enero", 5000.0, 3000.0, 2000.0, 1640995200000L),
        HistorialMes(2, "Febrero", 8000.0, 4000.0, 4000.0, 1643673600000L),
        HistorialMes(3, "Marzo", 12000.0, 2000.0, 10000.0, 1646092800000L),
        HistorialMes(4, "Abril", 15000.0, 5000.0, 10000.0, 1648771200000L),
        HistorialMes(5, "Mayo", 18000.0, 6000.0, 12000.0, 1651363200000L),
        HistorialMes(6, "Junio", 14000.0, 7000.0, 7000.0, 1654041600000L),
        HistorialMes(7, "Julio", 13000.0, 8000.0, 5000.0, 1656633600000L),
        HistorialMes(8, "Agosto", 11000.0, 9000.0, 2000.0, 1659312000000L),
        HistorialMes(9, "Septiembre", 16000.0, 5000.0, 11000.0, 1661990400000L),
        HistorialMes(10, "Octubre", 20000.0, 8000.0, 12000.0, 1664582400000L),
        HistorialMes(11, "Noviembre", 18000.0, 7000.0, 11000.0, 1667260800000L),
        HistorialMes(12, "Diciembre", 25000.0, 10000.0, 15000.0, 1669852800000L),
        HistorialMes(13, "Enero 23", 30000.0, 12000.0, 18000.0, 1672531200000L)
    )
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            GraficaBeneficios(historial = historialFicticio)
        }
    }
}

@Composable
fun GraficaBeneficios(historial: List<HistorialMes>, prefViewModel: PreferenciasViewModel = viewModel()) {
    val ultimosMeses = if (historial.size > 12) {
        historial.take(12).reversed()
    } else {
        historial.reversed()
    }
    
    if (ultimosMeses.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(stringResource(R.string.no_history_data), color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    // Cálculo de rango robusto para evitar divisiones por cero o desbordamientos
    val maxBeneficio = ultimosMeses.maxOf { it.beneficio }
    val minBeneficio = ultimosMeses.minOf { it.beneficio }
    
    val maxVal = maxOf(maxBeneficio, 100.0)
    val minVal = minOf(minBeneficio, 0.0)
    val range = (maxVal - minVal).let { if (it <= 0) 1.0 else it }
    
    val colorLinea = MaterialTheme.colorScheme.primary
    val colorEje = Color.Gray.copy(alpha = 0.2f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Eje Y: Etiquetas de valores
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(50.dp)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("${prefViewModel.simboloMoneda}${formatValor(maxVal)}", fontSize = 10.sp, color = Color.Gray)
                    Text("${prefViewModel.simboloMoneda}${formatValor(minVal + range / 2)}", fontSize = 10.sp, color = Color.Gray)
                    Text("${prefViewModel.simboloMoneda}${formatValor(minVal)}", fontSize = 10.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Área de dibujo de la gráfica
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val count = ultimosMeses.size
                    val spacing = if (count > 1) canvasWidth / (count - 1) else canvasWidth

                    // Dibujar líneas de guía horizontales
                    drawLine(colorEje, start = Offset(0f, 0f), end = Offset(canvasWidth, 0f), strokeWidth = 1f)
                    drawLine(colorEje, start = Offset(0f, canvasHeight / 2), end = Offset(canvasWidth, canvasHeight / 2), strokeWidth = 1f)
                    drawLine(colorEje, start = Offset(0f, canvasHeight), end = Offset(canvasWidth, canvasHeight), strokeWidth = 1f)

                    // Calcular puntos (x, y)
                    val puntos = ultimosMeses.mapIndexed { index, mes ->
                        val x = index * spacing
                        val y = canvasHeight - ((mes.beneficio - minVal) / range * canvasHeight).toFloat()
                        Offset(x, y)
                    }

                    // Dibujar la línea conectora
                    if (puntos.size > 1) {
                        for (i in 0 until puntos.size - 1) {
                            drawLine(
                                color = colorLinea,
                                start = puntos[i],
                                end = puntos[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Dibujar los puntos y su sombra/brillo
                    puntos.forEach { punto ->
                        drawCircle(color = colorLinea, radius = 5.dp.toPx(), center = punto)
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = punto)
                    }
                }
            }

            // Eje X: Nombres de los meses
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 58.dp), // Alineado con el Canvas (50dp + 8dp spacer)
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ultimosMeses.forEach { mes ->
                    Text(
                        text = mes.nombreMes.take(3).uppercase(),
                        fontSize = 9.sp,
                        color = Color.Gray,
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun formatValor(valor: Double): String {
    return when {
        valor >= 1000000 || valor <= -1000000 -> String.format(Locale.getDefault(), "%.1fM", valor / 1000000)
        valor >= 1000 || valor <= -1000 -> String.format(Locale.getDefault(), "%.1fk", valor / 1000)
        else -> String.format(Locale.getDefault(), "%.0f", valor)
    }
}

@Composable
fun ResumenCard(
    titulo: String,
    monto: Double,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier,
    prefViewModel: PreferenciasViewModel = viewModel()
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
                text = "${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", monto)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

