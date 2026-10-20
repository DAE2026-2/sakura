package com.sakura.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Faq {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String pregunta;

	@Column(nullable = false, length = 1000)
	private String respuesta;

	@Column(nullable = false)
	private int orden;

	public Long getId() { return id; }
	public String getPregunta() { return pregunta; }
	public String getRespuesta() { return respuesta; }
	public int getOrden() { return orden; }
}
