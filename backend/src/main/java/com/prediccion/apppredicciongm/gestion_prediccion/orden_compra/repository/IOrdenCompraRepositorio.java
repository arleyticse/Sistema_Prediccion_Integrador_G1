package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.repository;

import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;
import com.prediccion.apppredicciongm.models.OrdenCompra;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad OrdenCompra.
 * Proporciona métodos para acceder y gestionar órdenes de compra automáticas.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Repository
public interface IOrdenCompraRepositorio extends JpaRepository<OrdenCompra, Long> {

    /**
     * Obtiene la orden más reciente ordenada por fecha descendente.
     *
     * @return Optional con la orden más reciente
     */
    Optional<OrdenCompra> findFirstByOrderByFechaOrdenDesc();

    /**
     * Obtiene todas las órdenes ordenadas por fecha descendente.
     *
     * @return lista de todas las órdenes ordenadas
     */
    List<OrdenCompra> findAllByOrderByFechaOrdenDesc();

    /**
     * Obtiene órdenes en un estado específico.
     *
     * @param estado el estado de la orden
     * @return lista de órdenes en ese estado
     */
    List<OrdenCompra> findByEstadoOrden(EstadoOrdenCompra estado);

    /**
     * Obtiene órdenes de un estado específico ordenadas por fecha descendente.
     *
     * @param estado el estado de la orden
     * @return lista de órdenes que coinciden, ordenadas por fecha
     */
    List<OrdenCompra> findByEstadoOrdenOrderByFechaOrdenDesc(EstadoOrdenCompra estado);

    /**
     * Cuenta las órdenes en un estado específico.
     *
     * @param estado el estado a contar
     * @return número de órdenes en ese estado
     */
    long countByEstadoOrden(EstadoOrdenCompra estado);

    /**
     * Obtiene órdenes generadas en un rango de fechas.
     *
     * @param fechaDesde fecha inicial
     * @param fechaHasta fecha final
     * @return lista de órdenes en el rango
     */
    @Query("SELECT o FROM OrdenCompra o WHERE o.fechaOrden BETWEEN :desde AND :hasta ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findByFechaOrdenBetween(@Param("desde") LocalDate fechaDesde,
                                               @Param("hasta") LocalDate fechaHasta);

    /**
     * Obtiene órdenes marcadas como generadas automáticamente.
     *
     * @return lista de órdenes automáticas ordenadas por fecha
     */
    @Query("SELECT o FROM OrdenCompra o WHERE o.generadaAutomaticamente = true ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findOrdenesAutomaticas();

    /**
     * Obtiene órdenes de un proveedor específico.
     *
     * @param proveedorId el ID del proveedor
     * @return lista de órdenes del proveedor
     */
    @Query("SELECT o FROM OrdenCompra o WHERE o.proveedor.proveedorId = :proveedorId ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findByProveedorId(@Param("proveedorId") Integer proveedorId);

    /**
     * Elimina órdenes antiguas (más de N días) en estado CANCELADA.
     *
     * @param fechaLimite fecha límite para eliminación
     */
    @Query("DELETE FROM OrdenCompra o WHERE o.estadoOrden = 'CANCELADA' AND o.fechaOrden < :fecha")
    void deleteOldCancelledOrders(@Param("fecha") LocalDate fechaLimite);
}
