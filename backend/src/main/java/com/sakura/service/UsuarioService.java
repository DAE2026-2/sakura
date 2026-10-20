package com.sakura.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.UsuarioRequest;
import com.sakura.dto.UsuarioResponse;
import com.sakura.exception.ApiException;
import com.sakura.model.Rol;
import com.sakura.model.Usuario;
import com.sakura.repository.PedidoRepository;
import com.sakura.repository.UsuarioRepository;

/** CRUD de usuarios para el administrador (RF-08, HU-07). Las contraseñas se guardan con BCrypt. */
@Service
public class UsuarioService {

	private final UsuarioRepository usuarios;
	private final PedidoRepository pedidos;
	private final PasswordEncoder encoder;
	private final Clock clock;

	public UsuarioService(UsuarioRepository usuarios, PedidoRepository pedidos, PasswordEncoder encoder, Clock clock) {
		this.usuarios = usuarios;
		this.pedidos = pedidos;
		this.encoder = encoder;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> listar() {
		return usuarios.findAllByOrderByRolAscNombreAsc().stream().map(UsuarioResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public UsuarioResponse obtener(Long id) {
		return UsuarioResponse.de(buscar(id));
	}

	@Transactional
	public UsuarioResponse crear(UsuarioRequest req) {
		String email = req.email().trim().toLowerCase();
		if (usuarios.existsByEmailIgnoreCase(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
		}
		validarPassword(req, true);
		var usuario = new Usuario(req.nombre().trim(), email, encoder.encode(req.password()), req.rol(),
				OffsetDateTime.now(clock));
		usuario.setActivo(req.estaActivo());
		return UsuarioResponse.de(usuarios.save(usuario));
	}

	/** El administrador no puede quitarse a sí mismo el rol ADMIN ni desactivarse (se quedaría sin acceso). */
	@Transactional
	public UsuarioResponse actualizar(Long id, UsuarioRequest req, String emailActual) {
		var usuario = buscar(id);
		String email = req.email().trim().toLowerCase();
		usuarios.findByEmailIgnoreCase(email)
				.filter(otro -> !otro.getId().equals(id))
				.ifPresent(otro -> {
					throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
				});
		if (usuario.getEmail().equalsIgnoreCase(emailActual) && (req.rol() != Rol.ADMIN || !req.estaActivo())) {
			throw new ApiException(HttpStatus.CONFLICT, "No puedes quitarte el rol de administrador ni desactivarte");
		}
		validarPassword(req, false);
		usuario.setNombre(req.nombre().trim());
		usuario.setEmail(email);
		usuario.setRol(req.rol());
		usuario.setActivo(req.estaActivo());
		if (req.tienePassword()) usuario.setPasswordHash(encoder.encode(req.password()));
		return UsuarioResponse.de(usuario);
	}

	/** No se borra a uno mismo ni a quien tiene pedidos registrados (se desactiva en su lugar). */
	@Transactional
	public void eliminar(Long id, String emailActual) {
		var usuario = buscar(id);
		if (usuario.getEmail().equalsIgnoreCase(emailActual)) {
			throw new ApiException(HttpStatus.CONFLICT, "No puedes eliminar tu propio usuario");
		}
		if (pedidos.existsByUsuarioId(id)) {
			throw new ApiException(HttpStatus.CONFLICT, "El usuario tiene pedidos registrados. Desactívalo en lugar de eliminarlo");
		}
		usuarios.delete(usuario);
	}

	private void validarPassword(UsuarioRequest req, boolean obligatoria) {
		if (!req.tienePassword()) {
			if (obligatoria) throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe una contraseña");
			return;
		}
		if (req.password().length() < 8) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 8 caracteres");
		}
	}

	private Usuario buscar(Long id) {
		return usuarios.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe ese usuario"));
	}
}
