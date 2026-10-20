package com.sakura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sakura.model.Experiencia;

public interface ExperienciaRepository extends JpaRepository<Experiencia, Long> {

	List<Experiencia> findAllByOrderByOrden();
}
