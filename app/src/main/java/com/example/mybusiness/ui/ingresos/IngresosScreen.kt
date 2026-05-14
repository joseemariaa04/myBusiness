package com.example.mybusiness.ui.ingresos

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.example.mybusiness.data.Ingreso
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio
import com.example.mybusiness.ui.categorias.CategoriasViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.res.stringResource
import com.example.mybusiness.R
import com.example.mybusiness.data.Categoria
import com.example.mybusiness.ui.IconosCategoria
import com.example.mybusiness.ui.PreferenciasViewModel

@Composable
fun IngresosScreen(viewModel: IngresosViewModel, prefViewModel: PreferenciasViewModel = viewModel()) {
    val ingresos by viewModel.ingresos.collectAsState()
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_income_desc))
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
                text = stringResource(R.string.recent_income),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (ingresos.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.no_income),
                    subMensaje = stringResource(R.string.register_first_income),
                    icono = Icons.AutoMirrored.Filled.TrendingUp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(ingresos) { index, ingreso ->
                        AnimacionEntradaLista(indice = index) {
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
            val ingresosCategorias = categorias.filter { it.esIngreso }
            AgregarIngresoDialog(
                categoriasData = if (ingresosCategorias.isEmpty()) listOf(Categoria(nombre = "Venta", esIngreso = true, iconoNombre = "Sell")) else ingresosCategorias,
                onDismiss = { mostrarDialogo = false },
                onConfirm = { concepto, cantidad, categoria, esFijo ->
                    viewModel.agregarIngreso(concepto, cantidad, System.currentTimeMillis(), categoria, esFijo)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun IngresoCard(ingreso: Ingreso, onDelete: () -> Unit, prefViewModel: PreferenciasViewModel = viewModel(), catViewModel: CategoriasViewModel = viewModel()) {
    val categorias by catViewModel.categorias.collectAsState()
    val categoriaData = categorias.find { it.nombre == ingreso.categoria }
    val icono = IconosCategoria.obtenerIcono(categoriaData?.iconoNombre)

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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ingreso.concepto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (ingreso.esFijo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.fixed),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(ingreso.fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${prefViewModel.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", ingreso.cantidad)}",
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
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun AgregarIngresoDialog(
    categoriasData: List<Categoria>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Boolean) -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var cantidadStr by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(categoriasData.first().nombre) }
    var esFijo by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_income_entry)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text(stringResource(R.string.concept)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cantidadStr, onValueChange = { cantidadStr = it }, label = { Text(stringResource(R.string.amount)) }, modifier = Modifier.fillMaxWidth())

                Text(stringResource(R.string.select_category), fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoriasData) { cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat.nombre,
                            onClick = { categoriaSeleccionada = cat.nombre },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconosCategoria.obtenerIcono(cat.iconoNombre),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(cat.nombre)
                                }
                            }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = esFijo, onCheckedChange = { esFijo = it })
                    Text(stringResource(R.string.fixed_income))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cantidad = cantidadStr.toDoubleOrNull()
                if (concepto.isBlank() || cantidadStr.isBlank()) {
                    Toast.makeText(contexto, contexto.getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                } else if (cantidad == null) {
                    Toast.makeText(contexto, contexto.getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(concepto, cantidad, categoriaSeleccionada, esFijo)
                }
            }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
