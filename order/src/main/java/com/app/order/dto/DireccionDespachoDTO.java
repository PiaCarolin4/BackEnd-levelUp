package com.app.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionDespachoDTO {
    private String calle;
    private String numero;
    private String departamento;
    private String comuna;
    private String ciudad;
    private String region;
    private String codigoPostal;
    private String referencias;
}