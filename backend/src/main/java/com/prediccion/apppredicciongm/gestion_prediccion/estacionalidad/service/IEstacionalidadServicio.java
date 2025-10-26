package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.request.EstacionalidadCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.response.EstacionalidadResponse;

/**
 * Interfaz de servicio para gestión de patrones estacionales de productos.
 * Define operaciones para crear, actualizar, obtener y eliminar estacionalidades.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
public interface IEstacionalidadServicio {

    /**
     * Crea un nuevo patrón estacional para un producto.
     *
     * @param request datos del patrón estacional a crear
     * @return DTO de respuesta con los datos creados
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadYaExisteException
     *         si ya existe un patrón para ese producto y mes
     * @throws IllegalArgumentException si el producto no existe
     */
    EstacionalidadResponse crearEstacionalidad(EstacionalidadCreateRequest request);

    /**
     * Actualiza un patrón estacional existente.
     *
     * @param estacionalidadId ID del patrón estacional a actualizar
     * @param request nuevos datos del patrón
     * @return DTO de respuesta con los datos actualizados
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadNotFoundException
     *         si el patrón no existe
     * @throws IllegalArgumentException si el producto no existe
     */
    EstacionalidadResponse actualizarEstacionalidad(Long estacionalidadId, EstacionalidadCreateRequest request);

    /**
     * Obtiene un patrón estacional por su ID.
     *
     * @param estacionalidadId ID del patrón estacional
     * @return DTO de respuesta con los datos del patrón
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadNotFoundException
     *         si el patrón no existe
     */
    EstacionalidadResponse obtenerEstacionalidadPorId(Long estacionalidadId);

    /**
     * Obtiene el patrón estacional de un producto para un mes específico.
     *
     * @param productoId ID del producto
     * @param mes número del mes (1-12)
     * @return DTO de respuesta con el patrón estacional
     * @throws IllegalArgumentException si el mes es inválido o el producto no existe
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadNotFoundException
     *         si no existe patrón para ese producto y mes
     */
    EstacionalidadResponse obtenerEstacionalidadPorProductoYMes(Integer productoId, Integer mes);

    /**
     * Obtiene todos los patrones estacionales de un producto.
     *
     * @param productoId ID del producto
     * @return lista de DTOs con los patrones estacionales del producto
     * @throws IllegalArgumentException si el producto no existe
     */
    List<EstacionalidadResponse> obtenerEstacionalidadPorProducto(Integer productoId);

    /**
     * Obtiene patrones estacionales de un producto con paginación.
     *
     * @param productoId ID del producto
     * @param pageable información de paginación
     * @return página de DTOs con los patrones estacionales
     * @throws IllegalArgumentException si el producto no existe
     */
    Page<EstacionalidadResponse> obtenerEstacionalidadPorProductoPaginado(Integer productoId, Pageable pageable);

    /**
     * Lista todos los patrones estacionales con paginación.
     *
     * @param pageable información de paginación
     * @return página de DTOs con todos los patrones estacionales
     */
    Page<EstacionalidadResponse> listarTodasLasEstacionalidades(Pageable pageable);

    /**
     * Busca patrones estacionales por descripción de temporada.
     *
     * @param descripcion palabra clave de búsqueda (ej: "Navidad", "Verano")
     * @param pageable información de paginación
     * @return página de DTOs que coinciden con la búsqueda
     */
    Page<EstacionalidadResponse> buscarPorDescripcionTemporada(String descripcion, Pageable pageable);

    /**
     * Elimina un patrón estacional.
     *
     * @param estacionalidadId ID del patrón estacional a eliminar
     * @return true si la eliminación fue exitosa
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadNotFoundException
     *         si el patrón no existe
     */
    boolean eliminarEstacionalidad(Long estacionalidadId);

    /**
     * Calcula el factor estacional promedio para un producto considerando todos sus patrones.
     *
     * @param productoId ID del producto
     * @return factor promedio calculado
     * @throws IllegalArgumentException si el producto no existe o no tiene patrones
     */
    BigDecimal calcularFactorEstacionalPromedio(Integer productoId);

    /**
     * Calcula la demanda ajustada por estacionalidad.
     * Multiplica la demanda base por el factor estacional correspondiente al mes.
     *
     * @param productoId ID del producto
     * @param mes mes para el cual calcular (1-12)
     * @param demandaBase demanda sin ajuste estacional
     * @return demanda ajustada por factor estacional
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadNotFoundException
     *         si no existe patrón para ese mes
     */
    Integer calcularDemandaAjustadaPorEstacionalidad(Integer productoId, Integer mes, Integer demandaBase);
}
