package com.example.mybusiness.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Este objeto guarda una lista de iconos que podemos usar para las categorías.
// Así no tenemos que estar buscándolos por todo el código, están todos aquí centralizados.
object IconosCategoria {
    val mapa = mapOf(
        "Sell" to Icons.Default.Sell,
        "Build" to Icons.Default.Build,
        "Restaurant" to Icons.Default.Restaurant,
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "Tv" to Icons.Default.Tv,
        "Work" to Icons.Default.Work,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Payments" to Icons.Default.Payments,
        "Home" to Icons.Default.Home,
        "ShoppingBasket" to Icons.Default.ShoppingBasket,
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "Lightbulb" to Icons.Default.Lightbulb,
        "Construction" to Icons.Default.Construction,
        "Redeem" to Icons.Default.Redeem
    )

    // Esta función busca un icono por su nombre. Si no lo encuentra, pone uno por defecto.
    fun obtenerIcono(nombre: String?): ImageVector {
        return mapa[nombre] ?: Icons.Default.Category
    }
}

// Este componente sirve para cuando no hay datos que mostrar (por ejemplo, una lista vacía).
// Pone un dibujo grande y un par de textos explicando qué pasa.
@Composable
fun EstadoVacio(
    mensaje: String,
    subMensaje: String,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = mensaje,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subMensaje,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// Esta función hace que los elementos de una lista no aparezcan de golpe,
// sino que vayan entrando con una animación suave de abajo hacia arriba.
@Composable
fun AnimacionEntradaLista(
    indice: Int,
    contenido: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = indice * 100)) +
                slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(durationMillis = 500, delayMillis = indice * 100)
                )
    ) {
        contenido()
    }
}
