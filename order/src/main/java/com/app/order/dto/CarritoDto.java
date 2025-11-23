package com.app.order.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CarritoDto {
    private Long id;
    private Long usuarioId;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Double subtotal;
    private Double total;
    private Double totalDescuentos;
    private List<ItemCarrito> items;

    @Data
    public static class ItemCarrito {
        private Long id;
        private Long productoId;
        private Integer cantidad;
        private Double precioUnitario;
        private Double descuentoAplicado;
        private Double precioTotal;
        private String nombreProducto;
        private String imagenProducto;
        private String descripcionProducto;
        private Double descuentoProducto;
        private Double descuentoTotal;
        private Double precioFinalUnitario;
        private Double subtotal;
    }
}