package com.sakura.config;

import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Propiedades `sakura.*` de application.properties. */
@ConfigurationProperties("sakura")
public record SakuraProperties(ZoneId zonaHoraria, Reservas reservas, Cors cors) {

	public record Reservas(int maxPersonas, int diasAntelacion) {
	}

	public record Cors(List<String> origenes) {
	}
}
