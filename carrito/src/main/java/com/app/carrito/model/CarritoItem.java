package com.app.carrito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "carrito_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "carrito_id", nullable = false)
    private Long carritoId;

    // Agrega esta relación bidireccional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", insertable = false, updatable = false)
    private Carrito carrito;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "descuento_aplicado", precision = 10, scale = 2)
    private BigDecimal descuentoAplicado;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;


    // Campos transcientes para mostrar información del producto
    @Transient
    private String nombreProducto;

    @Transient
    private String imagenProducto;

    @Transient
    private String descripcionProducto;

    @Transient
    private BigDecimal descuentoProducto;

    // Método para calcular el precio total
    public void calcularPrecioTotal() {
        BigDecimal precioBase = this.precioUnitario;
        BigDecimal descuento = this.descuentoAplicado != null ? this.descuentoAplicado : BigDecimal.ZERO;
        BigDecimal precioConDescuento = precioBase.subtract(descuento);
        this.precioTotal = precioConDescuento.multiply(BigDecimal.valueOf(this.cantidad));
    }
}