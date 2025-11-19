package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.repository;

import com.prediccion.apppredicciongm.models.AnalisisEstacionalidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface IAnalisisEstacionalidadRepositorio extends JpaRepository<AnalisisEstacionalidad, Long> {

    /**
     * Busca análisis por producto ID
     */
    Optional<AnalisisEstacionalidad> findByProductoIdAndActivoTrue(@Param("productoId") Long productoId);

    /**
     * Busca todos los análisis activos
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.activo = true")
    List<AnalisisEstacionalidad> findAllActivos();

    /**
     * Busca productos con estacionalidad detectada
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.tieneEstacionalidad = true AND a.activo = true")
    List<AnalisisEstacionalidad> findProductosConEstacionalidad();

    /**
     * Busca productos sin estacionalidad
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.tieneEstacionalidad = false AND a.activo = true")
    List<AnalisisEstacionalidad> findProductosSinEstacionalidad();

    /**
     * Busca análisis por intensidad de estacionalidad
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.intensidadEstacionalidad >= :intensidadMinima AND a.activo = true ORDER BY a.intensidadEstacionalidad DESC")
    List<AnalisisEstacionalidad> findByIntensidadEstacionalidadGreaterThanEqual(@Param("intensidadMinima") java.math.BigDecimal intensidadMinima);

    /**
     * Cuenta productos con estacionalidad
     */
    @Query("SELECT COUNT(a) FROM AnalisisEstacionalidad a WHERE a.tieneEstacionalidad = true AND a.activo = true")
    Long countProductosConEstacionalidad();

    /**
     * Cuenta productos analizados
     */
    @Query("SELECT COUNT(a) FROM AnalisisEstacionalidad a WHERE a.activo = true")
    Long countProductosAnalizados();

    /**
     * Busca por mes de mayor demanda
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.mesMayorDemanda = :mes AND a.activo = true")
    List<AnalisisEstacionalidad> findByMesMayorDemanda(@Param("mes") Integer mes);

    /**
     * Busca por mes de menor demanda
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.mesMenorDemanda = :mes AND a.activo = true")
    List<AnalisisEstacionalidad> findByMesMenorDemanda(@Param("mes") Integer mes);

    /**
     * Obtiene análisis con paginación
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.activo = true ORDER BY a.fechaAnalisis DESC")
    Page<AnalisisEstacionalidad> findAllActivosPageable(Pageable pageable);

    /**
     * Busca análisis recientes (últimos 30 días)
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.fechaAnalisis >= :fechaLimite AND a.activo = true ORDER BY a.fechaAnalisis DESC")
    List<AnalisisEstacionalidad> findAnalisisRecientes(@Param("fechaLimite") java.time.LocalDate fechaLimite);
    
    /**
     * Busca análisis por fecha específica
     */
    @Query("SELECT a FROM AnalisisEstacionalidad a WHERE a.fechaAnalisis = :fecha AND a.activo = true")
    List<AnalisisEstacionalidad> findByFechaAnalisis(@Param("fecha") java.time.LocalDate fecha);

    /**
     * Elimina (desactiva) análisis antiguos
     */
    @Modifying
    @Transactional
    @Query("UPDATE AnalisisEstacionalidad a SET a.activo = false WHERE a.fechaAnalisis < :fechaLimite")
    void desactivarAnalisisAntiguos(@Param("fechaLimite") java.time.LocalDate fechaLimite);
}