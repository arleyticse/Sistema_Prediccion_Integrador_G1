package com.prediccion.apppredicciongm.services;

import java.util.List;

import com.prediccion.apppredicciongm.models.Inventario.Categoria;

public interface ICategoriaServicio {
    List<Categoria> obtenerCategorias();

    void crearCategoria(Categoria categoria);

    void eliminarCategoria(Integer id);
}
