package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Integer> {

}
