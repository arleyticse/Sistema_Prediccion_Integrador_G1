package com.prediccion.apppredicciongm.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.models.Inventario.Categoria;
import com.prediccion.apppredicciongm.repository.ICategoriaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServicio implements ICategoriaServicio {

    private final ICategoriaRepositorio categoriaRepositorio;
    @Override
    public void crearCategoria(Categoria categoria) {
        categoriaRepositorio.save(categoria);
    }

    @Override
    public void eliminarCategoria(Integer id) {
        categoriaRepositorio.deleteById(id);
    }

    @Override
    public List<Categoria> obtenerCategorias() {
        return categoriaRepositorio.findAll();
    }

    @Override
    public void actualizarCategoria(Integer id, Categoria categoria) {
        Categoria categoriaExistente = categoriaRepositorio.findById(id).orElse(null);
        if (categoriaExistente != null) {
            categoriaExistente.setNombre(categoria.getNombre());
            categoriaRepositorio.save(categoriaExistente);  
        }
    }
}
