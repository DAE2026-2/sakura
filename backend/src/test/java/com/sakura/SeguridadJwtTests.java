package com.sakura;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import com.sakura.repository.UsuarioRepository;

/** Inicio de sesión con JWT y permisos por rol (#39, #21), con los usuarios de data-local.sql. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadJwtTests {

	@Autowired MockMvc mvc;
	@Autowired UsuarioRepository usuarios;

	String token(String email, String password) throws Exception {
		var body = mvc.perform(login(email, password)).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	static org.springframework.test.web.servlet.RequestBuilder login(String email, String password) {
		return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password));
	}

	static String bearer(String token) {
		return "Bearer " + token;
	}

	@Test
	void elLoginDevuelveUnTokenYQuienEsElUsuario() throws Exception {
		mvc.perform(login("ADMIN@sakura.example", "sakura123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
				.andExpect(jsonPath("$.usuario.email").value("admin@sakura.example"));
	}

	@Test
	void conContrasenaIncorrectaOEmailDesconocidoResponde401ConMensaje() throws Exception {
		mvc.perform(login("admin@sakura.example", "mala")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensaje").value("Email o contraseña incorrectos"));
		mvc.perform(login("nadie@sakura.example", "sakura123")).andExpect(status().isUnauthorized());
	}

	@Test
	void yoFuncionaConElTokenYNoSinElNiConHttpBasic() throws Exception {
		var token = token("mesero@sakura.example", "mesero123");
		mvc.perform(get("/api/auth/yo").header("Authorization", bearer(token)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.rol").value("MESERO"));
		mvc.perform(get("/api/auth/yo")).andExpect(status().isUnauthorized());
		var basic = Base64.getEncoder().encodeToString("mesero@sakura.example:mesero123".getBytes());
		mvc.perform(get("/api/auth/yo").header("Authorization", "Basic " + basic)).andExpect(status().isUnauthorized());
	}

	@Test
	void unTokenAlteradoNoSirve() throws Exception {
		var token = token("cliente@sakura.example", "cliente123");
		// Cambia la firma: el servidor tiene que rechazarlo aunque el contenido sea el mismo.
		var alterado = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");
		mvc.perform(get("/api/auth/yo").header("Authorization", bearer(alterado))).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/auth/yo").header("Authorization", bearer("no-es-un-jwt"))).andExpect(status().isUnauthorized());
	}

	@Test
	void cadaRolSoloEntraASusRutas() throws Exception {
		var admin = bearer(token("admin@sakura.example", "sakura123"));
		var mesero = bearer(token("mesero@sakura.example", "mesero123"));
		var cliente = bearer(token("cliente@sakura.example", "cliente123"));

		mvc.perform(get("/api/admin/usuarios").header("Authorization", admin)).andExpect(status().isOk());
		mvc.perform(get("/api/admin/usuarios").header("Authorization", mesero)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/productos").header("Authorization", cliente)).andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());

		mvc.perform(get("/api/pedidos").header("Authorization", mesero)).andExpect(status().isOk());
		mvc.perform(get("/api/mesas").header("Authorization", cliente)).andExpect(status().isOk());
		mvc.perform(get("/api/menu")).andExpect(status().isOk());
	}

	@Test
	void unUsuarioDesactivadoNoEntraNiPuedeSeguirUsandoSuToken() throws Exception {
		var token = token("mesero@sakura.example", "mesero123");
		usuarios.findByEmailIgnoreCase("mesero@sakura.example").orElseThrow().setActivo(false);
		usuarios.flush();

		mvc.perform(get("/api/pedidos").header("Authorization", bearer(token))).andExpect(status().isUnauthorized());
		mvc.perform(login("mesero@sakura.example", "mesero123")).andExpect(status().isUnauthorized());
	}

	@Test
	void elRegistroSiguePublicoYLuegoSePuedeEntrar() throws Exception {
		mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\":\"Lucía\",\"email\":\"lucia@example.com\",\"password\":\"lucia1234\"}"))
				.andExpect(status().isCreated());
		mvc.perform(login("lucia@example.com", "lucia1234")).andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
	}
}
