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

import com.sakura.dto.ProductoRequest;
import com.sakura.dto.ProductoResponse;
import com.sakura.service.ProductoService;

import jakarta.validation.Valid;

/** CRUD de productos. Solo ADMIN (SecurityConfig protege /api/admin/**). */
@RestController
@RequestMapping("/api/admin/productos")
public class AdminProductoController {

	private final ProductoService service;

	public AdminProductoController(ProductoService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProductoResponse> listar() {
		return service.listar().stream().map(ProductoResponse::de).toList();
	}

	@GetMapping("/{id}")
	public ProductoResponse obtener(@PathVariable Long id) {
		return ProductoResponse.de(service.obtener(id));
	}

	@PostMapping
	public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req) {
		var producto = service.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/productos/" + producto.getId()))
				.body(ProductoResponse.de(producto));
	}

	@PutMapping("/{id}")
	public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
		return ProductoResponse.de(service.actualizar(id, req));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
