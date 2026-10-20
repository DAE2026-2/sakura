package com.sakura.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sakura.dto.RegistroRequest;
import com.sakura.dto.UsuarioResponse;
import com.sakura.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService service;

	public AuthController(AuthService service) {
		this.service = service;
	}

	/** Crea una cuenta de cliente. Después se inicia sesión con HTTP Basic (email y contraseña). */
	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
		return UsuarioResponse.de(service.registrar(req));
	}

	/** Comprueba las credenciales y devuelve quién es el usuario y su rol. */
	@GetMapping("/yo")
	public UsuarioResponse yo(Principal principal) {
		return UsuarioResponse.de(service.actual(principal.getName()));
	}
}
