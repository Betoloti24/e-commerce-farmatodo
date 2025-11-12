package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad {@link Product}.
 * Proporciona métodos de búsqueda para encontrar productos por identificador y por palabra clave.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Busca un producto basado en su número de parte (partNumber).
     *
     * @param partNumber El número de parte único del producto.
     * @return Un {@link Optional} que contiene el {@link Product} si se encuentra.
     */
    Optional<Product> findByPartNumber(String partNumber);

    /**
     * Realiza una búsqueda de productos por palabra clave, comparando el término
     * tanto en el número de parte como en el nombre del producto (case-insensitive).
     *
     * @param keyword La palabra clave de búsqueda.
     * @return Una lista de {@link Product} que coinciden con la palabra clave.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByKeyword(@Param("keyword") String keyword);
}