package com.sakura.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar una mesa (RF-07). El estado lo cambian los pedidos o PATCH /estado. */
public record MesaRequest(
		@NotNull(message = "Indica el número") @Min(value = 1, message = "El número debe ser mayor que 0") Integer numero,
		@NotNull(message = "Indica la capacidad") @Min(value = 1, message = "Mínimo 1 persona")
		@Max(value = 20, message = "Máximo 20 personas") Integer capacidad,
		@Size(max = 40, message = "Máximo 40 caracteres") String ubicacion) {
}
