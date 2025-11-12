package com.farmatodo.apigetway.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidad que representa un producto disponible en el inventario.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Identificador único (UUID) del producto.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Número de parte o SKU único del producto.
     */
    @Column(name = "part_number", length = 50, nullable = false, unique = true)
    private String partNumber;

    /**
     * Nombre descriptivo del producto.
     */
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /**
     * Categoría a la que pertenece el producto.
     */
    @Column(name = "category", length = 50, nullable = false)
    private String category;

    /**
     * Cantidad actual en inventario.
     */
    @Column(name = "stock", nullable = false)
    private Integer stock;

    /**
     * Fecha y hora de creación del registro del producto.
     */
    @Column(name = "creation_date", nullable = false)
    private ZonedDateTime creationDate = ZonedDateTime.now();

    /**
     * Fecha y hora de la última actualización del registro del producto.
     */
    @Column(name = "update_date")
    private ZonedDateTime updateDate;
}