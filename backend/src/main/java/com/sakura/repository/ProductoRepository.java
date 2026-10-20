package com.sakura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sakura.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

	/** Todos los productos con su categoría cargada, en el orden de la carta. */
	@EntityGraph(attributePaths = "categoria")
	List<Producto> findAllByOrderByOrdenAscNombreAsc();

	/** El menú que ven los clientes: solo lo disponible (RF-10, HU-06). */
	@EntityGraph(attributePaths = "categoria")
	List<Producto> findByDisponibleTrueOrderByOrdenAscNombreAsc();

	boolean existsByCategoriaId(Long categoriaId);
}
