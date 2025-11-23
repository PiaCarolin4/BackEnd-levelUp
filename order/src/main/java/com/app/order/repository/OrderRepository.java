package com.app.order.repository;

import com.app.order.model.Order;
import com.app.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    Optional<Order> findByIdAndUsuarioId(Long id, Long usuarioId);
    List<Order> findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(Long usuarioId, OrderStatus estado);
    Optional<Order> findByNumeroOrden(String numeroOrden);
    Long countByUsuarioId(Long usuarioId);
    @Query("SELECT o FROM Order o WHERE o.usuarioId = :usuarioId AND o.fechaCreacion BETWEEN :startDate AND :endDate ORDER BY o.fechaCreacion DESC")
    List<Order> findByUsuarioIdAndFechaCreacionBetween(
            @Param("usuarioId") Long usuarioId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
    List<Order> findByEstado(OrderStatus estado);
    boolean existsByNumeroOrden(String numeroOrden);
}