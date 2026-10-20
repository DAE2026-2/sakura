package com.sakura.dto;

import java.math.BigDecimal;

import com.sakura.model.DetallePedido;

public record DetalleResponse(Long id, Long productoId, String producto, int cantidad, BigDecimal precioUnitario,
		BigDecimal subtotal) {

	public static DetalleResponse de(DetallePedido d) {
		return new DetalleResponse(d.getId(), d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
				d.getPrecioUnitario(), d.getSubtotal());
	}
}
