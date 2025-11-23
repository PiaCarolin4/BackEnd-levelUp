package com.app.carrito.controller;

import com.app.carrito.dto.AgregarProductoRequest;
import com.app.carrito.dto.CarritoResponse;
import com.app.carrito.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<CarritoResponse> obtenerCarritoActivo(
            @RequestHeader("X-User-Id") Long usuarioId) {
        CarritoResponse carrito = carritoService.obtenerCarritoActivo(usuarioId);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarProductoAlCarrito(
            @RequestHeader("X-User-Id") Long usuarioId,
            @Valid @RequestBody AgregarProductoRequest request) {
        CarritoResponse carrito = carritoService.agregarProductoAlCarrito(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    @PutMapping("/items/{productoId}")
    public ResponseEntity<CarritoResponse> actualizarCantidadProducto(
            @RequestHeader("X-User-Id") Long usuarioId,
            @PathVariable Long productoId,
            @RequestParam Integer cantidad) {
        CarritoResponse carrito = carritoService.actualizarCantidadProducto(usuarioId, productoId, cantidad);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProductoDelCarrito(
            @RequestHeader("X-User-Id") Long usuarioId,
            @PathVariable Long productoId) {
        CarritoResponse carrito = carritoService.eliminarProductoDelCarrito(usuarioId, productoId);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito(
            @RequestHeader("X-User-Id") Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/completar-compra")
    public ResponseEntity<CarritoResponse> completarCompra(
            @RequestHeader("X-User-Id") Long usuarioId) {
        CarritoResponse carrito = carritoService.completarCompra(usuarioId);
        return ResponseEntity.ok(carrito);
    }
}