package com.example.proyecto.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto.data.remote.TipoItem
import com.example.proyecto.ui.views.DetalleRepuestoView
import com.example.proyecto.ui.views.FormularioRepuestoView
import com.example.proyecto.ui.views.ListaRepuestosView
import com.example.proyecto.viewmodel.RepuestosViewModel

@Composable
fun NavManager(viewModel: RepuestosViewModel) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val abrirDetalle = { id: Int -> navController.navigate("detalle/$id") }
    val abrirNuevo = { navController.navigate("formulario") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (destinosInferiores.any { it.ruta == rutaActual }) {
                NavigationBar {
                    destinosInferiores.forEach { destino ->
                        NavigationBarItem(
                            selected = rutaActual == destino.ruta,
                            onClick = {
                                navController.navigate(destino.ruta) {
                                    // Evita apilar pestañas y conserva el estado de cada una (por ejemplo, el buscador)
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                            label = { Text(destino.titulo) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Catalogo.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Las tres pestañas usan la misma pantalla, con distinto tipo
            composable(Destino.Catalogo.ruta) {
                ListaRepuestosView(viewModel, tipo = null, titulo = Destino.Catalogo.titulo,
                    onAbrirDetalle = abrirDetalle, onNuevo = abrirNuevo)
            }
            composable(Destino.Repuestos.ruta) {
                ListaRepuestosView(viewModel, tipo = TipoItem.REPUESTO, titulo = Destino.Repuestos.titulo,
                    onAbrirDetalle = abrirDetalle, onNuevo = abrirNuevo)
            }
            composable(Destino.Servicios.ruta) {
                ListaRepuestosView(viewModel, tipo = TipoItem.SERVICIO, titulo = Destino.Servicios.titulo,
                    onAbrirDetalle = abrirDetalle, onNuevo = abrirNuevo)
            }

            composable(
                route = "detalle/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: return@composable
                DetalleRepuestoView(
                    viewModel = viewModel,
                    id = id,
                    onVolver = { navController.popBackStack() },
                    onEditar = { navController.navigate("formulario?id=$id") }
                )
            }

            composable(
                route = "formulario?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1          // -1 significa "registro nuevo"
                })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id")?.takeIf { it != -1 }
                FormularioRepuestoView(
                    viewModel = viewModel,
                    id = id,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}
