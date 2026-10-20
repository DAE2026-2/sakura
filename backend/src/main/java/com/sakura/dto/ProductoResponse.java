package com.sakura.dto;

import java.math.BigDecimal;

import com.sakura.model.Producto;

/** Un producto tal como lo devuelve la API, con el nombre de su categoría. */
public record ProductoResponse(
		Long id,
		String nombre,
		String descripcion,
		BigDecimal precio,
		boolean disponible,
		boolean destacado,
		int orden,
		Long categoriaId,
		String categoria,
		String imagen) {

	public static ProductoResponse de(Producto p) {
		return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.isDisponible(),
				p.isDestacado(), p.getOrden(), p.getCategoria().getId(), p.getCategoria().getNombre(), p.getImagen());
	}
}
