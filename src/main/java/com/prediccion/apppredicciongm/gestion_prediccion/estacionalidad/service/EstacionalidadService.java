package com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.request.EstacionalidadCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.dto.response.EstacionalidadResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadInvalidaException;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadNotFoundException;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.errors.EstacionalidadYaExisteException;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.mapper.EstacionalidadMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.repository.IEstacionalidadRepositorio;
import com.prediccion.apppredicciongm.models.EstacionalidadProducto;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de estacionalidad de productos.
 * Gestiona patrones estacionales de demanda para mejorar predicciones.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-23
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EstacionalidadService implements IEstacionalidadServicio {

    private final IEstacionalidadRepositorio repositorio;
    private final IProductoRepositorio productoRepositorio;
    private final EstacionalidadMapper mapper;

    @Override
    public EstacionalidadResponse crearEstacionalidad(EstacionalidadCreateRequest request) {
        log.info("Iniciando creacion de estacionalidad para producto ID: {}, mes: {}", 
                request.getProductoId(), request.getMes());

        validarDatosRequest(request);

        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> {
                    log.error("Producto no encontrado: {}", request.getProductoId());
                    return new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductoId());
                });

        boolean yaExiste = repositorio.findByProductoAndMes(producto, request.getMes()).isPresent();
        if (yaExiste) {
            log.warn("Intento de crear estacionalidad duplicada para producto: {}, mes: {}", 
                    request.getProductoId(), request.getMes());
            throw new EstacionalidadYaExisteException(
                    "Ya existe un patrón estacional para el producto " + request.getProductoId() + 
                    " en el mes " + request.getMes());
        }

        EstacionalidadProducto estacionalidad = mapper.toEntity(request);
        estacionalidad.setProducto(producto);

        EstacionalidadProducto guardada = repositorio.save(estacionalidad);
        log.info("Estacionalidad creada exitosamente con ID: {}", guardada.getEstacionalidadId());

        return mapper.toResponse(guardada);
    }

    @Override
    public EstacionalidadResponse actualizarEstacionalidad(Long estacionalidadId, EstacionalidadCreateRequest request) {
        log.info("Iniciando actualizacion de estacionalidad ID: {}", estacionalidadId);

        validarDatosRequest(request);

        EstacionalidadProducto estacionalidad = repositorio.findById(estacionalidadId)
                .orElseThrow(() -> {
                    log.error("Estacionalidad no encontrada: {}", estacionalidadId);
                    return new EstacionalidadNotFoundException("Estacionalidad no encontrada con ID: " + estacionalidadId);
                });

        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductoId()));

        mapper.updateEntityFromDto(request, estacionalidad);
        estacionalidad.setProducto(producto);

        EstacionalidadProducto actualizada = repositorio.save(estacionalidad);
        log.info("Estacionalidad actualizada exitosamente: {}", estacionalidadId);

        return mapper.toResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public EstacionalidadResponse obtenerEstacionalidadPorId(Long estacionalidadId) {
        log.debug("Obteniendo estacionalidad con ID: {}", estacionalidadId);

        EstacionalidadProducto estacionalidad = repositorio.findById(estacionalidadId)
                .orElseThrow(() -> {
                    log.error("Estacionalidad no encontrada: {}", estacionalidadId);
                    return new EstacionalidadNotFoundException("Estacionalidad no encontrada con ID: " + estacionalidadId);
                });

        return mapper.toResponse(estacionalidad);
    }

    @Override
    @Transactional(readOnly = true)
    public EstacionalidadResponse obtenerEstacionalidadPorProductoYMes(Integer productoId, Integer mes) {
        log.debug("Obteniendo estacionalidad para producto: {}, mes: {}", productoId, mes);

        if (mes < 1 || mes > 12) {
            log.warn("Mes invalido: {}", mes);
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }

        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        EstacionalidadProducto estacionalidad = repositorio.findByProductoAndMes(producto, mes)
                .orElseThrow(() -> {
                    log.warn("No existe estacionalidad para producto: {}, mes: {}", productoId, mes);
                    return new EstacionalidadNotFoundException(
                            "No existe patrón estacional para el producto " + productoId + " en el mes " + mes);
                });

        return mapper.toResponse(estacionalidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstacionalidadResponse> obtenerEstacionalidadPorProducto(Integer productoId) {
        log.info("Obteniendo todas las estacionalidades para producto: {}", productoId);

        productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        List<EstacionalidadProducto> estacionalidades = repositorio.findByProductoId(productoId);
        log.debug("Se encontraron {} patrones estacionales para producto: {}", 
                estacionalidades.size(), productoId);

        return estacionalidades.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstacionalidadResponse> obtenerEstacionalidadPorProductoPaginado(Integer productoId, Pageable pageable) {
        log.info("Obteniendo estacionalidades paginadas para producto: {}", productoId);

        productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Page<EstacionalidadProducto> pagina = repositorio.findByProductoIdPaginado(productoId, pageable);
        return pagina.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstacionalidadResponse> listarTodasLasEstacionalidades(Pageable pageable) {
        log.info("Listando todas las estacionalidades");

        Page<EstacionalidadProducto> pagina = repositorio.findAll(pageable);
        return pagina.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EstacionalidadResponse> buscarPorDescripcionTemporada(String descripcion, Pageable pageable) {
        log.info("Buscando estacionalidades por descripcion: {}", descripcion);

        Page<EstacionalidadProducto> pagina = repositorio.findByDescripcionTemporada(descripcion, pageable);
        return pagina.map(mapper::toResponse);
    }

    @Override
    public boolean eliminarEstacionalidad(Long estacionalidadId) {
        log.info("Eliminando estacionalidad: {}", estacionalidadId);

        EstacionalidadProducto estacionalidad = repositorio.findById(estacionalidadId)
                .orElseThrow(() -> {
                    log.error("Estacionalidad no encontrada para eliminar: {}", estacionalidadId);
                    return new EstacionalidadNotFoundException("Estacionalidad no encontrada con ID: " + estacionalidadId);
                });

        repositorio.delete(estacionalidad);
        log.info("Estacionalidad eliminada exitosamente: {}", estacionalidadId);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularFactorEstacionalPromedio(Integer productoId) {
        log.info("Calculando factor estacional promedio para producto: {}", productoId);

        productoRepositorio.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        List<EstacionalidadProducto> estacionalidades = repositorio.findByProductoId(productoId);

        if (estacionalidades.isEmpty()) {
            log.warn("No hay patrones estacionales para producto: {}", productoId);
            throw new IllegalArgumentException("El producto no tiene patrones estacionales registrados");
        }

        BigDecimal suma = BigDecimal.ZERO;
        for (EstacionalidadProducto est : estacionalidades) {
            suma = suma.add(est.getFactorEstacional());
        }

        BigDecimal promedio = suma.divide(BigDecimal.valueOf(estacionalidades.size()), 4, java.math.RoundingMode.HALF_UP);
        log.debug("Factor estacional promedio calculado: {} para producto: {}", promedio, productoId);

        return promedio;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularDemandaAjustadaPorEstacionalidad(Integer productoId, Integer mes, Integer demandaBase) {
        log.info("Calculando demanda ajustada para producto: {}, mes: {}, demanda base: {}", 
                productoId, mes, demandaBase);

        if (mes < 1 || mes > 12) {
            log.warn("Mes invalido para calculo: {}", mes);
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }

        if (demandaBase == null || demandaBase < 0) {
            log.warn("Demanda base invalida: {}", demandaBase);
            throw new IllegalArgumentException("La demanda base debe ser mayor o igual a 0");
        }

        EstacionalidadResponse estacionalidad = obtenerEstacionalidadPorProductoYMes(productoId, mes);
        
        BigDecimal demandaBD = BigDecimal.valueOf(demandaBase);
        BigDecimal demandaAjustada = demandaBD.multiply(estacionalidad.getFactorEstacional());
        
        Integer resultado = demandaAjustada.intValue();
        log.debug("Demanda ajustada calculada: {} (base: {}, factor: {})", 
                resultado, demandaBase, estacionalidad.getFactorEstacional());

        return resultado;
    }

    /**
     * Valida los datos del request de estacionalidad.
     *
     * @param request datos a validar
     * @throws EstacionalidadInvalidaException si los datos son inválidos
     */
    private void validarDatosRequest(EstacionalidadCreateRequest request) {
        if (request.getProductoId() == null || request.getProductoId() <= 0) {
            throw new EstacionalidadInvalidaException("El ID del producto debe ser valido");
        }

        if (request.getMes() == null || request.getMes() < 1 || request.getMes() > 12) {
            throw new EstacionalidadInvalidaException("El mes debe estar entre 1 y 12");
        }

        if (request.getFactorEstacional() == null || 
            request.getFactorEstacional().compareTo(BigDecimal.ZERO) <= 0) {
            throw new EstacionalidadInvalidaException("El factor estacional debe ser mayor a 0");
        }

        if (request.getDemandaMaxima() != null && request.getDemandaMinima() != null &&
            request.getDemandaMaxima() < request.getDemandaMinima()) {
            throw new EstacionalidadInvalidaException("La demanda maxima no puede ser menor a la minima");
        }
    }
}
