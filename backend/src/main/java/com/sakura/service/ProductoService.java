package com.sakura.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakura.dto.ProductoRequest;
import com.sakura.exception.ApiException;
import com.sakura.model.Producto;
import com.sakura.repository.CategoriaRepository;
import com.sakura.repository.ProductoRepository;

/** CRUD de productos del menú (RF-01, HU-01). */
@Service
public class ProductoService {

	private final ProductoRepository productos;
	private final CategoriaRepository categorias;

	public ProductoService(ProductoRepository productos, CategoriaRepository categorias) {
		this.productos = productos;
		this.categorias = categorias;
	}

	@Transactional(readOnly = true)
	public List<Producto> listar() {
		return productos.findAllByOrderByOrdenAscNombreAsc();
	}

	@Transactional(readOnly = true)
	public Producto obtener(Long id) {
		var producto = productos.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe ese producto"));
		producto.getCategoria().getNombre(); // carga la categoría antes de cerrar la transacción
		return producto;
	}

	@Transactional
	public Producto crear(ProductoRequest req) {
		var categoria = categorias.findById(req.categoriaId())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Esa categoría no existe"));
		return productos.save(new Producto(categoria, req.nombre().trim(), vacioANull(req.descripcion()), req.precio(),
				req.esDisponible(), req.esDestacado(), req.ordenOCero()));
	}

	@Transactional
	public Producto actualizar(Long id, ProductoRequest req) {
		var producto = obtener(id);
		var categoria = categorias.findById(req.categoriaId())
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Esa categoría no existe"));
		producto.setCategoria(categoria);
		producto.setNombre(req.nombre().trim());
		producto.setDescripcion(vacioANull(req.descripcion()));
		producto.setPrecio(req.precio());
		producto.setDisponible(req.esDisponible());
		producto.setDestacado(req.esDestacado());
		producto.setOrden(req.ordenOCero());
		return producto;
	}

	/** Un producto que ya está en algún pedido no se borra (perdería el historial): se marca como no disponible. */
	@Transactional
	public void eliminar(Long id) {
		var producto = obtener(id);
		try {
			productos.delete(producto);
			productos.flush();
		} catch (DataIntegrityViolationException e) {
			throw new ApiException(HttpStatus.CONFLICT,
					"El producto aparece en pedidos. Márcalo como no disponible en lugar de eliminarlo");
		}
	}

	private static String vacioANull(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}
}
