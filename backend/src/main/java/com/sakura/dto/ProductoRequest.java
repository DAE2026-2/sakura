package com.sakura.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar un producto del menú (RF-01). */
public record ProductoRequest(
		@NotNull(message = "Elige una categoría") Long categoriaId,
		@NotBlank(message = "Escribe el nombre") @Size(max = 120, message = "Máximo 120 caracteres") String nombre,
		@Size(max = 300, message = "Máximo 300 caracteres") String descripcion,
		@NotNull(message = "Indica el precio") @DecimalMin(value = "0", message = "El precio no puede ser negativo")
		@Digits(integer = 8, fraction = 2, message = "Precio demasiado alto") BigDecimal precio,
		Boolean disponible,
		Boolean destacado,
		@Min(value = 0, message = "El orden no puede ser negativo") Integer orden) {

	/** Si no se indica, un producto nuevo está disponible. */
	public boolean esDisponible() {
		return disponible == null || disponible;
	}

	public boolean esDestacado() {
		return Boolean.TRUE.equals(destacado);
	}

	public int ordenOCero() {
		return orden == null ? 0 : orden;
	}
}
