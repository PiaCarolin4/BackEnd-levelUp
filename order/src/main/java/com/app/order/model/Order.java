package com.app.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, unique = true)
    private String numeroOrden;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus estado;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double totalDescuentos;

    @Column(nullable = false)
    private Double total;

    @Embedded
    private DireccionDespacho direccionDespacho;

    @Embedded
    private MetodoPago metodoPago;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;
}