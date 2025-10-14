package com.prediccion.apppredicciongm.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;
import com.prediccion.apppredicciongm.repository.IUnidadMedidaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadeMedidaServicio implements IUnidadeMedidaServicio {


    private final IUnidadMedidaRepositorio unidadMedidaRepositorio;
    @Override
    public void crearUnidadMedida(UnidadMedida unidadMedida) {
        unidadMedidaRepositorio.save(unidadMedida);
    }

    @Override
    public void eliminarUnidadMedida(Integer id) {
        unidadMedidaRepositorio.deleteById(id);
    }

    @Override
    public List<UnidadMedida> obtenerUnidadesMedida() {
        return unidadMedidaRepositorio.findAll();
    }
    
}
