package com.prediccion.apppredicciongm.services;

import java.util.List;

import com.prediccion.apppredicciongm.models.Inventario.Inventario;

public interface IInventarioServicio {
    void registrarInventario(Inventario inventario);
    Inventario obtenerInventarioPorId(Integer id);
    List<Inventario> listarInventarios();
    void actualizarInventario(Inventario inventario);
}
