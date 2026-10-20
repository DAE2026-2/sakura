package com.sakura.dto;

import com.sakura.model.EstadoReserva;

import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@NotNull(message = "Indica el estado") EstadoReserva estado) {
}
