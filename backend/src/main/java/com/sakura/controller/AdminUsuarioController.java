package com.sakura.controller;

import java.net.URI;
import java.security.Principal;
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

import com.sakura.dto.UsuarioRequest;
import com.sakura.dto.UsuarioResponse;
import com.sakura.service.UsuarioService;

import jakarta.validation.Valid;

/** CRUD de usuarios y roles (HU-07, RF-08). Solo ADMIN. */
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

	private final UsuarioService service;

	public AdminUsuarioController(UsuarioService service) {
		this.service = service;
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public UsuarioResponse obtener(@PathVariable Long id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest req) {
		var usuario = service.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/usuarios/" + usuario.id())).body(usuario);
	}

	@PutMapping("/{id}")
	public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req,
			Principal principal) {
		return service.actualizar(id, req, principal.getName());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id, Principal principal) {
		service.eliminar(id, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
