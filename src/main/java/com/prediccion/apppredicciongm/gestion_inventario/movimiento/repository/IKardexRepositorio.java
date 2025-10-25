package com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IKardexRepositorio extends JpaRepository<Kardex, Long> {

    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId")
    Optional<Kardex> findByProducto(@Param("productoId") Integer productoId);

    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findAllByProducto(@Param("productoId") Integer productoId, Pageable pageable);

    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId AND k.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findAllByProductoAndFechaBetween(@Param("productoId") Integer productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // Obtener último saldo de un producto
    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId ORDER BY k.fechaMovimiento DESC, k.kardexId DESC LIMIT 1")
    Optional<Kardex> findUltimoMovimientoByProducto(@Param("productoId") Integer productoId);

    // Buscar por tipo de movimiento
    @Query("SELECT k FROM Kardex k WHERE k.tipoMovimiento = :tipoMovimiento ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findByTipoMovimiento(@Param("tipoMovimiento") TipoMovimiento tipoMovimiento, Pageable pageable);

    // Buscar por rango de fechas
    @Query("SELECT k FROM Kardex k WHERE k.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findByFechaBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // Buscar por proveedor
    @Query("SELECT k FROM Kardex k WHERE k.proveedor.proveedorId = :proveedorId ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findByProveedor(@Param("proveedorId") Integer proveedorId, Pageable pageable);

    // Buscar por usuario que registró
    @Query("SELECT k FROM Kardex k WHERE k.usuario.usuarioId = :usuarioId ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findByUsuario(@Param("usuarioId") Integer usuarioId, Pageable pageable);

    // Buscar por número de documento
    @Query("SELECT k FROM Kardex k WHERE k.numeroDocumento = :numeroDocumento")
    List<Kardex> findByNumeroDocumento(@Param("numeroDocumento") String numeroDocumento);

    // Contar total de movimientos
    @Query("SELECT COUNT(k) FROM Kardex k")
    Long countTotalMovimientos();

    // Contar entradas
    @Query("SELECT COUNT(k) FROM Kardex k WHERE k.tipoMovimiento IN ('COMPRA', 'DEVOLUCION_CLIENTE', 'AJUSTE_ENTRADA', 'PRODUCCION')")
    Long countEntradas();

    // Contar salidas
    @Query("SELECT COUNT(k) FROM Kardex k WHERE k.tipoMovimiento IN ('VENTA', 'DEVOLUCION_PROVEEDOR', 'AJUSTE_SALIDA', 'CONSUMO', 'MERMA')")
    Long countSalidas();

    // Contar ajustes
    @Query("SELECT COUNT(k) FROM Kardex k WHERE k.tipoMovimiento IN ('AJUSTE_ENTRADA', 'AJUSTE_SALIDA')")
    Long countAjustes();

    // Sumar cantidad total de entradas
    @Query("SELECT COALESCE(SUM(k.cantidad), 0) FROM Kardex k WHERE k.tipoMovimiento IN ('COMPRA', 'DEVOLUCION_CLIENTE', 'AJUSTE_ENTRADA', 'PRODUCCION')")
    Long sumCantidadEntradas();

    // Sumar cantidad total de salidas
    @Query("SELECT COALESCE(SUM(k.cantidad), 0) FROM Kardex k WHERE k.tipoMovimiento IN ('VENTA', 'DEVOLUCION_PROVEEDOR', 'AJUSTE_SALIDA', 'CONSUMO', 'MERMA')")
    Long sumCantidadSalidas();

    // Obtener fecha del último movimiento
    @Query("SELECT MAX(k.fechaMovimiento) FROM Kardex k")
    Optional<LocalDateTime> findFechaUltimoMovimiento();

    // Obtener producto más movido (por cantidad de registros)
    @Query("SELECT p.nombre FROM Kardex k JOIN k.producto p GROUP BY p.nombre ORDER BY COUNT(k) DESC LIMIT 1")
    Optional<String> findProductoMasMovido();

    // Buscar movimientos por lote
    @Query("SELECT k FROM Kardex k WHERE k.lote = :lote ORDER BY k.fechaMovimiento DESC")
    List<Kardex> findByLote(@Param("lote") String lote);

    // Buscar movimientos con fecha de vencimiento próxima
    @Query("SELECT k FROM Kardex k WHERE k.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY k.fechaVencimiento ASC")
    List<Kardex> findByFechaVencimientoProxima(@Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);

    // Obtener movimientos por producto y tipo
    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId AND k.tipoMovimiento = :tipoMovimiento ORDER BY k.fechaMovimiento DESC")
    Page<Kardex> findByProductoAndTipo(@Param("productoId") Integer productoId,
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            Pageable pageable);

    // Obtener historial de precios de un producto
    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId AND k.costoUnitario IS NOT NULL ORDER BY k.fechaMovimiento DESC")
    List<Kardex> findHistorialPreciosByProducto(@Param("productoId") Integer productoId);
}
