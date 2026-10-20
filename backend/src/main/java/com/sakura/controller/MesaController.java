package com.sakura.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.MesaResponse;
import com.sakura.service.MesaService;

/** Mesas con su estado para la pantalla del mesero. Requiere sesión (SecurityConfig). */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

	private final MesaService service;

	public MesaController(MesaService service) {
		this.service = service;
	}

	@GetMapping
	public List<MesaResponse> listar() {
		return service.listar();
	}
}
