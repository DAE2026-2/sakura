package com.sakura.controller;

import java.net.URI;
import java.security.Principal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.CantidadRequest;
import com.sakura.dto.DetalleRequest;
import com.sakura.dto.EstadoPedidoRequest;
import com.sakura.dto.PedidoRequest;
import com.sakura.dto.PedidoResponse;
import com.sakura.service.PedidoService;

import jakarta.validation.Valid;

/** Pedidos y su detalle (HU-03, HU-04). Requiere sesión; PedidoService limita lo que puede hacer un cliente. */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService service;

	public PedidoController(PedidoService service) {
		this.service = service;
	}

	@GetMapping
	public List<PedidoResponse> abiertos(@RequestParam(required = false) Long mesaId, Principal principal) {
		return service.abiertos(principal.getName(), mesaId);
	}

	@GetMapping("/{id}")
	public PedidoResponse obtener(@PathVariable Long id, Principal principal) {
		return service.obtener(id, principal.getName());
	}

	@PostMapping
	public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest req, Principal principal) {
		var pedido = service.crear(req, principal.getName());
		return ResponseEntity.created(URI.create("/api/pedidos/" + pedido.id())).body(pedido);
	}

	@PatchMapping("/{id}/estado")
	public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoPedidoRequest req,
			Principal principal) {
		return service.cambiarEstado(id, req.estado(), principal.getName());
	}

	@PostMapping("/{id}/detalles")
	public PedidoResponse agregarDetalle(@PathVariable Long id, @Valid @RequestBody DetalleRequest req,
			Principal principal) {
		return service.agregarDetalle(id, req, principal.getName());
	}

	@PutMapping("/{id}/detalles/{detalleId}")
	public PedidoResponse cambiarCantidad(@PathVariable Long id, @PathVariable Long detalleId,
			@Valid @RequestBody CantidadRequest req, Principal principal) {
		return service.cambiarCantidad(id, detalleId, req.cantidad(), principal.getName());
	}

	@DeleteMapping("/{id}/detalles/{detalleId}")
	public PedidoResponse quitarDetalle(@PathVariable Long id, @PathVariable Long detalleId, Principal principal) {
		return service.quitarDetalle(id, detalleId, principal.getName());
	}
}
