package com.sakura.dto;

import java.util.List;

/** Una categoría del menú público con sus productos disponibles. */
public record MenuCategoriaResponse(Long id, String nombre, String descripcion, List<ProductoResponse> productos) {
}
