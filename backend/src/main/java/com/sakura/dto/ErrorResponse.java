package com.sakura.dto;

import java.util.Map;

/** Cuerpo de todas las respuestas de error: un mensaje para mostrar y, si aplica, el error de cada campo. */
public record ErrorResponse(String mensaje, Map<String, String> campos) {

	public ErrorResponse(String mensaje) {
		this(mensaje, null);
	}
}
