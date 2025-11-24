package com.prediccion.apppredicciongm.gestion_inventario.movimiento.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.services.IInventarioServicio;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request.KardexCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.KardexResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.MovimientoResumenResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.mapper.KardexMapper;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de implementación para registro y gestión del Kardex (movimientos de inventario).
 * 
 * Mantiene el historial completo de movimientos (entrada, salida, ajuste) con soporte para
 * anulación reversible. Proporciona trazabilidad por producto, fecha, tipo, proveedor y usuario.
 * Revierte automáticamente el impacto en stock al anular movimientos.
 * 
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class KardexServicioImpl implements IKardexService {

    private static final Logger log = LoggerFactory.getLogger(KardexServicioImpl.class);

    private final IKardexRepositorio kardexRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IInventarioServicio inventarioServicio;
    private final KardexMapper kardexMapper;
    private final IProveedorRepositorio proveedorRepositorio;
    @Override
    @Transactional
    public KardexResponse registrarMovimiento(KardexCreateRequest request) {
        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + request.getProductoId()));

        Integer saldoAnterior = calcularSaldoActualProducto(request.getProductoId());

        Kardex kardex = kardexMapper.toEntity(request);
        kardex.setProducto(producto);

        Proveedor proveedor = proveedorRepositorio.findById(request.getProveedorId()).orElseThrow(
                () -> new IllegalArgumentException(
                        "Proveedor no encontrado con ID: " + request.getProveedorId())
        );

        Integer nuevoSaldo;
        if (request.getTipoMovimiento().esEntrada()) {
            nuevoSaldo = saldoAnterior + request.getCantidad();
        } else {
            nuevoSaldo = saldoAnterior - request.getCantidad();
            if (nuevoSaldo < 0) {
                throw new IllegalArgumentException(
                        "Stock insuficiente. Saldo actual: " + saldoAnterior +
                                ", Cantidad solicitada: " + request.getCantidad());
            }
        }
        kardex.setProveedor(proveedor);
        kardex.setSaldoCantidad(nuevoSaldo);

        Kardex kardexGuardado = kardexRepositorio.save(kardex);

        inventarioServicio.actualizarStockDesdeMovimiento(
                request.getProductoId(),
                request.getCantidad(),
                request.getTipoMovimiento().esEntrada());

        return kardexMapper.toResponse(kardexGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public KardexResponse obtenerMovimientoPorId(Long kardexId) {
        Kardex kardex = kardexRepositorio.findById(kardexId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Movimiento no encontrado con ID: " + kardexId));
        return kardexMapper.toResponse(kardex);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarMovimientos(int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    /**
     * Anula un movimiento de kardex y revierte automáticamente su impacto en el stock del inventario.
     * 
     * El proceso es reversible: la reversión se registra para mantener trazabilidad.
     * Para movimientos de entrada: reduce el stock (cantidad negativa)
     * Para movimientos de salida: aumenta el stock (cantidad positiva)
     * Para ajustes: revierte según el signo original
     * 
     * @param kardexId Identificador único del movimiento a anular
     * @throws IllegalArgumentException Si el movimiento no existe o ya está anulado
     * @see IInventarioServicio#actualizarStockDesdeMovimiento(Integer, Integer, boolean)
     */
    @Override
    @Transactional
    public void eliminarMovimiento(Long kardexId) {
        log.info("Iniciando anulación del movimiento kardex con ID: {}", kardexId);
        
        Kardex kardex = kardexRepositorio.findById(kardexId)
                .orElseThrow(() -> {
                    log.error("Movimiento no encontrado con ID: {}", kardexId);
                    return new IllegalArgumentException("Movimiento no encontrado con ID: " + kardexId);
                });
        
        if (kardex.isAnulado()) {
            log.warn("Intento de anular movimiento ya anulado. Kardex ID: {}, Producto: {}", 
                    kardexId, kardex.getProducto().getNombre());
            throw new IllegalArgumentException("El movimiento ya está anulado");
        }
        
        try {
            // Obtener datos del movimiento original
            Integer productoId = kardex.getProducto().getProductoId();
            Integer cantidadOriginal = kardex.getCantidad();
            TipoMovimiento tipoMovimiento = kardex.getTipoMovimiento();
            
            log.debug("Reversión - Producto: {}, Cantidad: {}, Tipo: {}", 
                    productoId, cantidadOriginal, tipoMovimiento.getDescripcion());
            
            // Calcular la reversión: negar el impacto del movimiento
            // ENTRADA (+) → reversión es SALIDA (-)
            // SALIDA (-) → reversión es ENTRADA (+)
            // AJUSTE → se revierte el signo
            boolean esEntradaOriginal = tipoMovimiento.esEntrada();
            
            // Revertir el impacto en inventario (cantidad con signo opuesto)
            inventarioServicio.actualizarStockDesdeMovimiento(
                    productoId, 
                    cantidadOriginal, 
                    !esEntradaOriginal  // Invertir el tipo de movimiento
            );
            
            // Marcar como anulado
            kardex.setAnulado(true);
            kardexRepositorio.save(kardex);
            
            log.info("Movimiento anulado exitosamente - Kardex ID: {}, Producto ID: {}, " +
                    "Cantidad reversada: {}, Tipo Original: {}", 
                    kardexId, productoId, cantidadOriginal, tipoMovimiento.getDescripcion());
            
        } catch (Exception e) {
            log.error("Error al anular movimiento kardex ID: {}. Detalle: {}", 
                    kardexId, e.getMessage(), e);
            throw new RuntimeException("Error al anular el movimiento: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarMovimientosPorProducto(Integer productoId, int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findAllByProducto(productoId,
                PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarMovimientosPorProductoYFecha(Integer productoId,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findAllByProductoAndFechaBetween(
                productoId, fechaInicio, fechaFin, PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public KardexResponse obtenerUltimoMovimientoProducto(Integer productoId) {
        Kardex kardex = kardexRepositorio.findUltimoMovimientoByProducto(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontraron movimientos para el producto con ID: " + productoId));
        return kardexMapper.toResponse(kardex);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarPorTipoMovimiento(TipoMovimiento tipoMovimiento, int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findByTipoMovimiento(tipoMovimiento,
                PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarPorProductoYTipo(Integer productoId, TipoMovimiento tipoMovimiento,
            int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findByProductoAndTipo(productoId, tipoMovimiento,
                PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarPorRangoFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin,
            int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findByFechaBetween(fechaInicio, fechaFin,
                PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarPorProveedor(Integer proveedorId, int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findByProveedor(proveedorId,
                PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexResponse> listarPorUsuario(Integer usuarioId, int pagina, int tamanioPagina) {
        Page<Kardex> movimientos = kardexRepositorio.findByUsuario(usuarioId,
                PageRequest.of(pagina, tamanioPagina));
        return movimientos.map(kardexMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponse> buscarPorNumeroDocumento(String numeroDocumento) {
        List<Kardex> movimientos = kardexRepositorio.findByNumeroDocumento(numeroDocumento);
        return movimientos.stream()
                .map(kardexMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponse> buscarPorLote(String lote) {
        List<Kardex> movimientos = kardexRepositorio.findByLote(lote);
        return movimientos.stream()
                .map(kardexMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponse> buscarPorVencimientoProximo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Kardex> movimientos = kardexRepositorio.findByFechaVencimientoProxima(fechaInicio, fechaFin);
        return movimientos.stream()
                .map(kardexMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponse> obtenerHistorialPreciosProducto(Integer productoId) {
        List<Kardex> movimientos = kardexRepositorio.findHistorialPreciosByProducto(productoId);
        return movimientos.stream()
                .map(kardexMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResumenResponse obtenerResumenMovimientos() {
        Long totalMovimientos = kardexRepositorio.countTotalMovimientos();
        Long totalEntradas = kardexRepositorio.countEntradas();
        Long totalSalidas = kardexRepositorio.countSalidas();
        Long totalAjustes = kardexRepositorio.countAjustes();
        Long cantidadEntrada = kardexRepositorio.sumCantidadEntradas();
        Long cantidadSalida = kardexRepositorio.sumCantidadSalidas();
        LocalDateTime fechaUltimo = kardexRepositorio.findFechaUltimoMovimiento().orElse(null);
        String productoMasMovido = kardexRepositorio.findProductoMasMovido().orElse("N/A");

        return MovimientoResumenResponse.builder()
                .totalMovimientos(totalMovimientos.intValue())
                .totalEntradas(totalEntradas.intValue())
                .totalSalidas(totalSalidas.intValue())
                .totalAjustes(totalAjustes.intValue())
                .cantidadTotalEntrada(cantidadEntrada.intValue())
                .cantidadTotalSalida(cantidadSalida.intValue())
                .fechaUltimoMovimiento(fechaUltimo)
                .productoMasMovido(productoMasMovido)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calcularSaldoActualProducto(Integer productoId) {
        return kardexRepositorio.findUltimoMovimientoByProducto(productoId)
                .map(Kardex::getSaldoCantidad)
                .orElse(0);
    }

    /**
     * Restaura (deshace la anulación) de un movimiento de kardex y re-aplica su impacto en el stock.
     * 
     * Operación inversa a {@link #eliminarMovimiento(Long)}. Requiere que el movimiento
     * esté previamente anulado. Re-aplica el impacto original en el inventario.
     * 
     * @param kardexId Identificador único del movimiento a restaurar
     * @throws IllegalArgumentException Si el movimiento no existe o no está anulado
     * @see IInventarioServicio#actualizarStockDesdeMovimiento(Integer, Integer, boolean)
     */
    @Override
    @Transactional
    public void restaurarMovimiento(Long kardexId) {
        log.info("Iniciando restauración del movimiento kardex con ID: {}", kardexId);
        
        Kardex kardex = kardexRepositorio.findById(kardexId)
                .orElseThrow(() -> {
                    log.error("Movimiento no encontrado con ID: {}", kardexId);
                    return new IllegalArgumentException("Movimiento no encontrado con ID: " + kardexId);
                });
        
        if (!kardex.isAnulado()) {
            log.warn("Intento de restaurar movimiento no anulado. Kardex ID: {}, Producto: {}", 
                    kardexId, kardex.getProducto().getNombre());
            throw new IllegalArgumentException("El movimiento no está anulado y no puede restaurarse");
        }
        
        try {
            // Obtener datos del movimiento original
            Integer productoId = kardex.getProducto().getProductoId();
            Integer cantidadOriginal = kardex.getCantidad();
            TipoMovimiento tipoMovimiento = kardex.getTipoMovimiento();
            
            log.debug("Re-aplicación - Producto: {}, Cantidad: {}, Tipo: {}", 
                    productoId, cantidadOriginal, tipoMovimiento.getDescripcion());
            
            // Re-aplicar el impacto original del movimiento
            boolean esEntrada = tipoMovimiento.esEntrada();
            
            // Re-aplicar en inventario (con el tipo original)
            inventarioServicio.actualizarStockDesdeMovimiento(
                    productoId, 
                    cantidadOriginal, 
                    esEntrada
            );
            
            // Marcar como restaurado (no anulado)
            kardex.setAnulado(false);
            kardexRepositorio.save(kardex);
            
            log.info("Movimiento restaurado exitosamente - Kardex ID: {}, Producto ID: {}, " +
                    "Cantidad re-aplicada: {}, Tipo Original: {}", 
                    kardexId, productoId, cantidadOriginal, tipoMovimiento.getDescripcion());
            
        } catch (Exception e) {
            log.error("Error al restaurar movimiento kardex ID: {}. Detalle: {}", 
                    kardexId, e.getMessage(), e);
            throw new RuntimeException("Error al restaurar el movimiento: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KardexResponse> obtenerUltimosMovimientos(int limit) {
        log.info("Obteniendo los últimos {} movimientos para dashboard", limit);
        
        try {
            PageRequest pageRequest = PageRequest.of(0, limit);
            Page<Kardex> movimientos = kardexRepositorio.findAllByOrderByFechaMovimientoDesc(pageRequest);
            
            List<KardexResponse> response = movimientos.getContent().stream()
                    .map(kardexMapper::toResponse)
                    .collect(Collectors.toList());
            
            log.debug("Se obtuvieron {} movimientos recientes", response.size());
            return response;
            
        } catch (Exception e) {
            log.error("Error al obtener últimos movimientos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener los últimos movimientos: " + e.getMessage(), e);
        }
    }
}
