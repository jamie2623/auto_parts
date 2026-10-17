package com.example.proyecto.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.remote.RepuestoDto
import com.example.proyecto.repository.ImagenSeleccionada
import com.example.proyecto.repository.RepuestoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListaUiState(
    val cargando: Boolean = false,
    val repuestos: List<RepuestoDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class RepuestosViewModel @Inject constructor(
    private val repositorio: RepuestoRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ListaUiState())
    val estado: StateFlow<ListaUiState> = _estado.asStateFlow()

    // Ítem que muestran el detalle y el formulario de edición
    private val _detalle = MutableStateFlow<RepuestoDto?>(null)
    val detalle: StateFlow<RepuestoDto?> = _detalle.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            repositorio.obtenerTodos()
                .onSuccess { lista ->
                    _estado.update { it.copy(cargando = false, repuestos = lista) }
                }
                .onFailure { e ->
                    _estado.update { it.copy(cargando = false, error = e.message) }
                }
        }
    }

    // GET /repuestos-servicios/{id}: datos frescos del servidor para el detalle y la edición
    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            repositorio.obtenerPorId(id)
                .onSuccess { _detalle.value = it }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun limpiarDetalle() { _detalle.value = null }

    // id == null: crea (POST); id != null: edita (PUT)
    fun guardar(
        id: Int?,
        dto: RepuestoDto,
        imagen: ImagenSeleccionada?,
        alTerminar: () -> Unit
    ) {
        viewModelScope.launch {
            _guardando.value = true
            val resultado =
                if (id == null) repositorio.crear(dto, imagen)
                else repositorio.actualizar(id, dto, imagen)
            _guardando.value = false

            resultado
                .onSuccess { guardado ->
                    _detalle.value = guardado
                    _mensaje.value =
                        if (id == null) "Registrado correctamente" else "Actualizado correctamente"
                    cargar()
                    alTerminar()
                }
                .onFailure { _mensaje.value = it.message }
        }
    }

    // avisa cuando termina bien, para que el detalle pueda volver a la lista
    fun eliminar(id: Int, alTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            repositorio.eliminar(id)
                .onSuccess { mensaje ->
                    _mensaje.value = mensaje
                    cargar()
                    alTerminar()
                }
                .onFailure { _mensaje.value = it.message }
        }
    }

    fun mensajeMostrado() { _mensaje.value = null }
}
