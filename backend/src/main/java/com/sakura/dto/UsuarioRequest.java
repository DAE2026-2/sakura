package com.sakura.dto;

import com.sakura.model.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alta o edición de un usuario por un administrador (RF-08). Al editar, la contraseña vacía no se cambia. */
public record UsuarioRequest(
		@NotBlank(message = "Escribe el nombre") @Size(max = 80, message = "Máximo 80 caracteres") String nombre,
		@NotBlank(message = "Escribe el email") @Email(message = "El email no es válido")
		@Size(max = 120, message = "Máximo 120 caracteres") String email,
		@Size(max = 72, message = "Máximo 72 caracteres") String password,
		@NotNull(message = "Elige un rol") Rol rol,
		Boolean activo) {

	public boolean estaActivo() {
		return activo == null || activo;
	}

	public boolean tienePassword() {
		return password != null && !password.isBlank();
	}
}
