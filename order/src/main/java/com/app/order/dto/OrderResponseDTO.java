package com.app.order.dto;

import com.app.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long id;
    private String numeroOrden;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private OrderStatus estado;
    private Double subtotal;
    private Double totalDescuentos;
    private Double total;
    private DireccionDespachoDTO direccionDespacho;
    private MetodoPagoDTO metodoPago;
    private List<OrderItemDTO> items;
}