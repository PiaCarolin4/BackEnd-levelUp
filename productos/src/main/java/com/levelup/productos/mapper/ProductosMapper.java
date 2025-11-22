package com.levelup.productos.mapper;

import com.levelup.productos.dto.ProductosDTO;
import com.levelup.productos.model.Productos;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductosMapper {

    public ProductosDTO toDTO(Productos producto) {
        if (producto == null) return null;

        return ProductosDTO.builder()
                .id(producto.getId())
                .nombreProducto(producto.getNombreProducto())
                .descripcionProducto(producto.getDescripcionProducto())
                .precioProducto(producto.getPrecioProducto())
                .imagenProducto(producto.getImageProducto())
                .descuentoProducto(producto.getDescuentoProducto())
                .activo(producto.getActivo())
                .categorias(producto.getCategorias())
                .build();
    }

    public Productos toEntity(ProductosDTO dto) {
        if (dto == null) return null;

        return Productos.builder()
                .id(dto.getId())
                .nombreProducto(dto.getNombreProducto())
                .descripcionProducto(dto.getDescripcionProducto())
                .imageProducto(dto.getImagenProducto())
                .precioProducto(dto.getPrecioProducto())
                .descuentoProducto(dto.getDescuentoProducto())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .categorias(dto.getCategorias())
                .build();
    }

    public List<ProductosDTO> toDTOList(List<Productos> productos) {
        return productos.stream()
                .map(this::toDTO)
                .toList();
    }

    public void updateEntityFromDTO(ProductosDTO dto, Productos producto) {
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setDescripcionProducto(dto.getDescripcionProducto());
        producto.setPrecioProducto(dto.getPrecioProducto());
        producto.setImageProducto(dto.getImagenProducto());
        producto.setDescuentoProducto(dto.getDescuentoProducto());
        producto.setCategorias(dto.getCategorias());
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }
    }
}
