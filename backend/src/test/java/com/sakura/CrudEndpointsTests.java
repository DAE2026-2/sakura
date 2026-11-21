package com.sakura;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/** Cada CRUD de punta a punta por HTTP con un token real (#21): crear, consultar, editar y eliminar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CrudEndpointsTests {

	@Autowired MockMvc mvc;
	String admin;
	String mesero;

	@BeforeEach
	void entrar() throws Exception {
		admin = token("admin@sakura.example", "sakura123");
		mesero = token("mesero@sakura.example", "mesero123");
	}

	String token(String email, String password) throws Exception {
		var body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	ResultActions pedir(MockHttpServletRequestBuilder req, String token, String json) throws Exception {
		req.header("Authorization", "Bearer " + token);
		if (json != null) req.contentType(MediaType.APPLICATION_JSON).content(json);
		return mvc.perform(req);
	}

	int id(ResultActions r) throws Exception {
		return JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.id");
	}

	/** Recorre crear → consultar → editar → eliminar → 404 sobre un recurso de /api/admin. */
	void crud(String ruta, String alta, String edicion, String campo, Object valorEditado) throws Exception {
		int id = id(pedir(post(ruta), admin, alta).andExpect(status().isCreated()));
		pedir(get(ruta + "/" + id), admin, null).andExpect(status().isOk());
		pedir(put(ruta + "/" + id), admin, edicion).andExpect(status().isOk())
				.andExpect(jsonPath("$." + campo).value(valorEditado));
		pedir(delete(ruta + "/" + id), admin, null).andExpect(status().isNoContent());
		pedir(get(ruta + "/" + id), admin, null).andExpect(status().isNotFound());
	}

	@Test
	void crudDeCategorias() throws Exception {
		crud("/api/admin/categorias", "{\"nombre\":\"Ramen\"}", "{\"nombre\":\"Ramen y udon\"}", "nombre", "Ramen y udon");
	}

	@Test
	void crudDeProductos() throws Exception {
		crud("/api/admin/productos", "{\"categoriaId\":1,\"nombre\":\"Tempura\",\"precio\":20000}",
				"{\"categoriaId\":1,\"nombre\":\"Tempura\",\"precio\":25000}", "precio", 25000);
	}

	@Test
	void crudDeMesas() throws Exception {
		crud("/api/admin/mesas", "{\"numero\":50,\"capacidad\":4}", "{\"numero\":50,\"capacidad\":6}", "capacidad", 6);
	}

	@Test
	void crudDeUsuarios() throws Exception {
		crud("/api/admin/usuarios", "{\"nombre\":\"Pepe\",\"email\":\"pepe@sakura.example\",\"password\":\"pepe12345\",\"rol\":\"MESERO\"}",
				"{\"nombre\":\"Pepe\",\"email\":\"pepe@sakura.example\",\"rol\":\"ADMIN\"}", "rol", "ADMIN");
	}

	@Test
	void losDatosMalosDevuelven400ConElErrorDeCadaCampo() throws Exception {
		pedir(post("/api/admin/mesas"), admin, "{\"capacidad\":0}").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.numero").exists())
				.andExpect(jsonPath("$.campos.capacidad").exists());
	}

	@Test
	void pedidoCompletoDelMeseroConTotalAutomaticoYMesaLiberada() throws Exception {
		int mesa = id(pedir(post("/api/admin/mesas"), admin, "{\"numero\":51,\"capacidad\":2}"));
		int pedido = id(pedir(post("/api/pedidos"), mesero, "{\"mesaId\":%d}".formatted(mesa)).andExpect(status().isCreated()));
		pedir(get("/api/admin/mesas/" + mesa), admin, null).andExpect(jsonPath("$.estado").value("OCUPADA"));

		// Producto 1: Nigiri de toro flameado, 38.000 → 2 unidades = 76.000.
		pedir(post("/api/pedidos/%d/detalles".formatted(pedido)), mesero, "{\"productoId\":1,\"cantidad\":2}")
				.andExpect(jsonPath("$.total").value(76000));
		for (var estado : new String[] { "EN_PREPARACION", "SERVIDO", "PAGADO" }) {
			pedir(patch("/api/pedidos/%d/estado".formatted(pedido)), mesero, "{\"estado\":\"%s\"}".formatted(estado))
					.andExpect(status().isOk());
		}
		pedir(get("/api/admin/mesas/" + mesa), admin, null).andExpect(jsonPath("$.estado").value("DISPONIBLE"));
		pedir(get("/api/admin/pedidos?mesa=51"), admin, null).andExpect(jsonPath("$.ventasPagadas").value(76000));
	}
}
