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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import com.example.mybusiness.R
import com.example.mybusiness.data.Trabajador
import com.example.mybusiness.ui.AnimacionEntradaLista
import com.example.mybusiness.ui.EstadoVacio

import androidx.compose.ui.text.style.TextOverflow

// Esta pantalla sirve para ver quién trabaja con nosotros y añadir gente nueva
@Composable
fun TrabajadoresScreen(controladorTrabajadores: TrabajadoresViewModel) {
    // Escuchamos la lista de empleados que nos da el controlador
    val listaActualTrabajadores by controladorTrabajadores.listaDeTrabajadores.collectAsState()
    
    // Estados para controlar los diálogos y la búsqueda
    var mostrarCuadroNuevo by remember { mutableStateOf(false) }
    var empleadoABorrar by remember { mutableStateOf<Trabajador?>(null) }
    var textoABuscar by remember { mutableStateOf("") }

    // Filtramos la lista según lo que hayamos escrito en el buscador
    val trabajadoresFiltrados = listaActualTrabajadores.filter {
        it.nombre.contains(textoABuscar, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            // Botón "+" para contratar a alguien nuevo
            FloatingActionButton(
                onClick = { mostrarCuadroNuevo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.agregar_trabajador_desc))
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
                text = stringResource(R.string.titulo_personal),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Barra de Búsqueda para encontrar a alguien rápido por su nombre
            OutlinedTextField(
                value = textoABuscar,
                onValueChange = { textoABuscar = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(stringResource(R.string.buscar_nombre)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Si no hay nadie que coincida con la búsqueda, enseñamos el aviso de "Vacío"
            if (trabajadoresFiltrados.isEmpty()) {
                EstadoVacio(
                    mensaje = stringResource(R.string.sin_personal),
                    subMensaje = stringResource(R.string.sin_personal_sub),
                    icono = Icons.Default.Group
                )
            } else {
                // Si hay gente, los ponemos en una lista
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(trabajadoresFiltrados) { indice, unEmpleado ->
                        AnimacionEntradaLista(indice = indice) {
                            TarjetaDeEmpleado(
                                empleado = unEmpleado as Trabajador,
                                alCambiarEstado = { 
                                    // Si pulsamos el estado, cambia entre Activo e Inactivo
                                    controladorTrabajadores.actualizarDatosTrabajador((unEmpleado as Trabajador).copy(activo = !unEmpleado.activo))
                                },
                                alBorrar = { empleadoABorrar = unEmpleado as Trabajador }
                            )
                        }
                    }
                }
            }
        }

        // Aviso de confirmación antes de borrar a un trabajador
        if (empleadoABorrar != null) {
            AlertDialog(
                onDismissRequest = { empleadoABorrar = null },
                title = { Text(stringResource(R.string.confirmar_eliminacion_titulo)) },
                text = { Text(stringResource(R.string.confirmar_eliminacion_trabajador_desc, empleadoABorrar?.nombre ?: "")) },
                confirmButton = {
                    Button(
                        onClick = {
                            empleadoABorrar?.let { controladorTrabajadores.borrarTrabajador(it) }
                            empleadoABorrar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.eliminar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { empleadoABorrar = null }) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }

        // El formulario para meter los datos de un nuevo trabajador
        if (mostrarCuadroNuevo) {
            DialogoParaContratar(
                alCerrar = { mostrarCuadroNuevo = false },
                alGuardar = { nombre, puesto, tel, mail, sueldo, estaActivo ->
                    controladorTrabajadores.contratarTrabajador(nombre, puesto, tel, mail, sueldo, estaActivo)
                    mostrarCuadroNuevo = false
                }
            )
        }
    }
}

// Así es como se ve la tarjeta con la información de un empleado
@Composable
fun TarjetaDeEmpleado(empleado: Trabajador, alCambiarEstado: () -> Unit, alBorrar: () -> Unit) {
    val contexto = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Un redondel con el icono de una persona
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
                    Text(
                        text = empleado.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Botón pequeño para ver si está trabajando o no, ahora en su propia línea
                    val colorEstado = if (empleado.activo) Color(0xFF4CAF50) else Color.Gray
                    Surface(
                        color = colorEstado.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        onClick = alCambiarEstado,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (empleado.activo) stringResource(R.string.activo) else stringResource(R.string.inactivo),
                            color = colorEstado,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "${empleado.puesto} • ${stringResource(R.string.salario_mensual)}: ${empleado.salario}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                // Botón para llamar directamente por teléfono
                IconButton(onClick = {
                    val intencion = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${empleado.telefono}")
                    }
                    contexto.startActivity(intencion)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.llamar), tint = MaterialTheme.colorScheme.primary)
                }
                // Botón para mandar un email
                IconButton(onClick = {
                    val intencion = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${empleado.email}")
                    }
                    contexto.startActivity(intencion)
                }) {
                    Icon(Icons.Default.Email, contentDescription = stringResource(R.string.enviar_email), tint = MaterialTheme.colorScheme.secondary)
                }
                // Botón para quitar al empleado de la lista
                IconButton(onClick = alBorrar) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar), tint = Color.Gray)
                }
            }
        }
    }
}

// El cuadro que se abre para rellenar los datos del nuevo fichaje
@Composable
fun DialogoParaContratar(alCerrar: () -> Unit, alGuardar: (String, String, String, String, Double, Boolean) -> Unit) {
    var queNombre by remember { mutableStateOf("") }
    var quePuesto by remember { mutableStateOf("") }
    var queTelefono by remember { mutableStateOf("") }
    var queEmail by remember { mutableStateOf("") }
    var queSalario by remember { mutableStateOf("") }
    var estaTrabajandoYa by remember { mutableStateOf(true) }
    val aviso = LocalContext.current

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(stringResource(R.string.nuevo_empleado)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(value = queNombre, onValueChange = { queNombre = it }, label = { Text(stringResource(R.string.nombre)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = quePuesto, onValueChange = { quePuesto = it }, label = { Text(stringResource(R.string.puesto)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = queTelefono,
                    onValueChange = { queTelefono = it },
                    label = { Text(stringResource(R.string.telefono)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(value = queEmail, onValueChange = { queEmail = it }, label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = queSalario,
                    onValueChange = { queSalario = it },
                    label = { Text(stringResource(R.string.salario_mensual)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = estaTrabajandoYa, onCheckedChange = { estaTrabajandoYa = it })
                    Text(stringResource(R.string.trabajador_activo))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val sueldoNumero = queSalario.toDoubleOrNull()
                // Comprobamos que no falte nada importante
                if (queNombre.isBlank() || quePuesto.isBlank() || queTelefono.isBlank() || queEmail.isBlank() || sueldoNumero == null) {
                    Toast.makeText(aviso, aviso.getString(R.string.rellenar_todos_campos), Toast.LENGTH_SHORT).show()
                } else {
                    alGuardar(queNombre, quePuesto, queTelefono, queEmail, sueldoNumero, estaTrabajandoYa)
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
