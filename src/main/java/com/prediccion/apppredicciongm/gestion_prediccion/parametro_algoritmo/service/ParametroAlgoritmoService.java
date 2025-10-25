package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.service;

import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.request.ParametroAlgoritmoCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.response.ParametroAlgoritmoResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.errors.ParametroAlgoritmoNoEncontradoException;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.mapper.ParametroAlgoritmoMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.repository.IParametroAlgoritmoRepositorio;
import com.prediccion.apppredicciongm.models.ParametroAlgoritmo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de ParametroAlgoritmo
 * Parámetros configurables de algoritmos de predicción
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParametroAlgoritmoService implements IParametroAlgoritmoServicio {

    private final IParametroAlgoritmoRepositorio parametroRepositorio;
    private final ParametroAlgoritmoMapper mapper;

    private static final String PARAMETRO_NO_ENCONTRADO = "Parámetro de algoritmo no encontrado con ID: ";

    /**
     * Crea un nuevo parámetro de algoritmo
     */
    @Override
    public ParametroAlgoritmoResponse crearParametro(ParametroAlgoritmoCreateRequest request) {
        log.info("Creando parámetro - Nombre: {}, Algoritmo: {}", 
            request.getNombreParametro(), request.getTipoAlgoritmo());

        if (!validarValorParametro(request.getValorParametro(), request.getValorMinimo(), request.getValorMaximo())) {
            throw new IllegalArgumentException("El valor del parámetro está fuera del rango permitido");
        }

        ParametroAlgoritmo parametro = mapper.toEntity(request);
        parametro.setFechaCreacion(LocalDateTime.now());
        parametro.setFechaActualizacion(LocalDateTime.now());

        ParametroAlgoritmo guardado = parametroRepositorio.save(parametro);

        log.info("Parámetro creado con ID: {}", guardado.getParametroId());

        return mapper.toResponse(guardado);
    }

    /**
     * Obtiene un parámetro por ID
     */
    @Override
    @Transactional(readOnly = true)
    public ParametroAlgoritmoResponse obtenerParametroPorId(Integer parametroId) {
        log.debug("Obteniendo parámetro ID: {}", parametroId);

        ParametroAlgoritmo parametro = parametroRepositorio.findById(parametroId)
                .orElseThrow(() -> new ParametroAlgoritmoNoEncontradoException(
                    PARAMETRO_NO_ENCONTRADO + parametroId));

        return mapper.toResponse(parametro);
    }

    /**
     * Obtiene parámetros por tipo de algoritmo
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParametroAlgoritmoResponse> obtenerParametrosPorAlgoritmo(String tipoAlgoritmo) {
        log.debug("Obteniendo parámetros para algoritmo: {}", tipoAlgoritmo);

        List<ParametroAlgoritmo> parametros = parametroRepositorio.findByTipoAlgoritmo(tipoAlgoritmo);

        return parametros.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un parámetro específico por nombre y tipo
     */
    @Override
    @Transactional(readOnly = true)
    public ParametroAlgoritmoResponse obtenerParametroEspecifico(String nombreParametro, String tipoAlgoritmo) {
        log.debug("Obteniendo parámetro - Nombre: {}, Algoritmo: {}", nombreParametro, tipoAlgoritmo);

        ParametroAlgoritmo parametro = parametroRepositorio.findByNombreParametroAndTipoAlgoritmo(nombreParametro, tipoAlgoritmo)
                .orElseThrow(() -> new ParametroAlgoritmoNoEncontradoException(
                    "Parámetro no encontrado: " + nombreParametro + " para " + tipoAlgoritmo));

        return mapper.toResponse(parametro);
    }

    /**
     * Lista todos los parámetros
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ParametroAlgoritmoResponse> listarTodosLosParametros(Pageable pageable) {
        log.debug("Listando todos los parámetros");

        return parametroRepositorio.findAll(pageable)
                .map(mapper::toResponse);
    }

    /**
     * Actualiza un parámetro
     */
    @Override
    public ParametroAlgoritmoResponse actualizarParametro(Integer parametroId, ParametroAlgoritmoCreateRequest request) {
        log.info("Actualizando parámetro ID: {}", parametroId);

        ParametroAlgoritmo parametro = parametroRepositorio.findById(parametroId)
                .orElseThrow(() -> new ParametroAlgoritmoNoEncontradoException(
                    PARAMETRO_NO_ENCONTRADO + parametroId));

        if (!validarValorParametro(request.getValorParametro(), request.getValorMinimo(), request.getValorMaximo())) {
            throw new IllegalArgumentException("El valor del parámetro está fuera del rango permitido");
        }

        parametro.setNombreParametro(request.getNombreParametro());
        parametro.setValorParametro(request.getValorParametro());
        parametro.setTipoAlgoritmo(request.getTipoAlgoritmo());
        parametro.setDescripcion(request.getDescripcion());
        parametro.setValorMinimo(request.getValorMinimo());
        parametro.setValorMaximo(request.getValorMaximo());
        parametro.setActivo(request.getActivo() != null ? request.getActivo() : true);
        parametro.setFechaActualizacion(LocalDateTime.now());

        ParametroAlgoritmo actualizado = parametroRepositorio.save(parametro);

        log.info("Parámetro actualizado correctamente");

        return mapper.toResponse(actualizado);
    }

    /**
     * Valida un parámetro
     */
    @Override
    public Boolean validarParametro(ParametroAlgoritmoResponse parametro) {
        log.debug("Validando parámetro: {}", parametro.getNombreParametro());

        if (parametro.getValorParametro() == null) {
            log.warn("Valor de parámetro nulo");
            return false;
        }

        return validarValorParametro(parametro.getValorParametro(), 
            parametro.getValorMinimo(), parametro.getValorMaximo());
    }

    /**
     * Elimina un parámetro
     */
    @Override
    public void eliminarParametro(Integer parametroId) {
        log.info("Eliminando parámetro ID: {}", parametroId);

        if (!parametroRepositorio.existsById(parametroId)) {
            throw new ParametroAlgoritmoNoEncontradoException(
                PARAMETRO_NO_ENCONTRADO + parametroId);
        }

        parametroRepositorio.deleteById(parametroId);
        log.info("Parámetro eliminado correctamente");
    }

    /**
     * Valida que el valor esté dentro del rango permitido
     */
    private Boolean validarValorParametro(java.math.BigDecimal valor, 
                                        java.math.BigDecimal minimo, 
                                        java.math.BigDecimal maximo) {
        if (minimo != null && valor.compareTo(minimo) < 0) {
            return false;
        }
        if (maximo != null && valor.compareTo(maximo) > 0) {
            return false;
        }
        return true;
    }
}
