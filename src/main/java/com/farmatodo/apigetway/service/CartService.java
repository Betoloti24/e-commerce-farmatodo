package com.farmatodo.apigetway.service;

import com.farmatodo.apigetway.model.CartItem;
import com.farmatodo.apigetway.model.Client;
import com.farmatodo.apigetway.model.Product;
import com.farmatodo.apigetway.model.dto.CartItemResponse;
import com.farmatodo.apigetway.repository.CartItemRepository;
import com.farmatodo.apigetway.repository.ClientRepository;
import com.farmatodo.apigetway.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio central para la gestión de la lógica de negocio del carrito de compras.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    /**
     * Busca una entidad {@link Client} por su ID o lanza una excepción.
     *
     * @param clientId ID del cliente.
     * @return La entidad {@link Client}.
     * @throws IllegalArgumentException Si el cliente no existe.
     */
    private Client findClientById(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + clientId + " no encontrado."));
    }

    /**
     * Busca una entidad {@link Product} por su ID o lanza una excepción.
     *
     * @param productId ID del producto.
     * @return La entidad {@link Product}.
     * @throws IllegalArgumentException Si el producto no existe.
     */
    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto con ID " + productId + " no encontrado."));
    }

    /**
     * Convierte una entidad {@link CartItem} a su DTO de respuesta.
     *
     * @param item La entidad de origen.
     * @return El DTO {@link CartItemResponse}.
     */
    private CartItemResponse convertToResponse(CartItem item) {
        return new CartItemResponse(item);
    }

    /**
     * Obtiene todos los ítems del carrito para un cliente específico.
     *
     * @param clientId ID del cliente.
     * @return Una lista de DTOs {@link CartItemResponse}.
     */
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItemsByClient(UUID clientId) {
        findClientById(clientId); // Asegura que el cliente exista

        List<CartItem> cartItems = cartItemRepository.findByClient_Id(clientId);

        return cartItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Añade un producto al carrito o actualiza la cantidad si ya existe.
     *
     * @param clientId ID del cliente propietario del carrito.
     * @param productId ID del producto.
     * @param quantity Cantidad a añadir. Si el ítem ya existe, se suma a la cantidad actual.
     * @return El DTO del ítem del carrito guardado/actualizado.
     */
    @Transactional
    public CartItemResponse addOrUpdateItemInCart(UUID clientId, UUID productId, int quantity) {
        CartItem savedItem = cartItemRepository.findByClient_IdAndProduct_Id(clientId, productId)
                .map(existingItem -> {
                    // Si ya existe, actualiza la cantidad
                    existingItem.setQuantity(existingItem.getQuantity() + quantity);
                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    // Si no existe, crea un nuevo ítem
                    Client client = findClientById(clientId);
                    Product product = findProductById(productId);

                    CartItem newItem = new CartItem();
                    newItem.setClient(client);
                    newItem.setProduct(product);
                    newItem.setQuantity(quantity);
                    return cartItemRepository.save(newItem);
                });

        return convertToResponse(savedItem);
    }
}