package com.prediccion.apppredicciongm.gestion_inventario.inventario.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;

import java.util.List;
import java.util.Optional;

@Repository
public interface IInventarioRepositorio extends JpaRepository<Inventario, Integer> {

    @Query("SELECT i FROM Inventario i WHERE i.producto.productoId = :productoId")
    Page<Inventario> findByProducto(@Param("productoId") Integer productoId, Pageable pageable);

    @Query("SELECT i FROM Inventario i WHERE i.producto.productoId = :productoId")
    Optional<Inventario> findByProducto(@Param("productoId") Integer productoId);
    
    // Buscar inventarios con stock bajo (por debajo del punto de reorden)
    @Query("SELECT i FROM Inventario i WHERE i.stockDisponible <= i.puntoReorden AND i.estado = 'ACTIVO'")
    List<Inventario> findInventariosBajoStockReorden();
    
    // Buscar inventarios críticos (por debajo del stock mínimo)
    @Query("SELECT i FROM Inventario i WHERE i.stockDisponible < i.stockMinimo AND i.estado = 'ACTIVO'")
    List<Inventario> findInventariosCriticos();
    
    // Buscar inventarios agotados
    @Query("SELECT i FROM Inventario i WHERE i.stockDisponible = 0 AND i.estado = 'ACTIVO'")
    List<Inventario> findInventariosAgotados();
    
    // Buscar inventarios sin movimiento
    @Query("SELECT i FROM Inventario i WHERE i.diasSinVenta > :dias AND i.estado = 'ACTIVO'")
    List<Inventario> findInventariosSinMovimiento(@Param("dias") Integer dias);
    
    // Buscar por categoría
    @Query("SELECT i FROM Inventario i WHERE i.producto.categoria.categoriaId = :categoriaId")
    List<Inventario> findByCategoria(@Param("categoriaId") Integer categoriaId);
    
    // Buscar por estado
    @Query("SELECT i FROM Inventario i WHERE i.estado = :estado")
    Page<Inventario> findByEstado(@Param("estado") EstadoInventario estado, Pageable pageable);
    
    // Buscar por rango de stock
    @Query("SELECT i FROM Inventario i WHERE i.stockDisponible BETWEEN :minStock AND :maxStock")
    List<Inventario> findByRangoStock(@Param("minStock") Integer minStock, @Param("maxStock") Integer maxStock);
    
    // Contar productos activos
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.estado = 'ACTIVO'")
    Long countProductosActivos();
    
    // Contar productos inactivos
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.estado = 'INACTIVO'")
    Long countProductosInactivos();
    
    // Contar productos con stock bajo
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.stockDisponible <= i.puntoReorden AND i.estado = 'ACTIVO'")
    Long countProductosStockBajo();
    
    // Contar productos agotados
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.stockDisponible = 0 AND i.estado = 'ACTIVO'")
    Long countProductosAgotados();
    
    // Contar productos sin movimiento
    @Query("SELECT COUNT(i) FROM Inventario i WHERE i.diasSinVenta > :dias AND i.estado = 'ACTIVO'")
    Long countProductosSinMovimiento(@Param("dias") Integer dias);
    
    // Calcular valor total del inventario
    @Query("SELECT SUM(i.stockDisponible * p.costoAdquisicion) FROM Inventario i JOIN i.producto p WHERE i.estado = 'ACTIVO'")
    Double calcularValorTotalInventario();
    
    // Sumar stock total disponible
    @Query("SELECT SUM(i.stockDisponible) FROM Inventario i WHERE i.estado = 'ACTIVO'")
    Long sumStockTotalDisponible();
    
    // Buscar por nombre de producto (búsqueda parcial)
    @Query("SELECT i FROM Inventario i WHERE LOWER(i.producto.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Inventario> findByNombreProductoContaining(@Param("nombre") String nombre, Pageable pageable);
    
    // Buscar inventarios con stock sobre el máximo
    @Query("SELECT i FROM Inventario i WHERE i.stockDisponible > i.stockMaximo AND i.stockMaximo IS NOT NULL AND i.estado = 'ACTIVO'")
    List<Inventario> findInventariosSobreStockMaximo();
}
