package com.sakura.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Abrir un pedido en una mesa (RF-03). */
public record PedidoRequest(
		@NotNull(message = "Elige una mesa") Long mesaId,
		@Size(max = 300, message = "Máximo 300 caracteres") String notas) {
}
