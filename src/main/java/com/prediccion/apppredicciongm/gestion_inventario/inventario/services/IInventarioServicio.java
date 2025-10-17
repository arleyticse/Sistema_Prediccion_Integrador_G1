package com.prediccion.apppredicciongm.gestion_inventario.inventario.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.AjusteStockRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.request.InventarioUpdateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioAlertaResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.InventarioResponse;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response.StockResumenResponse;

public interface IInventarioServicio {
    
    // CRUD básico
    InventarioResponse crearInventario(InventarioCreateRequest request);
    InventarioResponse actualizarInventario(Integer inventarioId, InventarioUpdateRequest request);
    void eliminarInventario(Integer inventarioId);
    InventarioResponse obtenerInventarioPorId(Integer inventarioId);
    Page<InventarioResponse> listarInventarios(int pagina, int tamanioPagina);
    
    // Búsquedas específicas
    InventarioResponse buscarPorProducto(Integer productoId);
    List<InventarioResponse> buscarPorCategoria(Integer categoriaId);
    Page<InventarioResponse> buscarPorEstado(EstadoInventario estado, int pagina, int tamanioPagina);
    Page<InventarioResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina);
    List<InventarioResponse> buscarPorRangoStock(Integer minStock, Integer maxStock);
    
    // Alertas y control de stock
    List<InventarioAlertaResponse> obtenerAlertasStockBajo();
    List<InventarioAlertaResponse> obtenerAlertasCriticas();
    List<InventarioAlertaResponse> obtenerAlertasAgotados();
    List<InventarioAlertaResponse> obtenerAlertasSinMovimiento(Integer diasSinMovimiento);
    List<InventarioResponse> obtenerInventariosSobreMaximo();
    
    // Operaciones de stock
    InventarioResponse ajustarStock(AjusteStockRequest request);
    void actualizarStockDesdeMovimiento(Integer productoId, Integer cantidad, boolean esEntrada);
    void actualizarFechaUltimoMovimiento(Integer inventarioId);
    void actualizarDiasSinVenta(Integer inventarioId, Integer dias);
    
    // Resúmenes y estadísticas
    StockResumenResponse obtenerResumenGeneral();
    boolean necesitaReorden(Integer inventarioId);
    boolean estaBajoPuntoMinimo(Integer inventarioId);
}
