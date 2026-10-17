package com.example.autopartsapi;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Mismos campos y nombres que el RepuestoDto de la app (Gson/Jackson relacionan por nombre). */
public record RepuestoServicioDTO(
        Integer id,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "El nombre admite maximo 100 caracteres") String nombre,
        @Size(max = 250, message = "La descripcion admite maximo 250 caracteres") String descripcion,
        @NotNull(message = "El precio es obligatorio") @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que 0") BigDecimal precio,
        @NotNull(message = "El stock es obligatorio") @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        String foto,
        @NotNull(message = "El tipo es obligatorio") TipoItem tipo
) {
    public static RepuestoServicioDTO desde(RepuestoServicio e) {
        return new RepuestoServicioDTO(e.getId(), e.getNombre(), e.getDescripcion(),
                e.getPrecio(), e.getStock(), e.getFoto(), e.getTipo());
    }
}
