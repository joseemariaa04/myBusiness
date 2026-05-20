package com.example.mybusiness.ui.gastos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.mybusiness.R
import com.example.mybusiness.data.Gasto
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio
import com.example.mybusiness.ui.categorias.CategoriasViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.example.mybusiness.data.Categoria
import com.example.mybusiness.ui.IconosCategoria
import com.example.mybusiness.ui.PreferenciasViewModel

// Esta pantalla sirve para ver y anotar todos los gastos (salidas de dinero) del negocio
@Composable
fun GastosScreen(controladorGastos: GastosViewModel, controladorPreferencias: PreferenciasViewModel = viewModel()) {
    // Escuchamos la lista de gastos que nos da el controlador
    val listaActualGastos by controladorGastos.listaDeGastos.collectAsState()
    
    // Necesitamos las categorías para poder clasificar en qué gastamos el dinero
    val catViewModel: CategoriasViewModel = viewModel()
    val listaCategorias by catViewModel.categorias.collectAsState()
    
    // Para saber si tenemos que enseñar el cuadro de "Añadir nuevo"
    var mostrarCuadroNuevo by remember { mutableStateOf(false) }
    var gastoABorrar by remember { mutableStateOf<Gasto?>(null) }

    Scaffold(
        floatingActionButton = {
            // Botón redondo con un "+" para añadir gastos
            FloatingActionButton(
                onClick = { mostrarCuadroNuevo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.agregar_gasto_desc))
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
                text = stringResource(R.string.gastos_recientes),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Si no hay gastos, enseñamos un dibujo y un texto de "No hay nada"
            if (listaActualGastos.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.sin_gastos),
                    subMensaje = stringResource(R.string.registrar_primer_gasto),
                    icono = Icons.Default.MoneyOff
                )
            } else {
                // Si hay gastos, los ponemos en una lista vertical
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(listaActualGastos) { indice, unGasto ->
                        AnimacionEntradaLista(indice = indice) {
                            TarjetaDeGasto(
                                elGasto = unGasto as Gasto,
                                alBorrar = { gastoABorrar = unGasto as Gasto },
                                pref = controladorPreferencias
                            )
                        }
                    }
                }
            }
        }

        // Si hemos pulsado el botón "+", enseñamos este diálogo
        if (mostrarCuadroNuevo) {
            // Solo queremos las categorías que son para gastos (no las de ingresos)
            val categoriasParaGastos = listaCategorias.filter { !it.esIngreso }
            DialogoParaAñadirGasto(
                listaDeCategorias = if (categoriasParaGastos.isEmpty()) listOf(Categoria(nombre = "Varios", esIngreso = false, iconoNombre = "ShoppingBag")) else categoriasParaGastos,
                alCerrar = { mostrarCuadroNuevo = false },
                alGuardar = { concepto, dinero, categoria, esFijo ->
                    controladorGastos.apuntarNuevoGasto(concepto, dinero, System.currentTimeMillis(), categoria, esFijo)
                    mostrarCuadroNuevo = false
                }
            )
        }

        // Diálogo de confirmación para borrar
        if (gastoABorrar != null) {
            AlertDialog(
                onDismissRequest = { gastoABorrar = null },
                title = { Text(stringResource(R.string.confirmar_eliminacion_titulo)) },
                text = { Text(stringResource(R.string.confirmar_eliminacion_gasto_desc, gastoABorrar?.concepto ?: "")) },
                confirmButton = {
                    Button(
                        onClick = {
                            gastoABorrar?.let { controladorGastos.borrarGasto(it) }
                            gastoABorrar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.eliminar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { gastoABorrar = null }) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }
    }
}

// Así es como se ve la tarjeta de cada gasto suelto
@Composable
fun TarjetaDeGasto(
    elGasto: Gasto, 
    alBorrar: () -> Unit, 
    pref: PreferenciasViewModel, 
    catViewModel: CategoriasViewModel = viewModel()
) {
    val categorias by catViewModel.categorias.collectAsState()
    val datosCategoria = categorias.find { it.nombre == elGasto.categoria }
    val dibujoIcono = IconosCategoria.obtenerIcono(datosCategoria?.iconoNombre)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Un cuadradito rojo con el icono de la categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF5252).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = dibujoIcono, contentDescription = null, tint = Color(0xFFFF5252))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = elGasto.concepto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Si el gasto es fijo (como el alquiler), ponemos una chincheta
                    if (elGasto.esFijo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.fijo),
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFF5252)
                        )
                    }
                }
                // Ponemos la fecha y hora
                Text(
                    text = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(elGasto.fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                // El dinero que ha salido en color rojo
                Text(
                    text = "-${pref.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", elGasto.cantidad)}",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = elGasto.categoria,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            // Botón para borrar el gasto
            IconButton(onClick = alBorrar) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar), tint = Color.Gray)
            }
        }
    }
}

// Este es el formulario que aparece para escribir un nuevo gasto
@Composable
fun DialogoParaAñadirGasto(
    listaDeCategorias: List<Categoria>,
    alCerrar: () -> Unit,
    alGuardar: (String, Double, String, Boolean) -> Unit
) {
    var queEs by remember { mutableStateOf("") }
    var cuantoDinero by remember { mutableStateOf("") }
    var queCategoria by remember { mutableStateOf(listaDeCategorias.first().nombre) }
    var seRepiteSiempre by remember { mutableStateOf(false) }
    val aviso = LocalContext.current

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(stringResource(R.string.nuevo_gasto)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hueco para el nombre del gasto
                OutlinedTextField(value = queEs, onValueChange = { queEs = it }, label = { Text(stringResource(R.string.concepto)) }, modifier = Modifier.fillMaxWidth())
                // Hueco para el dinero
                OutlinedTextField(
                    value = cuantoDinero,
                    onValueChange = { cuantoDinero = it },
                    label = { Text(stringResource(R.string.cantidad)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Text(stringResource(R.string.seleccionar_categoria), fontWeight = FontWeight.Bold)
                // Lista de burbujas para elegir la categoría
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaDeCategorias) { cat ->
                        FilterChip(
                            selected = queCategoria == cat.nombre,
                            onClick = { queCategoria = cat.nombre },
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
                // Casilla para marcar si el gasto es fijo (se repite todos los meses)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = seRepiteSiempre, onCheckedChange = { seRepiteSiempre = it })
                    Text(stringResource(R.string.gasto_fijo))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val numeroDinero = cuantoDinero.toDoubleOrNull()
                // Comprobamos que hayan escrito todo antes de guardar
                if (queEs.isBlank() || cuantoDinero.isBlank()) {
                    Toast.makeText(aviso, aviso.getString(R.string.campos_obligatorios), Toast.LENGTH_SHORT).show()
                } else if (numeroDinero == null) {
                    Toast.makeText(aviso, aviso.getString(R.string.cantidad_invalida), Toast.LENGTH_SHORT).show()
                } else {
                    alGuardar(queEs, numeroDinero, queCategoria, seRepiteSiempre)
                }
            }) {
                Text(stringResource(R.string.agregar))
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
