package com.sakura.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.MenuCategoriaResponse;
import com.sakura.dto.ProductoResponse;
import com.sakura.model.Experiencia;
import com.sakura.model.Faq;
import com.sakura.repository.CategoriaRepository;
import com.sakura.repository.ExperienciaRepository;
import com.sakura.repository.FaqRepository;
import com.sakura.repository.ProductoRepository;

/** Contenido público de la landing: menú, experiencias y preguntas frecuentes. */
@Service
@Transactional(readOnly = true)
public class ContenidoService {

	private final CategoriaRepository categorias;
	private final ProductoRepository productos;
	private final ExperienciaRepository experiencias;
	private final FaqRepository faqs;

	public ContenidoService(CategoriaRepository categorias, ProductoRepository productos,
			ExperienciaRepository experiencias, FaqRepository faqs) {
		this.categorias = categorias;
		this.productos = productos;
		this.experiencias = experiencias;
		this.faqs = faqs;
	}

	/** Menú agrupado por categoría, solo con productos disponibles. Las categorías vacías no aparecen. */
	public List<MenuCategoriaResponse> menu() {
		Map<Long, List<ProductoResponse>> porCategoria = productos.findByDisponibleTrueOrderByOrdenAscNombreAsc()
				.stream()
				.map(ProductoResponse::de)
				.collect(Collectors.groupingBy(ProductoResponse::categoriaId));
		return categorias.findAllByOrderByOrdenAscNombreAsc().stream()
				.filter(c -> porCategoria.containsKey(c.getId()))
				.map(c -> new MenuCategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), porCategoria.get(c.getId())))
				.toList();
	}

	public List<Experiencia> experiencias() {
		return experiencias.findAllByOrderByOrden();
	}

	public List<Faq> faqs() {
		return faqs.findAllByOrderByOrden();
	}
}
