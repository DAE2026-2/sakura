package com.sakura.dto;

import com.sakura.model.Rol;
import com.sakura.model.Usuario;

/** Un usuario sin su contraseña. */
public record UsuarioResponse(Long id, String nombre, String email, Rol rol, boolean activo) {

	public static UsuarioResponse de(Usuario u) {
		return new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo());
	}
}
