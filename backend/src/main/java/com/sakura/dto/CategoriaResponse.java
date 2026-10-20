package com.sakura.dto;

import com.sakura.model.Categoria;

public record CategoriaResponse(Long id, String nombre, String descripcion, int orden) {

	public static CategoriaResponse de(Categoria c) {
		return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getOrden());
	}
}
