package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository;

import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Prediccion.
 * Proporciona métodos para acceder y gestionar predicciones.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Repository
public interface IPrediccionRepositorio extends JpaRepository<Prediccion, Integer> {

    /**
     * Obtiene la predicción más reciente para un producto.
     *
     * @param producto el producto
     * @return Optional con la predicción más reciente
     */
    Optional<Prediccion> findFirstByProductoOrderByFechaEjecucionDesc(Producto producto);

    /**
     * Obtiene todas las predicciones de un producto.
     *
     * @param producto el producto
     * @return lista de predicciones del producto
     */
    List<Prediccion> findByProductoOrderByFechaEjecucionDesc(Producto producto);

    /**
     * Obtiene predicciones generadas en un rango de fechas.
     *
     * @param fechaDesde fecha inicial
     * @param fechaHasta fecha final
     * @return lista de predicciones en el rango
     */
    @Query("SELECT p FROM Prediccion p WHERE p.fechaEjecucion BETWEEN :desde AND :hasta ORDER BY p.fechaEjecucion DESC")
    List<Prediccion> findByFechaEjecucionBetween(@Param("desde") LocalDateTime fechaDesde, 
                                                   @Param("hasta") LocalDateTime fechaHasta);

    /**
     * Obtiene predicciones vigentes (recientemente creadas).
     *
     * @return lista de predicciones vigentes
     */
    @Query("SELECT p FROM Prediccion p ORDER BY p.fechaEjecucion DESC")
    List<Prediccion> findPrediccionesVigentes();

    /**
     * Obtiene predicciones vigentes para un producto específico.
     *
     * @param producto el producto
     * @return lista de predicciones vigentes del producto
     */
    @Query("SELECT p FROM Prediccion p WHERE p.producto = :producto ORDER BY p.fechaEjecucion DESC")
    List<Prediccion> findPrediccionesVigentesPorProducto(@Param("producto") Producto producto);

    /**
     * Cuenta el número de predicciones para un producto.
     *
     * @param producto el producto
     * @return número de predicciones
     */
    long countByProducto(Producto producto);

    /**
     * Elimina todas las predicciones de un producto.
     *
     * @param producto el producto
     */
    void deleteByProducto(Producto producto);

    /**
     * Busca predicción existente por configuración (para evitar duplicados).
     * 
     * @param producto el producto
     * @param algoritmo algoritmo usado
     * @param horizonte horizonte de tiempo
     * @return Optional con predicción si existe
     */
    Optional<Prediccion> findByProductoAndAlgoritmoUsadoAndHorizonteTiempo(
        Producto producto,
        String algoritmo,
        Integer horizonte
    );

    /**
     * Obtiene predicciones antiguas de un producto (más allá del límite de historial).
     * Usado para limpieza automática manteniendo solo las N más recientes.
     * 
     * @param producto el producto
     * @param algoritmo algoritmo usado
     * @param horizonte horizonte de tiempo
     * @return lista de predicciones ordenadas por fecha descendente
     */
    @Query("""
        SELECT p FROM Prediccion p 
        WHERE p.producto = :producto 
          AND p.algoritmoUsado = :algoritmo
          AND p.horizonteTiempo = :horizonte
        ORDER BY p.fechaEjecucion DESC
        """)
    List<Prediccion> findPrediccionesAntiguasParaLimpieza(
        @Param("producto") Producto producto,
        @Param("algoritmo") String algoritmo,
        @Param("horizonte") Integer horizonte
    );

    /**
     * Cuenta predicciones por configuración específica.
     * 
     * @param producto el producto
     * @param algoritmo algoritmo usado
     * @param horizonte horizonte de tiempo
     * @return número de predicciones con esa configuración
     */
    long countByProductoAndAlgoritmoUsadoAndHorizonteTiempo(
        Producto producto,
        String algoritmo,
        Integer horizonte
    );
}
