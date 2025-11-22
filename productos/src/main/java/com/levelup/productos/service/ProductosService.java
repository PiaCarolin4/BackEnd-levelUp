package com.levelup.productos.service;

import com.levelup.productos.model.Productos;
import com.levelup.productos.repository.ProductosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductosService {

    private final ProductosRepository productosRepository;

    public List<Productos> findAll() {
        return productosRepository.findAll();
    }

    public List<Productos> findAllActivos() {
        return productosRepository.findByActivoTrue();
    }

    public List<Productos> findAllInactivos() {
        return productosRepository.findByActivoFalse();
    }

    public Optional<Productos> findById(Long id) {
        return productosRepository.findById(id);
    }

    public Optional<Productos> findByIdAndActivoTrue(Long id) {
        return productosRepository.findByIdAndActivoTrue(id);
    }

    @Transactional
    public Productos save(Productos producto) {
        // Si es un producto nuevo, establecer como activo por defecto
        if (producto.getId() == null) {
            producto.setActivo(true);
        }
        return productosRepository.save(producto);
    }

    @Transactional
    public Optional<Productos> update(Long id, Productos productoDetails) {
        return productosRepository.findById(id)
                .map(producto -> {
                    producto.setNombreProducto(productoDetails.getNombreProducto());
                    producto.setDescripcionProducto(productoDetails.getDescripcionProducto());
                    producto.setPrecioProducto(productoDetails.getPrecioProducto());
                    producto.setDescuentoProducto(productoDetails.getDescuentoProducto());
                    producto.setCategorias(productoDetails.getCategorias());
                    producto.setActivo(productoDetails.getActivo());
                    return productosRepository.save(producto);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return productosRepository.findById(id)
                .map(producto -> {
                    productosRepository.delete(producto);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<Productos> deactivate(Long id) {
        return productosRepository.findById(id)
                .map(producto -> {
                    producto.setActivo(false);
                    return productosRepository.save(producto);
                });
    }

    @Transactional
    public Optional<Productos> activate(Long id) {
        return productosRepository.findById(id)
                .map(producto -> {
                    producto.setActivo(true);
                    return productosRepository.save(producto);
                });
    }

    public List<Productos> findByNombreContaining(String nombre) {
        return productosRepository.findByNombreProductoContainingIgnoreCase(nombre);
    }

    public List<Productos> findByNombreContainingAndActivoTrue(String nombre) {
        return productosRepository.findByNombreProductoContainingIgnoreCaseAndActivoTrue(nombre);
    }

    public List<Productos> findByPrecioRange(Double minPrecio, Double maxPrecio) {
        return productosRepository.findByPrecioProductoBetween(minPrecio, maxPrecio);
    }

    public List<Productos> findByPrecioRangeAndActivoTrue(Double minPrecio, Double maxPrecio) {
        return productosRepository.findByPrecioProductoBetweenAndActivoTrue(minPrecio, maxPrecio);
    }

    public boolean existsById(Long id) {
        return productosRepository.existsById(id);
    }

    public long count() {
        return productosRepository.count();
    }

    public long countActivos() {
        return productosRepository.countByActivoTrue();
    }

    @Transactional
    public Optional<Productos> actualizarPrecio(Long id, Double nuevoPrecio) {
        return productosRepository.findById(id)
                .map(producto -> {
                    producto.setPrecioProducto(nuevoPrecio);
                    return productosRepository.save(producto);
                });
    }

    @Transactional
    public Optional<Productos> aplicarDescuento(Long id, Double porcentajeDescuento) {
        return productosRepository.findById(id)
                .map(producto -> {
                    Double precioActual = producto.getPrecioProducto();
                    Double descuento = precioActual * (porcentajeDescuento / 100);
                    producto.setDescuentoProducto(descuento);
                    producto.setPrecioProducto(precioActual - descuento);
                    return productosRepository.save(producto);
                });
    }
}