package com.sakura.service;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.RegistroRequest;
import com.sakura.exception.ApiException;
import com.sakura.model.Rol;
import com.sakura.model.Usuario;
import com.sakura.repository.UsuarioRepository;

/** Registro de clientes y datos del usuario que ha iniciado sesión. */
@Service
public class AuthService {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder encoder;
	private final Clock clock;

	public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, Clock clock) {
		this.usuarios = usuarios;
		this.encoder = encoder;
		this.clock = clock;
	}

	@Transactional
	public Usuario registrar(RegistroRequest req) {
		String email = req.email().trim().toLowerCase();
		if (usuarios.existsByEmailIgnoreCase(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
		}
		return usuarios.save(new Usuario(req.nombre().trim(), email, encoder.encode(req.password()), Rol.CLIENTE,
				OffsetDateTime.now(clock)));
	}

	@Transactional(readOnly = true)
	public Usuario actual(String email) {
		return usuarios.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sesión no válida"));
	}
}
