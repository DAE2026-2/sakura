package com.sakura.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sakura.model.Cierre;

public interface CierreRepository extends JpaRepository<Cierre, Long> {

	boolean existsByFecha(LocalDate fecha);
}
