package com.sakura.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sakura.repository.UsuarioRepository;

/**
 * Autenticación con JWT y autorización por rol (RF-08, RF-11):
 * - POST /api/auth/login comprueba email y contraseña (BCrypt) y devuelve un token firmado (AuthService).
 * - Las demás peticiones protegidas llevan {@code Authorization: Bearer <token>}.
 * - /api/admin/** solo ADMIN; /api/mesas/** y /api/pedidos/** cualquier usuario con sesión, y cada servicio limita
 *   lo que ve un CLIENTE. El resto (menú, reservas, registro, login) es público.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, Converter<Jwt, AbstractAuthenticationToken> usuarioDelToken)
			throws Exception {
		return http
				.csrf(csrf -> csrf.disable()) // sin cookies de sesión no hay CSRF: el token va en una cabecera
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/mesas/**", "/api/pedidos/**", "/api/auth/yo").authenticated()
						.anyRequest().permitAll())
				.oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(usuarioDelToken)))
				.build();
	}

	/**
	 * Del token solo se usa el email: el rol y si está activo se leen de la base en cada petición, así un usuario
	 * desactivado o con otro rol deja de poder usar su token al momento, sin esperar a que caduque.
	 */
	@Bean
	Converter<Jwt, AbstractAuthenticationToken> usuarioDelToken(UserDetailsService usuarios) {
		return jwt -> {
			var usuario = usuarios.loadUserByUsername(jwt.getSubject());
			if (!usuario.isEnabled()) throw new DisabledException("Usuario desactivado");
			return new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, usuario.getAuthorities());
		};
	}

	@Bean
	SecretKey claveJwt(SakuraProperties props) {
		var secreto = props.jwt().secreto();
		if (secreto == null || secreto.length() < 32) {
			throw new IllegalStateException("sakura.jwt.secreto (JWT_SECRET) debe tener al menos 32 caracteres");
		}
		return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey clave) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey clave) {
		return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
	}

	/** Comprueba email y contraseña en el login; rechaza usuarios desactivados. */
	@Bean
	AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder encoder) {
		var provider = new DaoAuthenticationProvider(usuarios);
		provider.setPasswordEncoder(encoder);
		return new ProviderManager(provider);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	UserDetailsService usuarios(UsuarioRepository repo) {
		return email -> repo.findByEmailIgnoreCase(email)
				.map(u -> User.withUsername(u.getEmail())
						.password(u.getPasswordHash())
						.roles(u.getRol().name())
						.disabled(!u.isActivo())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException(email));
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(SakuraProperties props) {
		var config = new CorsConfiguration();
		config.setAllowedOrigins(props.cors().origenes());
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}
}
