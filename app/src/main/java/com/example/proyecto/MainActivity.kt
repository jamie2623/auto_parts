package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.navigation.NavManager
import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.viewmodel.RepuestosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoTheme {
                val viewModel: RepuestosViewModel = viewModel()
                NavManager(viewModel)
            }
        }
    }
}
