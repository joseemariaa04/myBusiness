package com.example.mybusiness.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mybusiness.R

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Inicio : Screen("inicio", R.string.nav_home, Icons.Default.Home)
    object Trabajadores : Screen("trabajadores", R.string.nav_staff, Icons.Default.Group)
    object Clientes : Screen("clientes", R.string.nav_clients, Icons.Default.BusinessCenter)
    object Gastos : Screen("gastos", R.string.nav_expenses, Icons.Default.MoneyOff)
    object Ingresos : Screen("ingresos", R.string.nav_income, Icons.Default.AttachMoney)
    object Chat : Screen("chat", R.string.nav_chat, Icons.Default.AutoAwesome)
    object DetalleMes : Screen("detalle_mes/{mesId}/{nombreMes}", R.string.app_name, Icons.Default.Home)
}

val itemsNavegacion = listOf(
    Screen.Inicio,
    Screen.Gastos,
    Screen.Ingresos,
    Screen.Chat,
    Screen.Trabajadores,
    Screen.Clientes
)
