package com.sakura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sakura.model.Faq;

public interface FaqRepository extends JpaRepository<Faq, Long> {

	List<Faq> findAllByOrderByOrden();
}
