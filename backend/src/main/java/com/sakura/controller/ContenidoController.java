package com.sakura.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.ExperienciaResponse;
import com.sakura.dto.FaqResponse;
import com.sakura.dto.MenuCategoriaResponse;
import com.sakura.service.ContenidoService;

@RestController
@RequestMapping("/api")
public class ContenidoController {

	private final ContenidoService service;

	public ContenidoController(ContenidoService service) {
		this.service = service;
	}

	@GetMapping("/menu")
	public List<MenuCategoriaResponse> menu() {
		return service.menu();
	}

	@GetMapping("/experiencias")
	public List<ExperienciaResponse> experiencias() {
		return service.experiencias();
	}

	@GetMapping("/faqs")
	public List<FaqResponse> faqs() {
		return service.faqs();
	}
}
