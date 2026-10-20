package com.sakura.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.CategoriaRequest;
import com.sakura.exception.ApiException;
import com.sakura.model.Categoria;
import com.sakura.repository.CategoriaRepository;
import com.sakura.repository.ProductoRepository;

/** CRUD de categorías del menú (RF-02, HU-02). */
@Service
public class CategoriaService {

	private final CategoriaRepository categorias;
	private final ProductoRepository productos;

	public CategoriaService(CategoriaRepository categorias, ProductoRepository productos) {
		this.categorias = categorias;
		this.productos = productos;
	}

	@Transactional(readOnly = true)
	public List<Categoria> listar() {
		return categorias.findAllByOrderByOrdenAscNombreAsc();
	}

	@Transactional(readOnly = true)
	public Categoria obtener(Long id) {
		return categorias.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe esa categoría"));
	}

	@Transactional
	public Categoria crear(CategoriaRequest req) {
		String nombre = req.nombre().trim();
		if (categorias.existsByNombreIgnoreCase(nombre)) {
			throw new ApiException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
		}
		return categorias.save(new Categoria(nombre, vacioANull(req.descripcion()), req.ordenOCero()));
	}

	@Transactional
	public Categoria actualizar(Long id, CategoriaRequest req) {
		var categoria = obtener(id);
		String nombre = req.nombre().trim();
		if (categorias.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
			throw new ApiException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
		}
		categoria.setNombre(nombre);
		categoria.setDescripcion(vacioANull(req.descripcion()));
		categoria.setOrden(req.ordenOCero());
		return categoria;
	}

	/** Una categoría con productos no se puede borrar: primero hay que moverlos o eliminarlos. */
	@Transactional
	public void eliminar(Long id) {
		var categoria = obtener(id);
		if (productos.existsByCategoriaId(id)) {
			throw new ApiException(HttpStatus.CONFLICT,
					"La categoría tiene productos. Muévelos a otra categoría o elimínalos antes");
		}
		categorias.delete(categoria);
	}

	private static String vacioANull(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}
}
