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

import com.example.mybusiness.ui.chat.ChatScreen
import com.example.mybusiness.ui.chat.ChatViewModel
import com.example.mybusiness.ui.inicio.DetalleMesScreen
import com.example.mybusiness.ui.inicio.DetalleMesViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.mybusiness.ui.PreferenciasViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefViewModel: PreferenciasViewModel = viewModel()
            val darkTheme = when (prefViewModel.modoOscuro) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }
            
            MyBusinessTheme(darkTheme = darkTheme) {
                MainApp(prefViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(prefViewModel: PreferenciasViewModel) {
    val navController = rememberNavController()
    var mostrarConfig by remember { mutableStateOf(false) }

    val inicioViewModel: InicioViewModel = viewModel()
    val trabajadoresViewModel: TrabajadoresViewModel = viewModel()
    val clientesViewModel: ClientesViewModel = viewModel()
    val gastosViewModel: GastosViewModel = viewModel()
    val ingresosViewModel: IngresosViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = prefViewModel.nombreEmpresa,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { mostrarConfig = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
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
                        label = { Text(stringResource(pantalla.titleRes)) },
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
                    viewModel = inicioViewModel,
                    onVerDetalleMes = { id, nombre ->
                        navController.navigate("detalle_mes/$id/$nombre")
                    }
                )
            }
            composable(Screen.Gastos.route) { GastosScreen(viewModel = gastosViewModel) }
            composable(Screen.Ingresos.route) { IngresosScreen(viewModel = ingresosViewModel) }
            composable(Screen.Trabajadores.route) { TrabajadoresScreen(viewModel = trabajadoresViewModel) }
            composable(Screen.Clientes.route) { ClientesScreen(viewModel = clientesViewModel) }
            composable(Screen.Chat.route) { ChatScreen(viewModel = chatViewModel) }
            composable(
                route = Screen.DetalleMes.route,
                arguments = listOf(
                    navArgument("mesId") { type = NavType.IntType },
                    navArgument("nombreMes") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val mesId = backStackEntry.arguments?.getInt("mesId") ?: 0
                val nombreMes = backStackEntry.arguments?.getString("nombreMes") ?: ""
                val detalleViewModel: DetalleMesViewModel = viewModel()
                DetalleMesScreen(
                    mesId = mesId,
                    nombreMes = nombreMes,
                    viewModel = detalleViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        if (mostrarConfig) {
            DialogoConfiguracion(
                prefViewModel = prefViewModel,
                inicioViewModel = inicioViewModel,
                onDismiss = { mostrarConfig = false }
            )
        }
    }
}

@Composable
fun DialogoConfiguracion(
    prefViewModel: PreferenciasViewModel,
    inicioViewModel: InicioViewModel,
    onDismiss: () -> Unit
) {
    var nombreTmp by remember { mutableStateOf(prefViewModel.nombreEmpresa) }
    val monedas = listOf("€", "$", "£", "¥", "MXN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = nombreTmp,
                    onValueChange = { nombreTmp = it },
                    label = { Text(stringResource(R.string.company_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(stringResource(R.string.currency_symbol), fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(monedas) { simbolo ->
                        FilterChip(
                            selected = prefViewModel.simboloMoneda == simbolo,
                            onClick = { prefViewModel.guardarSimboloMoneda(simbolo) },
                            label = { Text(simbolo) }
                        )
                    }
                }

                Text(stringResource(R.string.app_theme), fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = prefViewModel.modoOscuro == false,
                        onClick = { prefViewModel.guardarModoOscuro(false) },
                        label = { Text(stringResource(R.string.light)) }
                    )
                    FilterChip(
                        selected = prefViewModel.modoOscuro == true,
                        onClick = { prefViewModel.guardarModoOscuro(true) },
                        label = { Text(stringResource(R.string.dark)) }
                    )
                    FilterChip(
                        selected = prefViewModel.modoOscuro == null,
                        onClick = { prefViewModel.guardarModoOscuro(null) },
                        label = { Text(stringResource(R.string.system)) }
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.auto_month_closure), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.auto_month_closure_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = prefViewModel.cierreAutomatico,
                        onCheckedChange = { 
                            prefViewModel.guardarCierreAutomatico(it)
                            if (it) {
                                inicioViewModel.comprobarCierreMesAutomatico()
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                prefViewModel.guardarNombreEmpresa(nombreTmp)
                onDismiss()
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
