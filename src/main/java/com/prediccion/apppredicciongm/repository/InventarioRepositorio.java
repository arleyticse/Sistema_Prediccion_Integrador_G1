package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Inventario;

@Repository
public interface InventarioRepositorio extends JpaRepository<Inventario, Integer> {
    
}
