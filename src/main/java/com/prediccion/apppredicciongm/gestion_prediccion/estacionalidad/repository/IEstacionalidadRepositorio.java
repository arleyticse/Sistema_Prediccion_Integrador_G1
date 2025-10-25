package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.EstacionalidadProducto;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

/**
 * Repositorio para acceso a datos de estacionalidad de productos.
 * Proporciona métodos de consulta personalizados para obtener factores estacionales.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
@Repository
public interface IEstacionalidadRepositorio extends JpaRepository<EstacionalidadProducto, Long> {

    /**
     * Busca un patrón estacional por producto y mes específico.
     *
     * @param producto entidad del producto
     * @param mes número del mes (1-12)
     * @return Optional con el patrón estacional si existe
     */
    @Query("SELECT e FROM EstacionalidadProducto e WHERE e.producto = :producto AND e.mes = :mes")
    Optional<EstacionalidadProducto> findByProductoAndMes(
            @Param("producto") Producto producto,
            @Param("mes") Integer mes);

    /**
     * Obtiene todos los patrones estacionales de un producto.
     *
     * @param productoId ID del producto
     * @return lista de patrones estacionales ordenados por mes
     */
    @Query("SELECT e FROM EstacionalidadProducto e WHERE e.producto.productoId = :productoId " +
           "ORDER BY e.mes ASC")
    List<EstacionalidadProducto> findByProductoId(@Param("productoId") Integer productoId);

    /**
     * Obtiene patrones estacionales de un producto con paginación.
     *
     * @param productoId ID del producto
     * @param pageable información de paginación
     * @return página de patrones estacionales
     */
    @Query("SELECT e FROM EstacionalidadProducto e WHERE e.producto.productoId = :productoId " +
           "ORDER BY e.mes ASC")
    Page<EstacionalidadProducto> findByProductoIdPaginado(
            @Param("productoId") Integer productoId,
            Pageable pageable);

    /**
     * Obtiene patrones estacionales de un producto para un año específico.
     *
     * @param producto entidad del producto
     * @param anioReferencia año de referencia
     * @return lista de patrones para el año especificado
     */
    @Query("SELECT e FROM EstacionalidadProducto e WHERE e.producto = :producto " +
           "AND e.anioReferencia = :anio ORDER BY e.mes ASC")
    List<EstacionalidadProducto> findByProductoAndAnioReferencia(
            @Param("producto") Producto producto,
            @Param("anio") Integer anioReferencia);

    /**
     * Cuenta cuántos patrones estacionales existen para un producto.
     *
     * @param productoId ID del producto
     * @return cantidad de patrones
     */
    @Query("SELECT COUNT(e) FROM EstacionalidadProducto e WHERE e.producto.productoId = :productoId")
    long countByProductoId(@Param("productoId") Integer productoId);

    /**
     * Busca patrones estacionales por descripción de temporada (búsqueda flexible).
     *
     * @param descripcion palabra clave de búsqueda
     * @param pageable información de paginación
     * @return página de patrones que coinciden con la búsqueda
     */
    @Query("SELECT e FROM EstacionalidadProducto e WHERE UPPER(e.descripcionTemporada) LIKE UPPER(CONCAT('%', :desc, '%')) " +
           "ORDER BY e.anioReferencia DESC, e.mes ASC")
    Page<EstacionalidadProducto> findByDescripcionTemporada(
            @Param("desc") String descripcion,
            Pageable pageable);
}
