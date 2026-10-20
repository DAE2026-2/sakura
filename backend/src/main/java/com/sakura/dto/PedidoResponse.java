package com.sakura.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.sakura.model.EstadoPedido;
import com.sakura.model.Pedido;

/** Un pedido con su mesa, quién lo registró y el detalle con subtotales (RF-05, RF-06). */
public record PedidoResponse(
		Long id,
		Long mesaId,
		int mesa,
		String usuario,
		EstadoPedido estado,
		BigDecimal total,
		String notas,
		OffsetDateTime creadoEn,
		OffsetDateTime actualizadoEn,
		List<DetalleResponse> detalles) {

	public static PedidoResponse de(Pedido p) {
		return new PedidoResponse(p.getId(), p.getMesa().getId(), p.getMesa().getNumero(),
				p.getUsuario() == null ? null : p.getUsuario().getNombre(), p.getEstado(), p.getTotal(), p.getNotas(),
				p.getCreadoEn(), p.getActualizadoEn(), p.getDetalles().stream().map(DetalleResponse::de).toList());
	}
}
