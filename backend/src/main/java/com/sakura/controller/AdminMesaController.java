package com.sakura.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.EstadoMesaRequest;
import com.sakura.dto.MesaRequest;
import com.sakura.dto.MesaResponse;
import com.sakura.service.MesaService;

import jakarta.validation.Valid;

/** CRUD de mesas. Solo ADMIN (SecurityConfig protege /api/admin/**). */
@RestController
@RequestMapping("/api/admin/mesas")
public class AdminMesaController {

	private final MesaService service;

	public AdminMesaController(MesaService service) {
		this.service = service;
	}

	@GetMapping
	public List<MesaResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public MesaResponse obtener(@PathVariable Long id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<MesaResponse> crear(@Valid @RequestBody MesaRequest req) {
		var mesa = service.crear(req);
		return ResponseEntity.created(URI.create("/api/admin/mesas/" + mesa.id())).body(mesa);
	}

	@PutMapping("/{id}")
	public MesaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MesaRequest req) {
		return service.actualizar(id, req);
	}

	@PatchMapping("/{id}/estado")
	public MesaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoMesaRequest req) {
		return service.cambiarEstado(id, req.estado());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
