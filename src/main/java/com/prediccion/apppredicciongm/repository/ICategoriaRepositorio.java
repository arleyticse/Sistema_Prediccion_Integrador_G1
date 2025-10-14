package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Categoria;

@Repository
public interface ICategoriaRepositorio extends JpaRepository<Categoria, Integer> {

}
