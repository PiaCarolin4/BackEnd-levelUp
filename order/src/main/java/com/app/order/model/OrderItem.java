package com.app.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String nombreProducto;

    @Column(length = 1000)
    private String descripcionProducto;

    @Column(length = 500)
    private String imagenProducto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double descuentoProducto;

    @Column(nullable = false)
    private Double descuentoAplicado;

    @Column(nullable = false)
    private Double precioFinalUnitario;

    @Column(nullable = false)
    private Double descuentoTotal;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double precioTotal;
}