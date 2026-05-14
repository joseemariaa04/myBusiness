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
import androidx.compose.ui.res.stringResource
import com.example.mybusiness.R
import com.example.mybusiness.data.Categoria
import com.example.mybusiness.data.HistorialMes
import com.example.mybusiness.ui.categorias.CategoriasViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.example.mybusiness.ui.IconosCategoria
import com.example.mybusiness.ui.PreferenciasViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

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
                    text = "${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", estado.beneficioMensual)}",
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
                titulo = stringResource(R.string.income),
                monto = estado.ingresosTotales,
                icono = Icons.Default.ArrowUpward,
                colorIcono = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1.0f)
            )
            ResumenCard(
                titulo = stringResource(R.string.expenses),
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
                Text(stringResource(R.string.new_month), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { mostrarDialogoCategorias = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.categories), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.history),
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
                HistorialCard(mes, onClick = { onVerDetalleMes(mes.id, mes.nombreMes) })
            }
        }
    }

    // Diálogo para cerrar mes
    if (mostrarDialogoNuevoMes) {
        NuevoMesDialog(
            onDismiss = { mostrarDialogoNuevoMes = false },
            onConfirm = {
                viewModel.cerrarMesActual()
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

@Composable
fun NuevoMesDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.close_month_title)) },
        text = {
            Column {
                Text(stringResource(R.string.close_month_desc))
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm()
            }) {
                Text(stringResource(R.string.confirm_restart))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
