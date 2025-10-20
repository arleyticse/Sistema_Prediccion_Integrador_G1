package com.prediccion.apppredicciongm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Proveedor;

@Repository
public interface IProveedorRepositorio extends JpaRepository<Proveedor, Integer> {
    
}
