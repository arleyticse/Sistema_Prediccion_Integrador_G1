package com.prediccion.apppredicciongm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.models.Inventario.Kardex;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface KardexRepositorio extends JpaRepository<Kardex, Integer> {

    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId")
    Optional<Kardex> findByProducto(Integer productoId);
    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId")
    Page<Kardex> findAllByProducto(Integer productoId, Pageable pageable);

    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId AND k.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    Page<Kardex> findAllByProductoAndFechaBetween(Integer productoId,
                                                  LocalDateTime fechaInicio,
                                                  LocalDateTime fechaFin,
                                                  Pageable pageable);
}
