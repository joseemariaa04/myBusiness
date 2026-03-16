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
import com.example.mybusiness.data.Trabajador
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio

@Composable
fun TrabajadoresScreen(viewModel: TrabajadoresViewModel) {
    val trabajadores by viewModel.trabajadores.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
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
                Icon(Icons.Default.Add, contentDescription = "Agregar Trabajador")
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
                text = "Personal",
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
                placeholder = { Text("Buscar por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            if (trabajadoresFiltrados.isEmpty()) {
                EstadoVacio(
                    mensaje = "No hay trabajadores",
                    subMensaje = "Comienza agregando a tu primer empleado pulsando el botón +",
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
                                onDelete = { viewModel.eliminarTrabajador(trabajador) }
                            )
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            AgregarTrabajadorDialog(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, puesto, telefono, email ->
                    viewModel.agregarTrabajador(nombre, puesto, telefono, email)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun TrabajadorCard(trabajador: Trabajador, onDelete: () -> Unit) {
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
                        Surface(
                            color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Activo",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = trabajador.puesto,
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
                    Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${trabajador.email}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Email, contentDescription = "Enviar Correo", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun AgregarTrabajadorDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var puesto by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Trabajador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                TextField(value = puesto, onValueChange = { puesto = it }, label = { Text("Puesto") })
                TextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (nombre.isBlank() || puesto.isBlank() || telefono.isBlank() || email.isBlank()) {
                    Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(nombre, puesto, telefono, email)
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
