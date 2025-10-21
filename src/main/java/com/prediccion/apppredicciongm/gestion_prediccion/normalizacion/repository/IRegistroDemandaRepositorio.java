package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.RegistroDemanda;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de RegistroDemanda.
 * Proporciona métodos para consultar y persistir registros de demanda histórica.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
@Repository
public interface IRegistroDemandaRepositorio extends JpaRepository<RegistroDemanda, Integer> {

    /**
     * Busca todos los registros de demanda para un producto específico.
     *
     * @param producto el producto a buscar
     * @return lista de registros de demanda del producto
     */
    List<RegistroDemanda> findByProducto(Producto producto);

    /**
     * Busca registros de demanda para un producto dentro de un rango de fechas.
     *
     * @param producto el producto a buscar
     * @param fechaInicio fecha de inicio del rango (inclusive)
     * @param fechaFin fecha de fin del rango (inclusive)
     * @return lista de registros dentro del rango de fechas
     */
    @Query("SELECT rd FROM RegistroDemanda rd WHERE rd.producto = :producto " +
           "AND CAST(rd.fechaRegistro AS DATE) BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY rd.fechaRegistro ASC")
    List<RegistroDemanda> findByProductoAndFechaRange(
            @Param("producto") Producto producto,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Busca un registro de demanda existente para una fecha y producto específicos.
     * Útil para evitar duplicados.
     *
     * @param producto el producto
     * @param fechaRegistro la fecha en formato YYYY-MM-DD
     * @return Optional con el registro si existe
     */
    @Query("SELECT rd FROM RegistroDemanda rd WHERE rd.producto = :producto " +
           "AND CAST(rd.fechaRegistro AS DATE) = CAST(:fechaRegistro AS DATE)")
    Optional<RegistroDemanda> findByProductoAndFecha(
            @Param("producto") Producto producto,
            @Param("fechaRegistro") LocalDate fechaRegistro
    );

    /**
     * Obtiene el registro de demanda más reciente para un producto.
     *
     * @param producto el producto a buscar
     * @return Optional con el registro más reciente
     */
    @Query("SELECT rd FROM RegistroDemanda rd WHERE rd.producto = :producto " +
           "ORDER BY rd.fechaRegistro DESC LIMIT 1")
    Optional<RegistroDemanda> findLastByProducto(@Param("producto") Producto producto);

    /**
     * Busca registros por período (ej: "2025-10" para octubre 2025).
     *
     * @param producto el producto
     * @param periodoRegistro el período en formato YYYY-MM
     * @return lista de registros del período
     */
    List<RegistroDemanda> findByProductoAndPeriodoRegistro(Producto producto, String periodoRegistro);

    /**
     * Cuenta el número de registros de demanda disponibles para un producto.
     * Útil para validar si hay suficientes datos históricos.
     *
     * @param producto el producto
     * @return número de registros
     */
    long countByProducto(Producto producto);

    /**
     * Elimina todos los registros de demanda para un producto específico.
     * ADVERTENCIA: Esta operación es destructiva.
     *
     * @param producto el producto
     */
    void deleteByProducto(Producto producto);
}
