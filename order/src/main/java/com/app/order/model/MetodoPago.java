package com.app.order.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPago {
    private String tipo; // TARJETA, TRANSFERENCIA, etc.
    private String ultimosDigitos;
    private String nombreTitular;
    private String banco;
    private String numeroTransaccion;
}