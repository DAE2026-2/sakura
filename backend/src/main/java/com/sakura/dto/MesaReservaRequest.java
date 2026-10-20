package com.sakura.dto;

import jakarta.validation.constraints.NotNull;

/** Mesa a la que el administrador mueve una reserva. */
public record MesaReservaRequest(@NotNull(message = "Elige una mesa") Long mesaId) {
}
