package com.prediccion.apppredicciongm.services;

import java.util.List;

import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;

public interface IUnidadeMedidaServicio {
    List<UnidadMedida> obtenerUnidadesMedida();
    void crearUnidadMedida(UnidadMedida unidadMedida);
    void eliminarUnidadMedida(Integer id);
    void actualizarUnidadMedida(Integer id, UnidadMedida unidadMedida);
}
