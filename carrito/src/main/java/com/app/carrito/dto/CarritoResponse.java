package com.app.carrito.dto;

import com.app.carrito.model.Carrito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponse {
    private Long id;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Carrito.EstadoCarrito estado;
    private BigDecimal subtotal;
    private BigDecimal totalDescuentos;
    private BigDecimal total;
    private List<CarritoItemResponse> items;

    public CarritoResponse(Carrito carrito) {
        this.id = carrito.getId();
        this.usuarioId = carrito.getUsuarioId();
        this.fechaCreacion = carrito.getFechaCreacion();
        this.fechaActualizacion = carrito.getFechaActualizacion();
        this.estado = carrito.getEstado();
        this.subtotal = carrito.getSubtotal();
        this.totalDescuentos = carrito.getTotalDescuentos();
        this.total = carrito.getTotal();
    }
}