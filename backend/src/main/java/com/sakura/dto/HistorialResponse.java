package com.sakura.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resultado del historial (RF-09): los pedidos del filtro y la suma de los pagados. */
public record HistorialResponse(List<PedidoResponse> pedidos, long cantidad, BigDecimal ventasPagadas) {
}
