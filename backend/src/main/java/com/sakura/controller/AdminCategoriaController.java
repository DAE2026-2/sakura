package com.sakura.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.CategoriaRequest;
import com.sakura.dto.CategoriaResponse;
import com.sakura.service.CategoriaService;

import jakarta.validation.Valid;

/** CRUD de categorías. Solo ADMIN (SecurityConfig protege /api/admin/**). */
@RestController
@RequestMapping("/api/admin/categorias")
public class AdminCategoriaController {

	private final CategoriaService service;

	public AdminCategoriaController(CategoriaService service) {
		this.service = service;
	}

	@GetMapping
	public List<CategoriaResponse> listar() {
		return service.listar().stream().map(CategoriaResponse::de).toList();
	}

	@GetMapping("/{id}")
	public CategoriaResponse obtener(@PathVariable Long id) {
		return CategoriaResponse.de(service.obtener(id));
	}

	@PostMapping
	public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest req) {
		var categoria = service.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/categorias/" + categoria.getId()))
				.body(CategoriaResponse.de(categoria));
	}

	@PutMapping("/{id}")
	public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
		return CategoriaResponse.de(service.actualizar(id, req));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
