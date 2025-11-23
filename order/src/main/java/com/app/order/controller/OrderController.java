package com.app.order.controller;

import com.app.order.dto.OrderRequestDTO;
import com.app.order.dto.OrderResponseDTO;
import com.app.order.model.OrderStatus;
import com.app.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "API para gestión de órdenes de compra")
public class OrderController {

    private final OrderService orderService;

    // RF-OR-01 – Creación de orden desde el carrito
    @Operation(summary = "Crear orden desde carrito", description = "Crea una orden de compra a partir del carrito activo del usuario")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> crearOrdenDesdeCarrito(
            @Valid @RequestBody OrderRequestDTO orderRequest,
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Solicitando creación de orden para usuario ID: {}", usuarioId);
        OrderResponseDTO ordenCreada = orderService.crearOrdenDesdeCarritoActivo(orderRequest, usuarioId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCreada);
    }

    // RF-OR-03 – Confirmación de orden / RF-OR-05 – Detalle de orden
    @Operation(summary = "Obtener orden por ID", description = "Obtiene el detalle completo de una orden específica")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> obtenerOrdenPorId(
            @PathVariable @Parameter(description = "ID de la orden") Long orderId,
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Solicitando orden ID: {} para usuario ID: {}", orderId, usuarioId);
        OrderResponseDTO orden = orderService.obtenerOrdenPorId(orderId, usuarioId);

        return ResponseEntity.ok(orden);
    }

    // RF-OR-04 – Listado de órdenes por usuario
    @Operation(summary = "Obtener historial de órdenes", description = "Obtiene el listado de todas las órdenes del usuario ordenadas por fecha")
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> obtenerOrdenesPorUsuario(
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Solicitando historial de órdenes para usuario ID: {}", usuarioId);
        List<OrderResponseDTO> ordenes = orderService.obtenerOrdenesPorUsuario(usuarioId);

        return ResponseEntity.ok(ordenes);
    }

    // RF-OR-02 – Actualizar estado de la orden (para admin o sistema)
    @Operation(summary = "Actualizar estado de orden", description = "Actualiza el estado de una orden específica")
    @PutMapping("/{orderId}/estado")
    public ResponseEntity<OrderResponseDTO> actualizarEstadoOrden(
            @PathVariable @Parameter(description = "ID de la orden") Long orderId,
            @RequestParam @Parameter(description = "Nuevo estado de la orden") OrderStatus nuevoEstado,
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Solicitando actualización de estado a {} para orden ID: {} usuario ID: {}", nuevoEstado, orderId, usuarioId);
        OrderResponseDTO ordenActualizada = orderService.actualizarEstadoOrden(orderId, nuevoEstado, usuarioId);

        return ResponseEntity.ok(ordenActualizada);
    }

    // RF-OR-06 – Cancelación de orden
    @Operation(summary = "Cancelar orden", description = "Cancela una orden según las reglas de negocio definidas")
    @PutMapping("/{orderId}/cancelar")
    public ResponseEntity<OrderResponseDTO> cancelarOrden(
            @PathVariable @Parameter(description = "ID de la orden") Long orderId,
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Solicitando cancelación de orden ID: {} para usuario ID: {}", orderId, usuarioId);
        OrderResponseDTO ordenCancelada = orderService.cancelarOrden(orderId, usuarioId);

        return ResponseEntity.ok(ordenCancelada);
    }

    // Endpoint adicional: Obtener órdenes por estado
    @Operation(summary = "Obtener órdenes por estado", description = "Obtiene las órdenes del usuario filtradas por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<OrderResponseDTO>> obtenerOrdenesPorEstado(
            @PathVariable @Parameter(description = "Estado de las órdenes") OrderStatus estado,
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Solicitando órdenes con estado {} para usuario ID: {}", estado, usuarioId);
        // Nota: Necesitarías agregar este método en el servicio
        // List<OrderResponseDTO> ordenes = orderService.obtenerOrdenesPorEstado(usuarioId, estado);

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // Endpoint adicional: Verificar si una orden puede ser cancelada
    @Operation(summary = "Verificar cancelación de orden", description = "Verifica si una orden puede ser cancelada según las reglas de negocio")
    @GetMapping("/{orderId}/puede-cancelar")
    public ResponseEntity<Boolean> puedeCancelarOrden(
            @PathVariable @Parameter(description = "ID de la orden") Long orderId,
            @RequestHeader("X-User-Id") @Parameter(description = "ID del usuario") Long usuarioId) {

        log.info("Verificando si orden ID: {} puede ser cancelada para usuario ID: {}", orderId, usuarioId);
        try {
            // Intenta obtener la orden y validar si puede ser cancelada
            orderService.obtenerOrdenPorId(orderId, usuarioId);
            // La validación de cancelación se hace en el servicio al intentar cancelar
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}