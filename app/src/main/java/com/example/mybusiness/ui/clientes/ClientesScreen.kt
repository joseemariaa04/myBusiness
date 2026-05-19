package com.example.mybusiness.ui.clientes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.BusinessCenter
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
import com.example.mybusiness.data.Cliente
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio

// Esta pantalla sirve para gestionar nuestra lista de clientes y sus datos de contacto
@Composable
fun ClientesScreen(controladorClientes: ClientesViewModel) {
    // Obtenemos la lista de clientes que el controlador tiene guardada
    val clientes by controladorClientes.clientes.collectAsState()
    
    // Estados para controlar si se ve el formulario de añadir o el de borrar
    var mostrarDialogo by remember { mutableStateOf(false) }
    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }
    var textoBusqueda by remember { mutableStateOf("") }

    // Filtramos los clientes por nombre o empresa según lo que escribamos en el buscador
    val clientesFiltrados = clientes.filter {
        it.nombre.contains(textoBusqueda, ignoreCase = true) || it.empresa.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            // Botón "+" para añadir un cliente nuevo
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_client_desc))
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
                text = stringResource(R.string.clients_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Barra de Búsqueda para encontrar clientes rápido
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(stringResource(R.string.search_clients)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Si no hay clientes que mostrar, ponemos el aviso de "Vacío"
            if (clientesFiltrados.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.no_clients),
                    subMensaje = stringResource(R.string.no_clients_sub),
                    icono = Icons.Default.BusinessCenter
                )
            } else {
                // Si hay clientes, los enseñamos en una lista
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(clientesFiltrados) { indice, cliente ->
                        AnimacionEntradaLista(indice = indice) {
                            ClienteCard(
                                cliente = cliente,
                                onDelete = { clienteAEliminar = cliente }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo para confirmar si de verdad queremos borrar a un cliente
        if (clienteAEliminar != null) {
            AlertDialog(
                onDismissRequest = { clienteAEliminar = null },
                title = { Text(stringResource(R.string.delete_confirm_title)) },
                text = { Text(stringResource(R.string.delete_client_confirm_desc, clienteAEliminar?.nombre ?: "")) },
                confirmButton = {
                    Button(
                        onClick = {
                            clienteAEliminar?.let { controladorClientes.eliminarCliente(it) }
                            clienteAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { clienteAEliminar = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // El formulario que se abre para rellenar los datos del nuevo cliente
        if (mostrarDialogo) {
            AgregarClienteDialog(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, empresa, telefono, email ->
                    controladorClientes.agregarCliente(nombre, empresa, telefono, email)
                    mostrarDialogo = false
                }
            )
        }
    }
}

// Así es como se ve la tarjeta de cada cliente en la lista
@Composable
fun ClienteCard(cliente: Cliente, onDelete: () -> Unit) {
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
                // Icono de edificio para representar a la empresa
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cliente.empresa,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = cliente.nombre,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                // Botón para llamar por teléfono
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${cliente.telefono}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.call), tint = MaterialTheme.colorScheme.primary)
                }
                // Botón para enviar un correo electrónico
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${cliente.email}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Email, contentDescription = stringResource(R.string.send_email), tint = MaterialTheme.colorScheme.secondary)
                }
                // Botón de papelera para eliminar al cliente
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Gray)
                }
            }
        }
    }
}

// El cuadro que aparece para escribir los datos del nuevo cliente
@Composable
fun AgregarClienteDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var empresa by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_client)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Campos de texto para rellenar la información
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text(stringResource(R.string.contact_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = empresa, onValueChange = { empresa = it }, label = { Text(stringResource(R.string.company)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
                // Validamos que no haya huecos vacíos y que el email y teléfono parezcan correctos
                if (nombre.isBlank() || empresa.isBlank() || telefono.isBlank() || email.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
                } else if (!email.matches(emailRegex)) {
                    Toast.makeText(context, context.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
                } else if (telefono.length < 9) {
                    Toast.makeText(context, context.getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(nombre, empresa, telefono, email)
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

