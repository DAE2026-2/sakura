package com.sakura.controller;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.DisponibilidadResponse;
import com.sakura.dto.ReservaRequest;
import com.sakura.dto.ReservaResponse;
import com.sakura.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ReservaController {

	private final ReservaService service;

	public ReservaController(ReservaService service) {
		this.service = service;
	}

	@GetMapping("/disponibilidad")
	public DisponibilidadResponse disponibilidad(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
			@RequestParam(defaultValue = "2") int personas) {
		return service.disponibilidad(fecha, personas);
	}

	@PostMapping("/reservas")
	public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest req) {
		var reserva = service.crear(req);
		return ResponseEntity.created(URI.create("/api/reservas/" + reserva.getCodigo()))
				.body(ReservaResponse.de(reserva));
	}

	@GetMapping("/reservas/{codigo}")
	public ReservaResponse consultar(@PathVariable String codigo, @RequestParam String email) {
		return ReservaResponse.de(service.consultar(codigo, email));
	}

	@DeleteMapping("/reservas/{codigo}")
	public ReservaResponse cancelar(@PathVariable String codigo, @RequestParam String email) {
		return ReservaResponse.de(service.cancelar(codigo, email));
	}
}
