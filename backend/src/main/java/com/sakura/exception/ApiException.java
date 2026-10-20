package com.sakura.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio (reservas, carta…) con el código HTTP que debe recibir el cliente y un mensaje listo para mostrar. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String mensaje) {
		super(mensaje);
		this.status = status;
	}

	public static ApiException noEncontrada() {
		return new ApiException(HttpStatus.NOT_FOUND, "No encontramos ninguna reserva con esos datos");
	}

	public HttpStatus getStatus() {
		return status;
	}
}
