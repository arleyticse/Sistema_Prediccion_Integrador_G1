package com.prediccion.apppredicciongm.gestion_inventario.movimiento.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request.KardexCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.KardexResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.MovimientoResumenResponse;

public interface IKardexService {
    
    // CRUD básico
    KardexResponse registrarMovimiento(KardexCreateRequest request);
    KardexResponse obtenerMovimientoPorId(Long kardexId);
    Page<KardexResponse> listarMovimientos(int pagina, int tamanioPagina);
    void eliminarMovimiento(Long kardexId);
void restaurarMovimiento(Long kardexId) ;
    
    // Búsquedas por producto
    Page<KardexResponse> listarMovimientosPorProducto(Integer productoId, int pagina, int tamanioPagina);
    Page<KardexResponse> listarMovimientosPorProductoYFecha(Integer productoId, 
            LocalDateTime fechaInicio, LocalDateTime fechaFin, int pagina, int tamanioPagina);
    KardexResponse obtenerUltimoMovimientoProducto(Integer productoId);
    
    // Búsquedas por tipo de movimiento
    Page<KardexResponse> listarPorTipoMovimiento(TipoMovimiento tipoMovimiento, int pagina, int tamanioPagina);
    Page<KardexResponse> listarPorProductoYTipo(Integer productoId, TipoMovimiento tipoMovimiento, 
            int pagina, int tamanioPagina);
    
    // Búsquedas por fecha
    Page<KardexResponse> listarPorRangoFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin, 
            int pagina, int tamanioPagina);
    
    // Búsquedas por proveedor y usuario
    Page<KardexResponse> listarPorProveedor(Integer proveedorId, int pagina, int tamanioPagina);
    Page<KardexResponse> listarPorUsuario(Integer usuarioId, int pagina, int tamanioPagina);
    
    // Búsquedas específicas
    List<KardexResponse> buscarPorNumeroDocumento(String numeroDocumento);
    List<KardexResponse> buscarPorLote(String lote);
    List<KardexResponse> buscarPorVencimientoProximo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<KardexResponse> obtenerHistorialPreciosProducto(Integer productoId);
    
    // Resúmenes y estadísticas
    MovimientoResumenResponse obtenerResumenMovimientos();
    Integer calcularSaldoActualProducto(Integer productoId);
}
