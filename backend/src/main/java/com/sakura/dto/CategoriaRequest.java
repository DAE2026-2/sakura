package com.sakura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o editar una categoría del menú. */
public record CategoriaRequest(
		@NotBlank(message = "Escribe el nombre") @Size(max = 40, message = "Máximo 40 caracteres") String nombre,
		@Size(max = 200, message = "Máximo 200 caracteres") String descripcion,
		@Min(value = 0, message = "El orden no puede ser negativo") Integer orden) {

	public int ordenOCero() {
		return orden == null ? 0 : orden;
	}
}
