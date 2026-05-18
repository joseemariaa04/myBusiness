package com.example.mybusiness.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybusiness.R

import com.example.mybusiness.ui.PreferenciasViewModel
import com.example.mybusiness.ui.inicio.InicioViewModel

// Esta es la pantalla del chat donde podemos hablar con el asistente inteligente
@Composable
fun ChatScreen(
    controladorDelChat: ChatViewModel = viewModel(),
    controladorDeInicio: InicioViewModel,
    controladorDePreferencias: PreferenciasViewModel
) {
    // Aquí sacamos la lista de mensajes que se han ido enviando
    val listaDeMensajes by controladorDelChat.estadoMensajes.collectAsState()
    // Esto nos sirve para saber si la IA está escribiendo algo ahora mismo
    val estaCargandoLaIA by controladorDelChat.estaCargando.collectAsState()
    // Cogemos los datos del negocio para que la IA sepa qué recomendarnos
    val estadoDelNegocio by controladorDeInicio.estado.collectAsState()
    
    // Lo que estamos escribiendo en el hueco del teclado
    var textoEscritoPorElUsuario by remember { mutableStateOf("") }
    // Este estado sirve para que la lista de mensajes se mueva sola al final
    val estadoDeLaLista = rememberLazyListState()

    // Preparamos el "chuletero" para la IA con los datos de nuestra empresa
    val contextCompany = stringResource(R.string.ai_context_company)
    val contextDescription = stringResource(R.string.ai_context_description)
    val contextData = stringResource(R.string.ai_context_current_month_data)
    val labelIncome = stringResource(R.string.income)
    val labelExpenses = stringResource(R.string.expenses)
    val labelProfit = stringResource(R.string.current_monthly_profit)

    val informacionDeLaEmpresa = remember(estadoDelNegocio, controladorDePreferencias.descripcionEmpresa) {
        """
            $contextCompany: ${controladorDePreferencias.nombreEmpresa}
            $contextDescription: ${controladorDePreferencias.descripcionEmpresa}
            $contextData
            - $labelIncome: ${estadoDelNegocio.ingresosTotales} ${controladorDePreferencias.simboloMoneda}
            - $labelExpenses: ${estadoDelNegocio.gastosTotales} ${controladorDePreferencias.simboloMoneda}
            - $labelProfit: ${estadoDelNegocio.beneficioMensual} ${controladorDePreferencias.simboloMoneda}
        """.trimIndent()
    }

    // Al iniciar la pantalla, cargamos el mensaje de bienvenida si no hay mensajes
    val mensajeBienvenida = stringResource(R.string.mensaje_IA)
    LaunchedEffect(Unit) {
        controladorDelChat.inicializarChat(mensajeBienvenida)
    }

    // Si la lista de mensajes cambia (porque mandamos uno), bajamos hasta el último
    LaunchedEffect(listaDeMensajes.size) {
        if (listaDeMensajes.isNotEmpty()) {
            estadoDeLaLista.animateScrollToItem(listaDeMensajes.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Aquí mostramos todos los mensajes que hay en la conversación
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = estadoDeLaLista,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaDeMensajes) { mensajeSuelto ->
                BurbujaDelMensaje(mensajeSuelto)
            }
            // Si la IA está buscando la respuesta, ponemos el circulito de espera
            if (estaCargandoLaIA) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // La barra de abajo donde escribimos el mensaje
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoEscritoPorElUsuario,
                onValueChange = { textoEscritoPorElUsuario = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.chat_placeholder)) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textoEscritoPorElUsuario.isNotBlank()) {
                        // Pasamos también el prompt del sistema traducido
                        controladorDelChat.enviarMensaje(textoEscritoPorElUsuario, informacionDeLaEmpresa)
                        // Borramos el texto para poder escribir otro nuevo
                        textoEscritoPorElUsuario = ""
                    }
                },
                // El botón solo se activa si hay texto y la IA no está ocupada
                enabled = !estaCargandoLaIA && textoEscritoPorElUsuario.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send_message))
            }
        }
    }
}

// Así es como se ve un solo mensaje en la pantalla
@Composable
fun BurbujaDelMensaje(mensaje: MensajeChat) {
    // Comprobamos si el mensaje lo hemos escrito nosotros
    val loHeEscritoYo = mensaje.quienEscribe == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        // Si es nuestro mensaje lo ponemos a la derecha, si no a la izquierda
        horizontalAlignment = if (loHeEscritoYo) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (loHeEscritoYo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (loHeEscritoYo) 16.dp else 0.dp,
                bottomEnd = if (loHeEscritoYo) 0.dp else 16.dp
            ),
            tonalElevation = 2.dp
        ) {
            Text(
                text = mensaje.textoMensaje,
                modifier = Modifier.padding(12.dp),
                color = if (loHeEscritoYo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}
