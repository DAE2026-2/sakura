package com.sakura.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe recibir el cliente y un mensaje listo para mostrar. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String mensaje) {
		super(mensaje);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
