package com.sakura.dto;

import com.sakura.model.Faq;

/** Una pregunta frecuente de la landing. */
public record FaqResponse(Long id, String pregunta, String respuesta) {

	public static FaqResponse de(Faq f) {
		return new FaqResponse(f.getId(), f.getPregunta(), f.getRespuesta());
	}
}
