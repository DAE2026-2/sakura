package com.sakura.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.config.SakuraProperties;
import com.sakura.dto.LoginRequest;
import com.sakura.dto.LoginResponse;
import com.sakura.dto.RegistroRequest;
import com.sakura.dto.UsuarioResponse;
import com.sakura.exception.ApiException;
import com.sakura.model.Rol;
import com.sakura.model.Usuario;
import com.sakura.repository.UsuarioRepository;

/** Inicio de sesión con JWT, registro de clientes y datos del usuario que ha iniciado sesión. */
@Service
public class AuthService {

	private final UsuarioRepository usuarios;
	private final PasswordEncoder encoder;
	private final Clock clock;
	private final AuthenticationManager authManager;
	private final JwtEncoder jwt;
	private final Duration duracion;

	public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, Clock clock,
			AuthenticationManager authManager, JwtEncoder jwt, SakuraProperties props) {
		this.usuarios = usuarios;
		this.encoder = encoder;
		this.clock = clock;
		this.authManager = authManager;
		this.jwt = jwt;
		this.duracion = Duration.ofHours(props.jwt().horas());
	}

	/** Comprueba email y contraseña y firma un token con el email como sujeto, válido {@code sakura.jwt.horas}. */
	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest req) {
		String email = req.email().trim();
		try {
			authManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, req.password()));
		} catch (AuthenticationException e) {
			// Mismo mensaje para email desconocido, contraseña mala o usuario desactivado: no se revela cuál falló.
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
		}
		var usuario = actual(email);
		var ahora = Instant.now(clock);
		var claims = JwtClaimsSet.builder()
				.subject(usuario.getEmail())
				.issuedAt(ahora)
				.expiresAt(ahora.plus(duracion))
				.claim("rol", usuario.getRol().name()) // informativo para el frontend; el servidor lo lee de la base
				.build();
		var token = jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims));
		return new LoginResponse(token.getTokenValue(), UsuarioResponse.de(usuario));
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
