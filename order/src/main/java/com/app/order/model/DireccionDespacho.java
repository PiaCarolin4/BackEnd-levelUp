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
public class DireccionDespacho {
    private String calle;
    private String numero;
    private String departamento;
    private String comuna;
    private String ciudad;
    private String region;
    private String codigoPostal;
    private String referencias;
}

