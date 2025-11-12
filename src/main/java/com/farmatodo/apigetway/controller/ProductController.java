package com.farmatodo.apigetway.controller;

import com.farmatodo.apigetway.model.Product;
import com.farmatodo.apigetway.model.dto.ApiResponse;
import com.farmatodo.apigetway.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de productos y la funcionalidad de búsqueda.
 * <p>
 * Nota: El endpoint de creación (POST) típicamente estaría protegido con roles
 * administrativos, mientras que la búsqueda (GET /search) es accesible a clientes.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Crea un nuevo producto en el inventario.
     *
     * @param product La entidad {@link Product} con los datos a crear.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<Product>).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        try {
            Product newProduct = productService.createProduct(product);

            // Retorno exitoso 201 CREATED
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            HttpStatus.CREATED,
                            "Producto creado exitosamente. ID: " + newProduct.getId(),
                            newProduct
                    ));
        } catch (IllegalArgumentException e) {
            // Error de negocio (ej. número de parte duplicado)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "Error al crear producto: " + e.getMessage()
                    ));
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar la creación del producto."
                    ));
        }
    }

    /**
     * Busca productos por palabra clave en el nombre o número de parte,
     * aplicando filtros de visibilidad basados en el stock mínimo.
     *
     * @param keyword La palabra clave de búsqueda.
     * @return ResponseEntity con la estructura de respuesta estandarizada (ApiResponse<List<Product>>).
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(@RequestParam String keyword) {

        // Validar la entrada de la palabra clave
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            HttpStatus.BAD_REQUEST,
                            "La palabra clave de búsqueda es obligatoria."
                    ));
        }

        try {
            List<Product> products = productService.searchProductsByKeyword(keyword);

            if (products.isEmpty()) {
                // Si la búsqueda no encuentra resultados, retornamos 200 OK con mensaje informativo.
                return ResponseEntity.ok(
                        ApiResponse.success(
                                HttpStatus.OK,
                                "Búsqueda exitosa. No se encontraron productos que coincidan.",
                                products // Lista vacía
                        )
                );
            }

            // Retorno exitoso 200 OK
            return ResponseEntity.ok(
                    ApiResponse.success(
                            HttpStatus.OK,
                            "Búsqueda exitosa. Se encontraron " + products.size() + " productos.",
                            products
                    )
            );
        } catch (Exception e) {
            // Error interno
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar la búsqueda de productos."
                    ));
        }
    }
}