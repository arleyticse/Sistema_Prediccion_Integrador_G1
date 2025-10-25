package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.repository;

import com.prediccion.apppredicciongm.models.CalculoObtimizacion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para acceso a datos de CalculoObtimizacion
 * Proporciona métodos de consulta para cálculos de optimización de inventario
 */
@Repository
public interface ICalculoObtimizacionRepositorio extends JpaRepository<CalculoObtimizacion, Integer> {

    /**
     * Busca el último cálculo de optimización de un producto
     * @param producto el producto para el cual buscar el cálculo
     * @return Optional con el cálculo más reciente si existe
     */
    Optional<CalculoObtimizacion> findFirstByProductoOrderByFechaCalculoDesc(Producto producto);

    /**
     * Lista todos los cálculos de un producto
     * @param producto el producto para el cual listar cálculos
     * @param pageable información de paginación
     * @return página de cálculos del producto
     */
    Page<CalculoObtimizacion> findByProductoOrderByFechaCalculoDesc(Producto producto, Pageable pageable);

    /**
     * Busca todos los cálculos paginados
     * @param pageable información de paginación
     * @return página de todos los cálculos
     */
    @NonNull
    Page<CalculoObtimizacion> findAll(@NonNull Pageable pageable);
}
