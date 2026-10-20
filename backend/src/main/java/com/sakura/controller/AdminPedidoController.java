package com.sakura.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.HistorialResponse;
import com.sakura.model.EstadoPedido;
import com.sakura.service.PedidoService;

/** Historial de pedidos para el administrador (HU-05, RF-09). */
@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

	private final PedidoService service;

	public AdminPedidoController(PedidoService service) {
		this.service = service;
	}

	@GetMapping
	public HistorialResponse historial(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer mesa,
			@RequestParam(required = false) Long usuario,
			@RequestParam(required = false) EstadoPedido estado) {
		return service.historial(desde, hasta, mesa, usuario, estado);
	}
}
