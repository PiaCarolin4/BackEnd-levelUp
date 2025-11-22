package com.levelup.productos.repository;

import com.levelup.productos.model.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, Long> {

    List<Productos> findByActivoTrue();
    List<Productos> findByNombreProductoContainingIgnoreCase(String nombre);
    List<Productos> findByPrecioProductoBetween(Double minPrecio, Double maxPrecio);

    List<Productos> findByActivoFalse();
    Optional<Productos> findByIdAndActivoTrue(Long id);
    List<Productos> findByNombreProductoContainingIgnoreCaseAndActivoTrue(String nombre);
    List<Productos> findByPrecioProductoBetweenAndActivoTrue(Double minPrecio, Double maxPrecio);
    long countByActivoTrue();

    @Query("SELECT p FROM Productos p WHERE p.precioProducto > :precio AND p.activo = true")
    List<Productos> findProductosCaros(@Param("precio") Double precio);

    @Query("SELECT p FROM Productos p WHERE p.descuentoProducto > 0 AND p.activo = true")
    List<Productos> findProductosConDescuento();

}