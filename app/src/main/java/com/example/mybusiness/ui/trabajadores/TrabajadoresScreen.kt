package com.example.mybusiness.ui.trabajadores

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Group
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
import androidx.compose.ui.res.stringResource
import com.example.mybusiness.R
import com.example.mybusiness.data.Trabajador
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio

@Composable
fun TrabajadoresScreen(viewModel: TrabajadoresViewModel) {
    val trabajadores by viewModel.trabajadores.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var trabajadorAEliminar by remember { mutableStateOf<Trabajador?>(null) }
    var textoBusqueda by remember { mutableStateOf("") }

    val trabajadoresFiltrados = trabajadores.filter {
        it.nombre.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_worker_desc))
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
                text = stringResource(R.string.staff_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Barra de Búsqueda
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(stringResource(R.string.search_name)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            if (trabajadoresFiltrados.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.no_staff),
                    subMensaje = stringResource(R.string.no_staff_sub),
                    icono = Icons.Default.Group
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(trabajadoresFiltrados) { indice, trabajador ->
                        AnimacionEntradaLista(indice = indice) {
                            TrabajadorCard(
                                trabajador = trabajador,
                                onToggleActivo = { 
                                    viewModel.actualizarTrabajador(trabajador.copy(activo = !trabajador.activo))
                                },
                                onDelete = { trabajadorAEliminar = trabajador }
                            )
                        }
                    }
                }
            }
        }

        if (trabajadorAEliminar != null) {
            AlertDialog(
                onDismissRequest = { trabajadorAEliminar = null },
                title = { Text(stringResource(R.string.delete_confirm_title)) },
                text = { Text(stringResource(R.string.delete_worker_confirm_desc, trabajadorAEliminar?.nombre ?: "")) },
                confirmButton = {
                    Button(
                        onClick = {
                            trabajadorAEliminar?.let { viewModel.eliminarTrabajador(it) }
                            trabajadorAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { trabajadorAEliminar = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (mostrarDialogo) {
            AgregarTrabajadorDialog(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, puesto, telefono, email, salario, activo ->
                    viewModel.agregarTrabajador(nombre, puesto, telefono, email, salario, activo)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun TrabajadorCard(trabajador: Trabajador, onToggleActivo: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trabajador.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val statusColor = if (trabajador.activo) Color(0xFF4CAF50) else Color.Gray
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            onClick = onToggleActivo
                        ) {
                            Text(
                                text = if (trabajador.activo) stringResource(R.string.active) else stringResource(R.string.inactive),
                                color = statusColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${trabajador.puesto} • ${stringResource(R.string.salary)}: ${trabajador.salario}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${trabajador.telefono}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.call), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${trabajador.email}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Email, contentDescription = stringResource(R.string.send_email), tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AgregarTrabajadorDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, Double, Boolean) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var puesto by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var salario by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_employee)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text(stringResource(R.string.name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = puesto, onValueChange = { puesto = it }, label = { Text(stringResource(R.string.position)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = salario, onValueChange = { salario = it }, label = { Text(stringResource(R.string.salario)) }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activo, onCheckedChange = { activo = it })
                    Text(stringResource(R.string.active_worker))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val salarioDouble = salario.toDoubleOrNull()
                if (nombre.isBlank() || puesto.isBlank() || telefono.isBlank() || email.isBlank() || salarioDouble == null) {
                    Toast.makeText(context, context.getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(nombre, puesto, telefono, email, salarioDouble, activo)
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
