package com.sakura.service;

/** Limpieza de texto de los formularios, compartida por los servicios. */
final class Textos {

	private Textos() {
	}

	/** Quita espacios y convierte un campo opcional vacío en null, para no guardar "" en la base. */
	static String vacioANull(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}
}
