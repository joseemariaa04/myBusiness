package com.example.mybusiness.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    object Trabajadores : Screen("trabajadores", "Personal", Icons.Default.Group)
    object Clientes : Screen("clientes", "Clientes", Icons.Default.BusinessCenter)
    object Gastos : Screen("gastos", "Gastos", Icons.Default.MoneyOff)
    object Ingresos : Screen("ingresos", "Ingresos", Icons.Default.AttachMoney)
}

val itemsNavegacion = listOf(
    Screen.Inicio,
    Screen.Gastos,
    Screen.Ingresos,
    Screen.Trabajadores,
    Screen.Clientes
)
