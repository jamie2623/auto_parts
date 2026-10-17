package com.example.proyecto.ui.views

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.proyecto.data.remote.ApiConfig
import com.example.proyecto.data.remote.RepuestoDto
import com.example.proyecto.data.remote.TipoItem
import com.example.proyecto.repository.ImagenSeleccionada
import com.example.proyecto.viewmodel.RepuestosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioRepuestoView(
    viewModel: RepuestosViewModel,
    id: Int?,                 // null = nuevo, valor = editar
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val detalle by viewModel.detalle.collectAsStateWithLifecycle()
    val guardando by viewModel.guardando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioTexto by remember { mutableStateOf("") }
    var stockTexto by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoItem.REPUESTO) }
    var fotoActual by remember { mutableStateOf<String?>(null) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var imagen by remember { mutableStateOf<ImagenSeleccionada?>(null) }

    // Al editar: pedir el ítem al servidor (GET por id) y llenar los campos
    LaunchedEffect(id) {
        if (id != null) viewModel.cargarDetalle(id) else viewModel.limpiarDetalle()
    }
    LaunchedEffect(detalle) {
        detalle?.takeIf { it.id == id }?.let {
            nombre = it.nombre
            descripcion = it.descripcion.orEmpty()
            precioTexto = it.precio.toString()
            stockTexto = it.stock.toString()
            tipo = it.tipo
            fotoActual = it.foto
        }
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    // Galería: lee los bytes de la imagen elegida fuera del hilo principal
    val selector = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imagenUri = uri
            scope.launch {
                imagen = withContext(Dispatchers.IO) {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    context.contentResolver.openInputStream(uri)
                        ?.use { ImagenSeleccionada(it.readBytes(), mime) }
                }
            }
        }
    }

    val precio = precioTexto.toDoubleOrNull()
    val stock = if (tipo == TipoItem.SERVICIO) 0 else stockTexto.toIntOrNull()
    val valido = nombre.isNotBlank() && precio != null && precio > 0 &&
        stock != null && stock >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "Nuevo registro" else "Editar registro") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vista previa: la foto recién elegida o, si no hay, la que ya tiene en el servidor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val modelo: Any? = imagenUri ?: ApiConfig.urlFoto(fotoActual)
                if (modelo != null) {
                    AsyncImage(
                        model = modelo,
                        contentDescription = "Foto",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Sin foto")
                }
            }
            OutlinedButton(
                onClick = {
                    selector.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (imagenUri == null) "Elegir foto" else "Cambiar foto") }

            // Tipo
            Row(verticalAlignment = Alignment.CenterVertically) {
                TipoItem.entries.forEach { opcion ->
                    RadioButton(selected = tipo == opcion, onClick = { tipo = opcion })
                    Text(
                        opcion.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { if (it.length <= 100) nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descripcion,
                onValueChange = { if (it.length <= 250) descripcion = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = precioTexto,
                onValueChange = { precioTexto = it },
                label = { Text("Precio") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            if (tipo == TipoItem.REPUESTO) {
                OutlinedTextField(
                    value = stockTexto,
                    onValueChange = { stockTexto = it },
                    label = { Text("Stock") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                enabled = valido && !guardando,
                onClick = {
                    val dto = RepuestoDto(
                        id = id,
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim().ifBlank { null },
                        precio = precio!!,
                        stock = stock!!,
                        foto = fotoActual,
                        tipo = tipo
                    )
                    viewModel.guardar(id, dto, imagen, onVolver)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (guardando) "Guardando..." else "Guardar") }
        }
    }
}
