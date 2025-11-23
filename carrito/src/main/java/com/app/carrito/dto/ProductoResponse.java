package com.app.carrito.dto;

import lombok.Data;

@Data
public class ProductoResponse {
    private Long id;
    private String nombreProducto;
    private String descripcionProducto;
    private String imagenProducto;
    private Double precioProducto;
    private Integer descuentoProducto;
    private Boolean activo;
    private String categorias;
}
