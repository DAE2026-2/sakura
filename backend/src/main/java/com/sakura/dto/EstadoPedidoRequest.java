package com.sakura.dto;

import com.sakura.model.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record EstadoPedidoRequest(@NotNull(message = "Indica el estado") EstadoPedido estado) {
}
