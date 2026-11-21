package com.sakura.dto;

/** Token JWT para la cabecera {@code Authorization: Bearer <token>} y el usuario que ha entrado. */
public record LoginResponse(String token, UsuarioResponse usuario) {
}
