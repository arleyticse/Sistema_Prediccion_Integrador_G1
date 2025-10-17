package com.prediccion.apppredicciongm.gestion_inventario.movimiento.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexServicioImpl implements IKardexService {

    private final IKardexRepositorio kardexRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final IInventarioServicio inventarioServicio;
    private final KardexMapper kardexMapper;

    @Override
    @Transactional
    public KardexResponse registrarMovimiento(KardexCreateRequest request) {
        // Validar que el producto existe
        Producto producto = productoRepositorio.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + request.getProductoId()));
        
        // Obtener saldo actual
        Integer saldoAnterior = calcularSaldoActualProducto(request.getProductoId());
        
        // Crear el movimiento
        Kardex kardex = kardexMapper.toEntity(request);
        kardex.setProducto(producto);
        
        // TODO: Establecer relaciones opcionales cuando se implementen los repositorios
        // Las relaciones de Proveedor y Usuario se establecerán automáticamente
        // mediante los IDs en el mapper
        
        // Calcular nuevo saldo
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
        
        kardex.setSaldoCantidad(nuevoSaldo);
        
        // Guardar el movimiento
        Kardex kardexGuardado = kardexRepositorio.save(kardex);
        
        // Actualizar el inventario
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

    @Override
    @Transactional
    public void eliminarMovimiento(Long kardexId) {
        if (!kardexRepositorio.existsById(kardexId)) {
            throw new IllegalArgumentException("Movimiento no encontrado con ID: " + kardexId);
        }
        // Nota: En un sistema real, probablemente no querrías eliminar movimientos,
        // sino marcarlos como anulados para mantener la trazabilidad
        kardexRepositorio.deleteById(kardexId);
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
}
