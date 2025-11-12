package com.farmatodo.apigetway.repository;

import com.farmatodo.apigetway.model.OrderDetail;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio para la entidad {@link OrderDetail}.
 * Actualmente solo hereda las operaciones CRUD básicas de JPA,
 * ya que la gestión principal de detalles se realiza a través de la entidad {@link Order}.
 */
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {

}