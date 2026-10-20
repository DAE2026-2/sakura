package com.sakura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sakura.repository.UsuarioRepository;

/**
 * Autenticación HTTP Basic contra la tabla `usuario` (email + contraseña BCrypt) y autorización por rol (RF-08, RF-11):
 * - /api/admin/** solo ADMIN.
 * - /api/mesas/** y /api/pedidos/** cualquier usuario autenticado; cada servicio limita lo que ve un CLIENTE.
 * - El resto (menú, reservas, registro) es público.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/mesas/**", "/api/pedidos/**", "/api/auth/yo").authenticated()
						.anyRequest().permitAll())
				// 401 sin cabecera WWW-Authenticate para que el navegador no abra su propio diálogo de login.
				.httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.build();
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
