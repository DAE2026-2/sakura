package com.sakura.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CantidadRequest(
		@NotNull(message = "Indica la cantidad") @Min(value = 1, message = "Mínimo 1")
		@Max(value = 99, message = "Máximo 99") Integer cantidad) {
}
