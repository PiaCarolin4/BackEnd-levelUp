package com.app.order.service;

import com.app.order.config.security.TokenContext;
import com.app.order.dto.*;
import com.app.order.model.*;
import com.app.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    @Value("${carrito.url.get-id}")
    private String GET_CARRITO_ID_URL;

    @Value("${carrito.url.vaciar}")
    private String VACIAR_CARRITO_URL;



    private final MicroServiceClient microServiceClient;
    private final OrderRepository orderRepository;

    // RF-OR-01 – Creación de orden desde el carrito activo del usuario
    @Transactional
    @CircuitBreaker(name = "carritoService", fallbackMethod = "fallbackCrearOrden")
    public OrderResponseDTO crearOrdenDesdeCarritoActivo(OrderRequestDTO orderRequest, Long usuarioId) {
        log.info("Creando orden desde carrito activo para usuario ID: {}", usuarioId);

        // Obtener carrito activo del usuario
        CarritoDto carrito = obtenerCarritoActivo(usuarioId);

        // Validar que el carrito tenga items
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        // Crear la orden
        Order order = construirOrdenDesdeCarrito(carrito, orderRequest);

        // Guardar la orden
        Order ordenGuardada = orderRepository.save(order);

        // Vaciar carrito después de crear la orden
        vaciarCarrito(carrito.getId(), usuarioId);

        log.info("Orden creada exitosamente con ID: {} para usuario ID: {}", ordenGuardada.getId(), usuarioId);

        return convertirADTO(ordenGuardada);
    }

    // RF-OR-02 – Actualizar estado de la orden
    @Transactional
    public OrderResponseDTO actualizarEstadoOrden(Long orderId, OrderStatus nuevoEstado, Long usuarioId) {
        log.info("Actualizando estado de orden ID: {} a {} para usuario ID: {}", orderId, nuevoEstado, usuarioId);

        Order order = orderRepository.findByIdAndUsuarioId(orderId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId + " para el usuario"));

        // Validar transición de estado
        validarTransicionEstado(order.getEstado(), nuevoEstado);

        order.setEstado(nuevoEstado);
        order.setFechaActualizacion(LocalDateTime.now());

        Order ordenActualizada = orderRepository.save(order);

        log.info("Estado de orden ID: {} actualizado a {} para usuario ID: {}", orderId, nuevoEstado, usuarioId);

        return convertirADTO(ordenActualizada);
    }

    // RF-OR-03 – Obtener orden por ID (confirmación)
    public OrderResponseDTO obtenerOrdenPorId(Long orderId, Long usuarioId) {
        log.info("Obteniendo orden con ID: {} para usuario ID: {}", orderId, usuarioId);

        Order order = orderRepository.findByIdAndUsuarioId(orderId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId + " para el usuario"));

        return convertirADTO(order);
    }

    // RF-OR-04 – Listado de órdenes por usuario
    public List<OrderResponseDTO> obtenerOrdenesPorUsuario(Long usuarioId) {
        log.info("Obteniendo órdenes para usuario ID: {}", usuarioId);

        List<Order> orders = orderRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);

        return orders.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // RF-OR-05 – Detalle de orden específica
    public OrderResponseDTO obtenerDetalleOrden(Long orderId, Long usuarioId) {
        // Reutiliza el método obtenerOrdenPorId ya que incluye todos los detalles
        return obtenerOrdenPorId(orderId, usuarioId);
    }

    // RF-OR-06 – Cancelación de orden
    @Transactional
    public OrderResponseDTO cancelarOrden(Long orderId, Long usuarioId) {
        log.info("Solicitando cancelación de orden ID: {} para usuario ID: {}", orderId, usuarioId);

        Order order = orderRepository.findByIdAndUsuarioId(orderId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + orderId + " para el usuario"));

        // Validar que la orden puede ser cancelada según reglas de negocio
        validarCancelacionOrden(order);

        order.setEstado(OrderStatus.CANCELADO);
        order.setFechaActualizacion(LocalDateTime.now());

        Order ordenCancelada = orderRepository.save(order);

        log.info("Orden ID: {} cancelada exitosamente para usuario ID: {}", orderId, usuarioId);

        return convertirADTO(ordenCancelada);
    }

    // Métodos auxiliares privados
    private CarritoDto obtenerCarritoActivo(Long usuarioId) {
        String token = TokenContext.getToken();
        String url = GET_CARRITO_ID_URL.replace("{id}", usuarioId.toString());;


        ResponseEntity<CarritoDto> response = microServiceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                CarritoDto.class,
                token
        );
        return response.getBody();
    }

    private CarritoDto consultarCarrito(Long carritoId, Long usuarioId) {
        String token = TokenContext.getToken();
        String url = GET_CARRITO_ID_URL.replace("{id}", carritoId.toString());

        ResponseEntity<CarritoDto> response = microServiceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                CarritoDto.class,
                token
        );
        return response.getBody();
    }

    private void vaciarCarrito(Long carritoId, Long usuarioId) {
        try {
            String token = TokenContext.getToken();
            String url = VACIAR_CARRITO_URL.replace("{id}", carritoId.toString());

            microServiceClient.enviarConToken(
                    url,
                    HttpMethod.DELETE,
                    null,
                    Void.class,
                    token
            );
            log.info("Carrito ID: {} vaciado exitosamente para usuario ID: {}", carritoId, usuarioId);
        } catch (Exception e) {
            log.error("Error al vaciar carrito ID: {} para usuario ID: {}", carritoId, usuarioId, e);
            // No lanzamos excepción para no afectar la creación de la orden
        }
    }

    private Order construirOrdenDesdeCarrito(CarritoDto carrito, OrderRequestDTO orderRequest) {
        // Convertir items del carrito a items de orden
        List<OrderItem> orderItems = carrito.getItems().stream()
                .map(this::convertirItemCarritoAOrderItem)
                .collect(Collectors.toList());

        return Order.builder()
                .usuarioId(carrito.getUsuarioId())
                .numeroOrden(generarNumeroOrden())
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .estado(OrderStatus.PENDIENTE_PAGO) // Estado inicial
                .subtotal(carrito.getSubtotal())
                .totalDescuentos(carrito.getTotalDescuentos())
                .total(carrito.getTotal())
                .direccionDespacho(orderRequest.getDireccionDespacho())
                .metodoPago(orderRequest.getMetodoPago())
                .items(orderItems)
                .build();
    }

    private OrderItem convertirItemCarritoAOrderItem(CarritoDto.ItemCarrito itemCarrito) {
        return OrderItem.builder()
                .productoId(itemCarrito.getProductoId())
                .nombreProducto(itemCarrito.getNombreProducto())
                .descripcionProducto(itemCarrito.getDescripcionProducto())
                .imagenProducto(itemCarrito.getImagenProducto())
                .cantidad(itemCarrito.getCantidad())
                .precioUnitario(itemCarrito.getPrecioUnitario())
                .descuentoProducto(itemCarrito.getDescuentoProducto())
                .descuentoAplicado(itemCarrito.getDescuentoAplicado())
                .precioFinalUnitario(itemCarrito.getPrecioFinalUnitario())
                .descuentoTotal(itemCarrito.getDescuentoTotal())
                .subtotal(itemCarrito.getSubtotal())
                .precioTotal(itemCarrito.getPrecioTotal())
                .build();
    }

    private OrderResponseDTO convertirADTO(Order order) {
        List<OrderItemDTO> itemsDTO = order.getItems().stream()
                .map(this::convertirItemADTO)
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .numeroOrden(order.getNumeroOrden())
                .usuarioId(order.getUsuarioId())
                .fechaCreacion(order.getFechaCreacion())
                .fechaActualizacion(order.getFechaActualizacion())
                .estado(order.getEstado())
                .subtotal(order.getSubtotal())
                .totalDescuentos(order.getTotalDescuentos())
                .total(order.getTotal())
                .direccionDespacho(convertirDireccionADTO(order.getDireccionDespacho()))
                .metodoPago(convertirMetodoPagoADTO(order.getMetodoPago()))
                .items(itemsDTO)
                .build();
    }

    private OrderItemDTO convertirItemADTO(OrderItem item) {
        return OrderItemDTO.builder()
                .productoId(item.getProductoId())
                .nombreProducto(item.getNombreProducto())
                .descripcionProducto(item.getDescripcionProducto())
                .imagenProducto(item.getImagenProducto())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .descuentoProducto(item.getDescuentoProducto())
                .descuentoAplicado(item.getDescuentoAplicado())
                .precioFinalUnitario(item.getPrecioFinalUnitario())
                .descuentoTotal(item.getDescuentoTotal())
                .subtotal(item.getSubtotal())
                .precioTotal(item.getPrecioTotal())
                .build();
    }

    private DireccionDespachoDTO convertirDireccionADTO(DireccionDespacho direccion) {
        if (direccion == null) return null;

        return DireccionDespachoDTO.builder()
                .calle(direccion.getCalle())
                .numero(direccion.getNumero())
                .departamento(direccion.getDepartamento())
                .comuna(direccion.getComuna())
                .ciudad(direccion.getCiudad())
                .region(direccion.getRegion())
                .codigoPostal(direccion.getCodigoPostal())
                .referencias(direccion.getReferencias())
                .build();
    }

    private MetodoPagoDTO convertirMetodoPagoADTO(MetodoPago metodoPago) {
        if (metodoPago == null) return null;

        return MetodoPagoDTO.builder()
                .tipo(metodoPago.getTipo())
                .ultimosDigitos(metodoPago.getUltimosDigitos())
                .nombreTitular(metodoPago.getNombreTitular())
                .banco(metodoPago.getBanco())
                .numeroTransaccion(metodoPago.getNumeroTransaccion())
                .build();
    }

    private String generarNumeroOrden() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() +
                "-" + System.currentTimeMillis();
    }

    private void validarTransicionEstado(OrderStatus estadoActual, OrderStatus nuevoEstado) {
        // Implementar lógica de validación de transiciones de estado
        // Por ejemplo, no se puede pasar de CANCELADO a otro estado
        if (estadoActual == OrderStatus.CANCELADO) {
            throw new IllegalStateException("No se puede modificar una orden cancelada");
        }

        if (estadoActual == OrderStatus.ENTREGADO) {
            throw new IllegalStateException("No se puede modificar una orden ya entregada");
        }
    }

    private void validarCancelacionOrden(Order order) {
        // RF-OR-06: Sólo se puede cancelar si está "pendiente de pago" o "en preparación"
        if (order.getEstado() != OrderStatus.PENDIENTE_PAGO &&
                order.getEstado() != OrderStatus.EN_PREPARACION) {
            throw new IllegalStateException(
                    "La orden sólo puede ser cancelada si está en estado 'PENDIENTE_PAGO' o 'EN_PREPARACION'. Estado actual: " + order.getEstado()
            );
        }
    }

    // Fallback methods para Circuit Breaker
    public OrderResponseDTO fallbackCrearOrden(OrderRequestDTO orderRequest, Long usuarioId, Throwable t) {
        log.error("Fallback method ejecutado para crear orden. Error: {}", t.getMessage());
        throw new RuntimeException("Servicio de carrito no disponible. No se pudo crear la orden.");
    }

    public CarritoDto fallbackProducto(Long usuarioId, Throwable t) {
        log.error("Fallback method ejecutado para consultar carrito activo del usuario ID: {}. Error: {}", usuarioId, t.getMessage());
        throw new RuntimeException("Servicio de carrito no disponible.");
    }
}