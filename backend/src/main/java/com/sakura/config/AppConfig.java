package com.sakura.config;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SakuraProperties.class)
public class AppConfig {

	/** Reloj en la zona del restaurante: "hoy" y "ahora" son los del local, no los del servidor. */
	@Bean
	Clock clock(SakuraProperties props) {
		return Clock.system(props.zonaHoraria());
	}
}
