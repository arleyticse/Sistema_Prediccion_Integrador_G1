package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Kardex;

@Repository
public interface KardexRepositorio extends JpaRepository<Kardex, Integer> {
    
}
