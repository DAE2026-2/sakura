package com.sakura.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta de un cliente desde la web. Los meseros y administradores los crea un administrador. */
public record RegistroRequest(
		@NotBlank(message = "Escribe tu nombre") @Size(max = 80, message = "Máximo 80 caracteres") String nombre,
		@NotBlank(message = "Escribe tu email") @Email(message = "El email no es válido")
		@Size(max = 120, message = "Máximo 120 caracteres") String email,
		@NotBlank(message = "Escribe una contraseña")
		@Size(min = 8, max = 72, message = "Entre 8 y 72 caracteres") String password) {
}
