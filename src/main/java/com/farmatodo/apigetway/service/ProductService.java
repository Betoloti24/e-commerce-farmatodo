package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.config.InitialDataLoader;
import com.farmatodo.apigetway.model.Product;
import com.farmatodo.apigetway.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la lógica de negocio relacionada con los productos (inventario, búsqueda).
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PreferenceService preferenceService;
    private static final String PRODUCT_MIN_STOCK_KEY = InitialDataLoader.PRODUCT_MIN_STOCK;

    /**
     * Busca un producto por su ID.
     *
     * @param productId ID del producto.
     * @return El producto encontrado.
     * @throws IllegalArgumentException Si el producto no es encontrado.
     */
    @Transactional(readOnly = true)
    public Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto con ID " + productId + " no encontrado."));
    }

    /**
     * Realiza una búsqueda de productos por palabra clave, aplicando un filtro
     * de visibilidad basado en el stock mínimo configurado en las preferencias del sistema.
     *
     * @param keyword Palabra clave de búsqueda.
     * @return Lista de productos que coinciden y cumplen con el stock mínimo.
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByKeyword(String keyword) {
        Integer minStock = preferenceService.getPreferenceValueAsInteger(PRODUCT_MIN_STOCK_KEY);
        List<Product> matchedProducts = productRepository.findByKeyword(keyword);

        // Filtrar productos cuya cantidad de stock sea igual o mayor al mínimo configurado
        return matchedProducts.stream()
                .filter(p -> p.getStock() >= minStock)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo producto.
     *
     * @param product La entidad {@link Product} a guardar.
     * @return El producto guardado.
     * @throws IllegalArgumentException Si el número de parte ya existe.
     */
    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.findByPartNumber(product.getPartNumber()).isPresent()) {
            throw new IllegalArgumentException("El número de parte " + product.getPartNumber() + " ya existe.");
        }
        product.setCreationDate(ZonedDateTime.now());
        return productRepository.save(product);
    }
}