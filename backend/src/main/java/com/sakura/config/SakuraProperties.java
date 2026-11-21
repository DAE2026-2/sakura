package com.sakura.config;

import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades `sakura.*` de application.properties. */
@ConfigurationProperties("sakura")
public record SakuraProperties(ZoneId zonaHoraria, Reservas reservas, Cors cors, Jwt jwt) {

	/**
	 * @param minutosAntes   desde cuánto antes de la hora reservada la mesa aparece como reservada
	 * @param minutosDespues cuánto se guarda la mesa si el cliente no llega
	 * @param duracionMinutos cuánto dura una comida: dos reservas de la misma mesa deben separarse al menos esto
	 */
	public record Reservas(int maxPersonas, int diasAntelacion, int minutosAntes, int minutosDespues,
			int duracionMinutos) {
	}

	public record Cors(List<String> origenes) {
	}

	/**
	 * @param secreto clave para firmar los tokens (HS256): al menos 32 caracteres, fuera del repositorio en producción
	 * @param horas   cuánto dura la sesión: un turno de trabajo
	 */
	public record Jwt(String secreto, int horas) {
	}
}
