package com.sakura.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.AdminReservaResponse;
import com.sakura.dto.CambioEstadoRequest;
import com.sakura.dto.MesaReservaRequest;
import com.sakura.service.ReservaService;

import jakarta.validation.Valid;

/** Panel del restaurante. Protegido con HTTP Basic en SecurityConfig. */
@RestController
@RequestMapping("/api/admin/reservas")
public class AdminReservaController {

	private final ReservaService service;

	public AdminReservaController(ReservaService service) {
		this.service = service;
	}

	@GetMapping
	public List<AdminReservaResponse> delDia(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return service.delDia(fecha).stream().map(AdminReservaResponse::de).toList();
	}

	@PatchMapping("/{id}")
	public AdminReservaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest req) {
		return AdminReservaResponse.de(service.cambiarEstado(id, req.estado()));
	}

	@PutMapping("/{id}/mesa")
	public AdminReservaResponse cambiarMesa(@PathVariable Long id, @Valid @RequestBody MesaReservaRequest req) {
		return AdminReservaResponse.de(service.cambiarMesa(id, req.mesaId()));
	}
}
