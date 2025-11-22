package com.levelup.productos.controller;

import com.levelup.productos.dto.ProductosDTO;
import com.levelup.productos.mapper.ProductosMapper;
import com.levelup.productos.service.ProductosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Gestión de Productos", description = "API para la administración completa del catálogo de productos")
public class ProductosController {

    private final ProductosService productosService;
    private final ProductosMapper productosMapper;

    @Operation(summary = "Obtener todos los productos",
            description = "Retorna una lista completa de todos los productos disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductosDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/list")
    public ResponseEntity<List<ProductosDTO>> obtenerTodosLosProductos() {
        var productos = productosService.findAll();
        return ResponseEntity.ok(productosMapper.toDTOList(productos));
    }

    @Operation(summary = "Obtener productos activos",
            description = "Retorna una lista de productos que se encuentran en estado activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Productos activos obtenidos exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay productos activos disponibles")
    })
    @GetMapping("/estado/activos")
    public ResponseEntity<List<ProductosDTO>> obtenerProductosActivos() {
        var productos = productosService.findAllActivos();
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productosMapper.toDTOList(productos));
    }

    @Operation(summary = "Obtener producto por ID",
            description = "Busca y retorna un producto específico basado en su ID único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{productoId}")
    public ResponseEntity<ProductosDTO> obtenerProductoPorId(
            @Parameter(description = "ID único del producto", example = "1", required = true)
            @PathVariable Long productoId) {
        return productosService.findById(productoId)
                .map(productosMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear nuevo producto",
            description = "Registra un nuevo producto en el catálogo del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos del producto inválidos"),
            @ApiResponse(responseCode = "409", description = "Conflicto - El producto ya existe")
    })
    @PostMapping
    public ResponseEntity<ProductosDTO> crearProducto(
            @Parameter(description = "Datos del producto a crear", required = true)
            @Valid @RequestBody ProductosDTO productoDTO) {
        var producto = productosMapper.toEntity(productoDTO);
        var saved = productosService.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productosMapper.toDTO(saved));
    }

    @Operation(summary = "Actualizar producto completo",
            description = "Actualiza todos los campos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos")
    })
    @PutMapping("/{productoId}")
    public ResponseEntity<ProductosDTO> actualizarProductoCompleto(
            @Parameter(description = "ID único del producto a actualizar", example = "1", required = true)
            @PathVariable Long productoId,
            @Parameter(description = "Datos completos del producto actualizado", required = true)
            @Valid @RequestBody ProductosDTO productoDTO) {
        return productosService.findById(productoId)
                .map(producto -> {
                    productosMapper.updateEntityFromDTO(productoDTO, producto);
                    var updated = productosService.save(producto);
                    return ResponseEntity.ok(productosMapper.toDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar producto permanentemente",
            description = "Elimina un producto del sistema de manera permanente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID único del producto a eliminar", example = "1", required = true)
            @PathVariable Long productoId) {
        if (productosService.delete(productoId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Desactivar producto",
            description = "Cambia el estado de un producto a inactivo (eliminación lógica)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto desactivado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{productoId}/estado/desactivar")
    public ResponseEntity<ProductosDTO> desactivarProducto(
            @Parameter(description = "ID único del producto a desactivar", example = "1", required = true)
            @PathVariable Long productoId) {
        return productosService.deactivate(productoId)
                .map(productosMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar productos por nombre",
            description = "Busca productos cuyo nombre contenga el texto especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
            @ApiResponse(responseCode = "204", description = "No se encontraron productos con ese nombre")
    })
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<ProductosDTO>> buscarProductosPorNombre(
            @Parameter(description = "Texto a buscar en los nombres de productos", example = "laptop", required = true)
            @RequestParam String nombre) {
        var productos = productosService.findByNombreContaining(nombre);
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productosMapper.toDTOList(productos));
    }

    @Operation(summary = "Buscar productos por rango de precio",
            description = "Busca productos cuyo precio se encuentre dentro del rango especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda por precio completada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Rango de precio inválido"),
            @ApiResponse(responseCode = "204", description = "No se encontraron productos en ese rango de precio")
    })
    @GetMapping("/buscar/precio")
    public ResponseEntity<List<ProductosDTO>> buscarProductosPorRangoPrecio(
            @Parameter(description = "Precio mínimo del rango", example = "100.0", required = true)
            @RequestParam Double precioMinimo,
            @Parameter(description = "Precio máximo del rango", example = "500.0", required = true)
            @RequestParam Double precioMaximo) {

        if (precioMinimo > precioMaximo) {
            return ResponseEntity.badRequest().build();
        }

        var productos = productosService.findByPrecioRange(precioMinimo, precioMaximo);
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productosMapper.toDTOList(productos));
    }

    @Operation(summary = "Activar producto",
            description = "Cambia el estado de un producto a activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto activado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{productoId}/estado/activar")
    public ResponseEntity<ProductosDTO> activarProducto(
            @Parameter(description = "ID único del producto a activar", example = "1", required = true)
            @PathVariable Long productoId) {
        return productosService.activate(productoId)
                .map(productosMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}