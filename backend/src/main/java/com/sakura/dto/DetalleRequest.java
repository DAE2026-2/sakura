package com.sakura.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Añadir un producto al pedido (RF-04). Si ya estaba, se suma la cantidad. */
public record DetalleRequest(
		@NotNull(message = "Elige un producto") Long productoId,
		@NotNull(message = "Indica la cantidad") @Min(value = 1, message = "Mínimo 1")
		@Max(value = 99, message = "Máximo 99") Integer cantidad) {
}
