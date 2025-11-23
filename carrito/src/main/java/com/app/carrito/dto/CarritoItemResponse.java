package com.app.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemResponse {
    private Long id;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoAplicado;
    private BigDecimal precioTotal;
    private String nombreProducto;
    private String imagenProducto;
    private String descripcionProducto;
    private BigDecimal descuentoProducto;

    // Métodos auxiliares para cálculos
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public BigDecimal getDescuentoTotal() {
        return descuentoAplicado != null ?
                descuentoAplicado.multiply(BigDecimal.valueOf(cantidad)) :
                BigDecimal.ZERO;
    }

    public BigDecimal getPrecioFinalUnitario() {
        return descuentoAplicado != null ?
                precioUnitario.subtract(descuentoAplicado) :
                precioUnitario;
    }
}