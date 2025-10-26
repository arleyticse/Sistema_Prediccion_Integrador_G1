package com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.service;

import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.request.ParametroAlgoritmoCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.parametro_algoritmo.dto.response.ParametroAlgoritmoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interfaz de servicio para ParametroAlgoritmo
 */
public interface IParametroAlgoritmoServicio {

    /**
     * Crea un nuevo parámetro de algoritmo
     */
    ParametroAlgoritmoResponse crearParametro(ParametroAlgoritmoCreateRequest request);

    /**
     * Obtiene un parámetro por ID
     */
    ParametroAlgoritmoResponse obtenerParametroPorId(Integer parametroId);

    /**
     * Obtiene parámetros por tipo de algoritmo
     */
    List<ParametroAlgoritmoResponse> obtenerParametrosPorAlgoritmo(String tipoAlgoritmo);

    /**
     * Obtiene un parámetro específico por nombre y tipo
     */
    ParametroAlgoritmoResponse obtenerParametroEspecifico(String nombreParametro, String tipoAlgoritmo);

    /**
     * Lista todos los parámetros
     */
    Page<ParametroAlgoritmoResponse> listarTodosLosParametros(Pageable pageable);

    /**
     * Actualiza un parámetro
     */
    ParametroAlgoritmoResponse actualizarParametro(Integer parametroId, ParametroAlgoritmoCreateRequest request);

    /**
     * Valida un parámetro
     */
    Boolean validarParametro(ParametroAlgoritmoResponse parametro);

    /**
     * Elimina un parámetro
     */
    void eliminarParametro(Integer parametroId);
}
