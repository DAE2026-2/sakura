package com.sakura.config;

import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades `sakura.*` de application.properties. */
@ConfigurationProperties("sakura")
public record SakuraProperties(ZoneId zonaHoraria, Reservas reservas, Cors cors) {

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
}
