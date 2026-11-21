package com.sakura.dto;

import java.math.BigDecimal;

import com.sakura.model.Experiencia;

/** Una experiencia de la landing. DTO para que cambiar la tabla no cambie la API sin querer. */
public record ExperienciaResponse(Long id, String nombre, String descripcion, BigDecimal precio, int personas,
		String etiqueta) {

	public static ExperienciaResponse de(Experiencia e) {
		return new ExperienciaResponse(e.getId(), e.getNombre(), e.getDescripcion(), e.getPrecio(), e.getPersonas(),
				e.getEtiqueta());
	}
}
