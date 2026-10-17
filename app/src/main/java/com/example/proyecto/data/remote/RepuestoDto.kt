package com.example.proyecto.data.remote

enum class TipoItem { REPUESTO, SERVICIO }

data class RepuestoDto(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val foto: String? = null,
    val tipo: TipoItem
)

// Respuesta de POST y PUT: {"message": "...", "repuestoServicio": {...}}
data class RespuestaGuardadoDto(
    val message: String,
    val repuestoServicio: RepuestoDto
)

// Respuesta de DELETE: {"message": "..."}
data class MensajeDto(
    val message: String
)
