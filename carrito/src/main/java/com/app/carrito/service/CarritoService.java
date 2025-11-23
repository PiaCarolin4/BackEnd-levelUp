package com.app.carrito.service;

import com.app.carrito.config.security.TokenContext;
import com.app.carrito.dto.*;
import com.app.carrito.model.Carrito;
import com.app.carrito.model.CarritoItem;
import com.app.carrito.repository.CarritoRepository;
import com.app.carrito.repository.CarritoItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoService {

    @Value("${producto.url.get-id}")
    private String GET_PRODUCTO_ID_URL;

    private final MicroServiceClient microServiceClient;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;

    @CircuitBreaker(name = "productosService", fallbackMethod = "fallbackProducto")
    public ProductoResponse consultarProducto(Long productoId) {
        String token = TokenContext.getToken();
        String url = GET_PRODUCTO_ID_URL + productoId;
        ResponseEntity<ProductoResponse> response = microServiceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                ProductoResponse.class,
                token
        );
        return response.getBody();
    }

    // Fallback para cuando el servicio de productos no está disponible
    public ProductoResponse fallbackProducto(Long productoId, Exception e) {
        log.error("Fallback method called for productoId: {}, Error: {}", productoId, e.getMessage());
        // Retornar un producto por defecto o null según tu lógica de negocio
        return null;
    }

    @Transactional
    public CarritoResponse obtenerCarritoActivo(Long usuarioId) {
        Optional<Carrito> carritoOpt = obtenerCarritoActivoConItems(usuarioId);

        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            carrito.calcularTotales();
            carritoRepository.save(carrito); // Para actualizar los totales
            return construirCarritoResponse(carrito);
        } else {
            // Crear nuevo carrito si no existe uno activo
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setUsuarioId(usuarioId);
            nuevoCarrito.setSubtotal(BigDecimal.ZERO);
            nuevoCarrito.setTotalDescuentos(BigDecimal.ZERO);
            nuevoCarrito.setTotal(BigDecimal.ZERO);
            nuevoCarrito.setItems(new ArrayList<>()); // Inicializar lista vacía

            Carrito carritoGuardado = carritoRepository.save(nuevoCarrito);
            return construirCarritoResponse(carritoGuardado);
        }
    }
    private Optional<Carrito> obtenerCarritoActivoConItems(Long usuarioId) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioIdAndEstado(usuarioId, Carrito.EstadoCarrito.ACTIVO);

        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            // Forzar la carga de los items
            if (carrito.getItems() != null) {
                carrito.getItems().size(); // Esto fuerza la carga en LAZY
            } else {
                carrito.setItems(new ArrayList<>());
            }
        }

        return carritoOpt;
    }
    @Transactional
    public CarritoResponse agregarProductoAlCarrito(Long usuarioId, AgregarProductoRequest request) {
        // Obtener o crear carrito activo
        Carrito carrito = obtenerCarritoActivoInterno(usuarioId)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuarioId(usuarioId);
                    nuevoCarrito.setSubtotal(BigDecimal.ZERO);
                    nuevoCarrito.setTotalDescuentos(BigDecimal.ZERO);
                    nuevoCarrito.setTotal(BigDecimal.ZERO);
                    return carritoRepository.save(nuevoCarrito);
                });

        // Consultar información del producto
        ProductoResponse producto = consultarProducto(request.getProductoId());
        if (producto == null || !producto.getActivo()) {
            throw new RuntimeException("Producto no disponible");
        }

        // Verificar si el producto ya está en el carrito
        Optional<CarritoItem> itemExistenteOpt = carritoItemRepository.findByCarritoIdAndProductoId(carrito.getId(), request.getProductoId());

        if (itemExistenteOpt.isPresent()) {
            // Actualizar cantidad si ya existe
            CarritoItem itemExistente = itemExistenteOpt.get();
            itemExistente.setCantidad(itemExistente.getCantidad() + request.getCantidad());
            itemExistente.calcularPrecioTotal();
            carritoItemRepository.save(itemExistente);
        } else {
            // Crear nuevo item
            CarritoItem nuevoItem = new CarritoItem();
            nuevoItem.setCarritoId(carrito.getId());
            nuevoItem.setProductoId(request.getProductoId());
            nuevoItem.setCantidad(request.getCantidad());

            // Usar precio del producto o el proporcionado en la request
            BigDecimal precioUnitario = request.getPrecioUnitario() != null ?
                    request.getPrecioUnitario() :
                    BigDecimal.valueOf(producto.getPrecioProducto());

            nuevoItem.setPrecioUnitario(precioUnitario);

            // Calcular descuento
            BigDecimal descuento = calcularDescuento(producto, request.getDescuentoAplicado());
            nuevoItem.setDescuentoAplicado(descuento);

            nuevoItem.calcularPrecioTotal();

            // Guardar el item
            CarritoItem itemGuardado = carritoItemRepository.save(nuevoItem);

            // Asegurar que la lista de items se inicialice
            if (carrito.getItems() == null) {
                carrito.setItems(new ArrayList<>());
            }
            carrito.getItems().add(itemGuardado);
        }

        // Recalcular totales del carrito
        carrito.calcularTotales();
        carrito.setFechaActualizacion(LocalDateTime.now());
        Carrito carritoActualizado = carritoRepository.save(carrito);

        return construirCarritoResponse(carritoActualizado);
    }

    @Transactional
    public CarritoResponse actualizarCantidadProducto(Long usuarioId, Long productoId, Integer nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Carrito carrito = obtenerCarritoActivoInterno(usuarioId)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito activo"));

        CarritoItem item = carritoItemRepository.findByCarritoIdAndProductoId(carrito.getId(), productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        item.setCantidad(nuevaCantidad);
        item.calcularPrecioTotal();
        carritoItemRepository.save(item);

        carrito.calcularTotales();
        carrito.setFechaActualizacion(LocalDateTime.now());
        Carrito carritoActualizado = carritoRepository.save(carrito);

        return construirCarritoResponse(carritoActualizado);
    }

    @Transactional
    public CarritoResponse eliminarProductoDelCarrito(Long usuarioId, Long productoId) {
        Carrito carrito = obtenerCarritoActivoInterno(usuarioId)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito activo"));

        CarritoItem item = carritoItemRepository.findByCarritoIdAndProductoId(carrito.getId(), productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        carritoItemRepository.delete(item);

        carrito.calcularTotales();
        carrito.setFechaActualizacion(LocalDateTime.now());
        Carrito carritoActualizado = carritoRepository.save(carrito);

        return construirCarritoResponse(carritoActualizado);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = obtenerCarritoActivoInterno(usuarioId)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito activo"));

        carritoItemRepository.deleteByCarritoId(carrito.getId());

        carrito.setSubtotal(BigDecimal.ZERO);
        carrito.setTotalDescuentos(BigDecimal.ZERO);
        carrito.setTotal(BigDecimal.ZERO);
        carrito.setFechaActualizacion(LocalDateTime.now());

        carritoRepository.save(carrito);
    }

    @Transactional
    public CarritoResponse completarCompra(Long usuarioId) {
        Carrito carrito = obtenerCarritoActivoInterno(usuarioId)
                .orElseThrow(() -> new RuntimeException("No se encontró carrito activo"));

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        carrito.setEstado(Carrito.EstadoCarrito.COMPLETADO);
        carrito.setFechaActualizacion(LocalDateTime.now());

        Carrito carritoCompletado = carritoRepository.save(carrito);

        return construirCarritoResponse(carritoCompletado);
    }

    private BigDecimal calcularDescuento(ProductoResponse producto, BigDecimal descuentoRequest) {
        if (descuentoRequest != null) {
            return descuentoRequest;
        }

        if (producto.getDescuentoProducto() != null && producto.getDescuentoProducto() > 0) {
            BigDecimal precioOriginal = BigDecimal.valueOf(producto.getPrecioProducto());
            BigDecimal porcentajeDescuento = BigDecimal.valueOf(producto.getDescuentoProducto()).divide(BigDecimal.valueOf(100));
            return precioOriginal.multiply(porcentajeDescuento);
        }

        return BigDecimal.ZERO;
    }

    private CarritoResponse construirCarritoResponse(Carrito carrito) {
        CarritoResponse response = new CarritoResponse(carrito);

        // Obtener items con información completa de productos
        List<CarritoItemResponse> itemsResponse = carrito.getItems().stream()
                .map(this::construirCarritoItemResponse)
                .collect(Collectors.toList());

        response.setItems(itemsResponse);
        return response;
    }

    private CarritoItemResponse construirCarritoItemResponse(CarritoItem item) {
        // Consultar información actualizada del producto
        ProductoResponse producto = consultarProducto(item.getProductoId());

        CarritoItemResponse response = new CarritoItemResponse();
        response.setId(item.getId());
        response.setProductoId(item.getProductoId());
        response.setCantidad(item.getCantidad());
        response.setPrecioUnitario(item.getPrecioUnitario());
        response.setDescuentoAplicado(item.getDescuentoAplicado());
        response.setPrecioTotal(item.getPrecioTotal());

        if (producto != null) {
            response.setNombreProducto(producto.getNombreProducto());
            response.setImagenProducto(producto.getImagenProducto());
            response.setDescripcionProducto(producto.getDescripcionProducto());
            response.setDescuentoProducto(BigDecimal.valueOf(producto.getDescuentoProducto()));
        }

        return response;
    }

    // Método auxiliar para obtener carrito activo (para uso interno)
    private Optional<Carrito> obtenerCarritoActivoInterno(Long usuarioId) {
        return carritoRepository.findByUsuarioIdAndEstado(usuarioId, Carrito.EstadoCarrito.ACTIVO);
    }
}