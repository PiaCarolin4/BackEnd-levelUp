package com.app.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Long productoId;
    private String nombreProducto;
    private String descripcionProducto;
    private String imagenProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double descuentoProducto;
    private Double descuentoAplicado;
    private Double precioFinalUnitario;
    private Double descuentoTotal;
    private Double subtotal;
    private Double precioTotal;
}