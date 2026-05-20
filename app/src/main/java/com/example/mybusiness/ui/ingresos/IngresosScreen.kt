package com.example.mybusiness.ui.ingresos

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import com.example.mybusiness.R
import com.example.mybusiness.data.Categoria
import com.example.mybusiness.data.Cliente
import com.example.mybusiness.ui.IconosCategoria
import com.example.mybusiness.ui.PreferenciasViewModel
import com.example.mybusiness.ui.clientes.ClientesViewModel

// Esta pantalla sirve para ver y anotar el dinero que va entrando al negocio
@Composable
fun IngresosScreen(
    controladorIngresos: IngresosViewModel, 
    controladorPreferencias: PreferenciasViewModel = viewModel(),
    controladorClientes: ClientesViewModel = viewModel()
) {
    // Escuchamos la lista de ingresos que nos da el controlador
    val listaActualIngresos by controladorIngresos.listaDeIngresos.collectAsState()
    
    // Obtenemos los clientes para poder asociarlos a los ingresos
    val listaClientes by controladorClientes.clientes.collectAsState()
    
    // Necesitamos las categorías para poder clasificar los ingresos
    val catViewModel: CategoriasViewModel = viewModel()
    val listaCategorias by catViewModel.categorias.collectAsState()
    
    // Para saber si tenemos que enseñar el cuadro de "Añadir nuevo"
    var mostrarCuadroNuevo by remember { mutableStateOf(false) }
    var ingresoABorrar by remember { mutableStateOf<Ingreso?>(null) }

    Scaffold(
        floatingActionButton = {
            // Botón redondo con un "+" para añadir ingresos
            FloatingActionButton(
                onClick = { mostrarCuadroNuevo = true },
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

            // Si no hay ingresos, enseñamos un dibujo y un texto de "Está vacío"
            if (listaActualIngresos.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.no_income),
                    subMensaje = stringResource(R.string.register_first_income),
                    icono = Icons.AutoMirrored.Filled.TrendingUp
                )
            } else {
                // Si hay ingresos, los ponemos uno debajo de otro en una lista
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(listaActualIngresos) { indice, unIngreso ->
                        AnimacionEntradaLista(indice = indice) {
                            val clienteAsociado = listaClientes.find { it.id == unIngreso.clienteId }
                            TarjetaDeIngreso(
                                elIngreso = unIngreso as Ingreso,
                                nombreCliente = clienteAsociado?.empresa ?: clienteAsociado?.nombre,
                                alBorrar = { ingresoABorrar = unIngreso as Ingreso },
                                pref = controladorPreferencias
                            )
                        }
                    }
                }
            }
        }

        // Si hemos pulsado el botón "+", enseñamos este diálogo
        if (mostrarCuadroNuevo) {
            // Solo queremos las categorías que son para ingresos (no las de gastos)
            val categoriasParaIngresos = listaCategorias.filter { it.esIngreso }
            DialogoParaAñadirIngreso(
                listaDeCategorias = if (categoriasParaIngresos.isEmpty()) listOf(Categoria(nombre = "Venta", esIngreso = true, iconoNombre = "Sell")) else categoriasParaIngresos,
                listaClientes = listaClientes,
                alCerrar = { mostrarCuadroNuevo = false },
                alGuardar = { concepto, dinero, categoria, esFijo, idCliente ->
                    controladorIngresos.apuntarNuevoIngreso(concepto, dinero, System.currentTimeMillis(), categoria, esFijo, idCliente)
                    mostrarCuadroNuevo = false
                }
            )
        }

        // Diálogo de confirmación para borrar
        if (ingresoABorrar != null) {
            AlertDialog(
                onDismissRequest = { ingresoABorrar = null },
                title = { Text(stringResource(R.string.delete_confirm_title)) },
                text = { Text(stringResource(R.string.delete_income_confirm_desc, ingresoABorrar?.concepto ?: "")) },
                confirmButton = {
                    Button(
                        onClick = {
                            ingresoABorrar?.let { controladorIngresos.borrarIngreso(it) }
                            ingresoABorrar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ingresoABorrar = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

// Así es como se ve la tarjeta de cada ingreso suelto
@Composable
fun TarjetaDeIngreso(
    elIngreso: Ingreso, 
    nombreCliente: String? = null,
    alBorrar: () -> Unit, 
    pref: PreferenciasViewModel, 
    catViewModel: CategoriasViewModel = viewModel()
) {
    val categorias by catViewModel.categorias.collectAsState()
    val datosCategoria = categorias.find { it.nombre == elIngreso.categoria }
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
            // Un cuadradito de color con el icono de la categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = dibujoIcono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = elIngreso.concepto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Si el ingreso es fijo (como un sueldo recurrente), ponemos una chincheta
                    if (elIngreso.esFijo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.fixed),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                // Si el ingreso viene de un cliente, lo ponemos debajo del nombre
                if (nombreCliente != null) {
                    Text(
                        text = "Cliente: $nombreCliente",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                // Ponemos el día y la hora en que se anotó
                Text(
                    text = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(elIngreso.fecha)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                // El dinero que ha entrado en verde
                Text(
                    text = "+${pref.simboloMoneda}${String.format(Locale.getDefault(), "%,.2f", elIngreso.cantidad)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = elIngreso.categoria,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            // Botón de la papelera para borrar
            IconButton(onClick = alBorrar) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Gray)
            }
        }
    }
}

// Este es el formulario que aparece para escribir un nuevo ingreso
@Composable
fun DialogoParaAñadirIngreso(
    listaDeCategorias: List<Categoria>,
    listaClientes: List<Cliente>,
    alCerrar: () -> Unit,
    alGuardar: (String, Double, String, Boolean, Int?) -> Unit
) {
    var queEs by remember { mutableStateOf("") }
    var cuantoDinero by remember { mutableStateOf("") }
    var queCategoria by remember { mutableStateOf(listaDeCategorias.first().nombre) }
    var idClienteSeleccionado by remember { mutableStateOf<Int?>(null) }
    var seRepiteSiempre by remember { mutableStateOf(false) }
    var expandido by remember { mutableStateOf(false) }
    val aviso = LocalContext.current

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(stringResource(R.string.new_income_entry)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hueco para escribir el nombre (ej. "Venta de pan")
                OutlinedTextField(value = queEs, onValueChange = { queEs = it }, label = { Text(stringResource(R.string.concept)) }, modifier = Modifier.fillMaxWidth())
                // Hueco para escribir el dinero
                OutlinedTextField(
                    value = cuantoDinero,
                    onValueChange = { cuantoDinero = it },
                    label = { Text(stringResource(R.string.salary)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Selector de Cliente
                if (listaClientes.isNotEmpty()) {
                    Text("Asignar a cliente (Opcional):", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandido = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            val clienteActual = listaClientes.find { it.id == idClienteSeleccionado }
                            Text(clienteActual?.empresa ?: clienteActual?.nombre ?: "Ningún cliente")
                        }
                        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            DropdownMenuItem(
                                text = { Text("Ninguno") },
                                onClick = { idClienteSeleccionado = null; expandido = false }
                            )
                            listaClientes.forEach { cliente ->
                                DropdownMenuItem(
                                    text = { Text(cliente.empresa) },
                                    onClick = { idClienteSeleccionado = cliente.id; expandido = false }
                                )
                            }
                        }
                    }
                }

                Text(stringResource(R.string.select_category), fontWeight = FontWeight.Bold)
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
                // Casilla para marcar si el dinero entra todos los meses
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = seRepiteSiempre, onCheckedChange = { seRepiteSiempre = it })
                    Text(stringResource(R.string.fixed_income))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val numeroDinero = cuantoDinero.toDoubleOrNull()
                // Comprobamos que hayan escrito todo bien antes de guardar
                if (queEs.isBlank() || cuantoDinero.isBlank()) {
                    Toast.makeText(aviso, aviso.getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                } else if (numeroDinero == null) {
                    Toast.makeText(aviso, aviso.getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show()
                } else {
                    alGuardar(queEs, numeroDinero, queCategoria, seRepiteSiempre, idClienteSeleccionado)
                }
            }) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
