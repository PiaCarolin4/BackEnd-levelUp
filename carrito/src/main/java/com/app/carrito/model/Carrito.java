package com.app.carrito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCarrito estado;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_descuentos", precision = 10, scale = 2)
    private BigDecimal totalDescuentos;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    // Cambia a EAGER o usa JOIN FETCH en las consultas
    @OneToMany(mappedBy = "carritoId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CarritoItem> items = new ArrayList<>();
    // Enums para el estado del carrito
    public enum EstadoCarrito {
        ACTIVO,
        COMPLETADO,
        ABANDONADO
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoCarrito.ACTIVO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Método para calcular totales
    public void calcularTotales() {
        this.subtotal = BigDecimal.ZERO;
        this.totalDescuentos = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;

        if (items != null && !items.isEmpty()) {
            for (CarritoItem item : items) {
                item.calcularPrecioTotal();
                this.subtotal = this.subtotal.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
                this.totalDescuentos = this.totalDescuentos.add(
                        item.getDescuentoAplicado() != null ?
                                item.getDescuentoAplicado().multiply(BigDecimal.valueOf(item.getCantidad())) :
                                BigDecimal.ZERO
                );
                this.total = this.total.add(item.getPrecioTotal());
            }
        }
    }
}