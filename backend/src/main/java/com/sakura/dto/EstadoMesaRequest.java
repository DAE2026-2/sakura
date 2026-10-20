package com.sakura.dto;

import com.sakura.model.EstadoMesa;

import jakarta.validation.constraints.NotNull;

public record EstadoMesaRequest(@NotNull(message = "Indica el estado") EstadoMesa estado) {
}
