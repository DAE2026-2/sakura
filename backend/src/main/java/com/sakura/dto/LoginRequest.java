package com.sakura.dto;

import jakarta.validation.constraints.NotBlank;

/** Email y contraseña para POST /api/auth/login. */
public record LoginRequest(
		@NotBlank(message = "Escribe tu email") String email,
		@NotBlank(message = "Escribe tu contraseña") String password) {
}
