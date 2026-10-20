package com.sakura.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Persona que entra al sistema. El rol decide a qué partes de la API puede acceder (ver SecurityConfig). */
@Entity
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String nombre;

	@Column(nullable = false, unique = true, length = 120)
	private String email;

	/** Contraseña cifrada con BCrypt, con el prefijo {bcrypt} del DelegatingPasswordEncoder. Nunca sale en la API. */
	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Rol rol = Rol.CLIENTE;

	@Column(nullable = false)
	private boolean activo = true;

	@Column(name = "creado_en", nullable = false)
	private OffsetDateTime creadoEn;

	protected Usuario() {
	}

	public Usuario(String nombre, String email, String passwordHash, Rol rol, OffsetDateTime creadoEn) {
		this.nombre = nombre;
		this.email = email;
		this.passwordHash = passwordHash;
		this.rol = rol;
		this.creadoEn = creadoEn;
	}

	public Long getId() { return id; }
	public String getNombre() { return nombre; }
	public String getEmail() { return email; }
	public String getPasswordHash() { return passwordHash; }
	public Rol getRol() { return rol; }
	public boolean isActivo() { return activo; }
	public OffsetDateTime getCreadoEn() { return creadoEn; }

	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setEmail(String email) { this.email = email; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
	public void setRol(Rol rol) { this.rol = rol; }
	public void setActivo(boolean activo) { this.activo = activo; }
}
