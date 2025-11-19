package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.repository;

import com.prediccion.apppredicciongm.models.DetalleOrdenCompra;
import com.prediccion.apppredicciongm.models.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la gestión de detalles de órdenes de compra.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-12
 */
@Repository
public interface IDetalleOrdenCompraRepositorio extends JpaRepository<DetalleOrdenCompra, Long> {

    /**
     * Encuentra todos los detalles de una orden de compra específica
     * 
     * @param ordenCompra la orden de compra
     * @return lista de detalles asociados a la orden
     */
    List<DetalleOrdenCompra> findByOrdenCompra(OrdenCompra ordenCompra);

    /**
     * Encuentra detalles por ID de orden de compra
     * 
     * @param ordenId ID de la orden de compra
     * @return lista de detalles asociados a la orden
     */
    @Query("SELECT d FROM DetalleOrdenCompra d WHERE d.ordenCompra.ordenCompraId = :ordenId")
    List<DetalleOrdenCompra> findByOrdenCompraId(@Param("ordenId") Long ordenId);

    /**
     * Encuentra detalles por ID de producto
     * 
     * @param productoId ID del producto
     * @return lista de detalles que contienen el producto
     */
    @Query("SELECT d FROM DetalleOrdenCompra d WHERE d.producto.productoId = :productoId")
    List<DetalleOrdenCompra> findByProductoId(@Param("productoId") Integer productoId);

    /**
     * Cuenta la cantidad de productos distintos en una orden
     * 
     * @param ordenId ID de la orden de compra
     * @return cantidad de productos distintos
     */
    @Query("SELECT COUNT(DISTINCT d.producto.productoId) FROM DetalleOrdenCompra d WHERE d.ordenCompra.ordenCompraId = :ordenId")
    Long countDistinctProductosByOrdenId(@Param("ordenId") Long ordenId);

    /**
     * Elimina todos los detalles de una orden de compra
     * 
     * @param ordenId ID de la orden de compra
     */
    @Query("DELETE FROM DetalleOrdenCompra d WHERE d.ordenCompra.ordenCompraId = :ordenId")
    void deleteByOrdenCompraId(@Param("ordenId") Long ordenId);
}
