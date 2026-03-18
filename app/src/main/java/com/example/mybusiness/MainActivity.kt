package com.example.mybusiness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mybusiness.ui.Screen
import com.example.mybusiness.ui.clientes.ClientesScreen
import com.example.mybusiness.ui.clientes.ClientesViewModel
import com.example.mybusiness.ui.gastos.GastosScreen
import com.example.mybusiness.ui.gastos.GastosViewModel
import com.example.mybusiness.ui.ingresos.IngresosScreen
import com.example.mybusiness.ui.ingresos.IngresosViewModel
import com.example.mybusiness.ui.inicio.InicioScreen
import com.example.mybusiness.ui.inicio.InicioViewModel
import com.example.mybusiness.ui.itemsNavegacion
import com.example.mybusiness.ui.theme.MyBusinessTheme
import com.example.mybusiness.ui.trabajadores.TrabajadoresScreen
import com.example.mybusiness.ui.trabajadores.TrabajadoresViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBusinessTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    val inicioViewModel: InicioViewModel = viewModel()
    val trabajadoresViewModel: TrabajadoresViewModel = viewModel()
    val clientesViewModel: ClientesViewModel = viewModel()
    val gastosViewModel: GastosViewModel = viewModel()
    val ingresosViewModel: IngresosViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBack by navController.currentBackStackEntryAsState()
                val destinoActual = navBack?.destination
                itemsNavegacion.forEach { pantalla ->
                    NavigationBarItem(
                        icon = { Icon(pantalla.icon, contentDescription = null) },
                        label = { Text(pantalla.title) },
                        selected = destinoActual?.hierarchy?.any { it.route == pantalla.route } == true,
                        onClick = {
                            navController.navigate(pantalla.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Inicio.route) {
                InicioScreen(
                    viewModel = inicioViewModel
                )
            }
            composable(Screen.Gastos.route) { GastosScreen(viewModel = gastosViewModel) }
            composable(Screen.Ingresos.route) { IngresosScreen(viewModel = ingresosViewModel) }
            composable(Screen.Trabajadores.route) { TrabajadoresScreen(viewModel = trabajadoresViewModel) }
            composable(Screen.Clientes.route) { ClientesScreen(viewModel = clientesViewModel) }
        }
    }
}
