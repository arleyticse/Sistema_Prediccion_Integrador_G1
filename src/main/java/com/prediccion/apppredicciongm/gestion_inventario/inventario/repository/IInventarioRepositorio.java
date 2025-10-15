package com.prediccion.apppredicciongm.gestion_inventario.inventario.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Inventario;

import java.util.Optional;

@Repository
public interface IInventarioRepositorio extends JpaRepository<Inventario, Integer> {

    @Query("SELECT i FROM Inventario i WHERE i.producto.productoId = :productoId")
    Page<Inventario> findByProducto(Integer productoId, Pageable pageable);

    @Query("SELECT i FROM Inventario i WHERE i.producto.productoId = :productoId")
    Optional<Inventario> findByProducto(Integer productoId);
}
