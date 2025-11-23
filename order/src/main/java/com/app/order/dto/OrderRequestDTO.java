package com.app.order.dto;

import com.app.order.model.DireccionDespacho;
import com.app.order.model.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {
    private Long carritoId;
    private DireccionDespacho direccionDespacho;
    private MetodoPago metodoPago;
}