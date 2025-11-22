package com.levelup.productos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductosDTO {

    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombreProducto;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcionProducto;

    @NotNull(message = "La imagen es obrigatoria")
    private String imagenProducto;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    @DecimalMin(value = "0.01", message = "El precio mínimo es 0.01")
    private Double precioProducto;

    @PositiveOrZero(message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
    private Double descuentoProducto;

    private Boolean activo;

    @NotBlank(message = "Debe especificar al menos una categoría")
    private String categorias;
}