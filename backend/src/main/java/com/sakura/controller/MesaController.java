package com.sakura.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.MesaResponse;
import com.sakura.service.MesaService;

/** Mesas con su estado para las pantallas del mesero y del cliente. Requiere sesión (SecurityConfig). */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

	private final MesaService service;

	public MesaController(MesaService service) {
		this.service = service;
	}

	@GetMapping
	public List<MesaResponse> listar(Authentication auth) {
		// El cliente solo sabe que una mesa está reservada; la hora y el nombre son para el personal.
		boolean cliente = auth.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
		return service.listar(!cliente);
	}
}
