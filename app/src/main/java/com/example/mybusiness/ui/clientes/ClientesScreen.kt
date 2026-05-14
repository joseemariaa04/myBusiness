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

@Composable
fun ClientesScreen(viewModel: ClientesViewModel) {
    val clientes by viewModel.clientes.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    val clientesFiltrados = clientes.filter {
        it.nombre.contains(textoBusqueda, ignoreCase = true) || it.empresa.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
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

            // Barra de Búsqueda
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

            if (clientesFiltrados.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.no_clients),
                    subMensaje = stringResource(R.string.no_clients_sub),
                    icono = Icons.Default.BusinessCenter
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(clientesFiltrados) { indice, cliente ->
                        AnimacionEntradaLista(indice = indice) {
                            ClienteCard(
                                cliente = cliente,
                                onDelete = { viewModel.eliminarCliente(cliente) }
                            )
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            AgregarClienteDialog(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, empresa, telefono, email ->
                    viewModel.agregarCliente(nombre, empresa, telefono, email)
                    mostrarDialogo = false
                }
            )
        }
    }
}

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cliente.empresa,
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
                                text = stringResource(R.string.active),
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = cliente.nombre,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${cliente.telefono}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.call), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${cliente.email}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Email, contentDescription = stringResource(R.string.send_email), tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

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
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text(stringResource(R.string.contact_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = empresa, onValueChange = { empresa = it }, label = { Text(stringResource(R.string.company)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
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
