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

/**
 * Interfaz de servicio para gestión de inventario.
 * 
 * Define el contrato para todas las operaciones de inventario incluyendo CRUD,
 * búsquedas, alertas de stock, ajustes y resúmenes estadísticos del inventario
 * del sistema.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface IInventarioServicio {
    
    // ===== OPERACIONES CRUD BÁSICAS =====
    
    /**
     * Crea un nuevo registro de inventario para un producto.
     * 
     * @param request Datos del inventario a crear (producto, stock inicial, límites)
     * @return Respuesta con el inventario creado
     */
    InventarioResponse crearInventario(InventarioCreateRequest request);
    
    /**
     * Actualiza los parámetros de un inventario existente.
     * 
     * @param inventarioId ID del inventario a actualizar
     * @param request Datos actualizados (stock, límites, estado)
     * @return Respuesta con el inventario actualizado
     */
    InventarioResponse actualizarInventario(Integer inventarioId, InventarioUpdateRequest request);
    
    /**
     * Elimina un registro de inventario del sistema.
     * 
     * @param inventarioId ID del inventario a eliminar
     */
    void eliminarInventario(Integer inventarioId);
    
    /**
     * Obtiene un inventario específico por su ID.
     * 
     * @param inventarioId ID del inventario a obtener
     * @return Respuesta con los datos del inventario
     */
    InventarioResponse obtenerInventarioPorId(Integer inventarioId);
    
    /**
     * Lista todos los inventarios con paginación.
     * 
     * @param pagina Número de página (0-indexado)
     * @param tamanioPagina Cantidad de registros por página
     * @return Página de inventarios
     */
    Page<InventarioResponse> listarInventarios(int pagina, int tamanioPagina);
    
    // ===== BÚSQUEDAS ESPECÍFICAS =====
    
    /**
     * Busca el inventario de un producto específico.
     * 
     * @param productoId ID del producto
     * @return Respuesta con el inventario del producto
     */
    InventarioResponse buscarPorProducto(Integer productoId);
    
    /**
     * Busca todos los inventarios de productos en una categoría.
     * 
     * @param categoriaId ID de la categoría
     * @return Lista de inventarios de la categoría
     */
    List<InventarioResponse> buscarPorCategoria(Integer categoriaId);
    
    /**
     * Busca inventarios por estado (NORMAL, CRITICO, AGOTADO, etc.) con paginación.
     * 
     * @param estado Estado del inventario a filtrar
     * @param pagina Número de página
     * @param tamanioPagina Cantidad de registros por página
     * @return Página de inventarios con el estado especificado
     */
    Page<InventarioResponse> buscarPorEstado(EstadoInventario estado, int pagina, int tamanioPagina);
    
    /**
     * Busca inventarios por nombre de producto con paginación.
     * 
     * @param nombre Nombre o parte del nombre del producto
     * @param pagina Número de página
     * @param tamanioPagina Cantidad de registros por página
     * @return Página de inventarios cuyo producto coincide con el nombre
     */
    Page<InventarioResponse> buscarPorNombre(String nombre, int pagina, int tamanioPagina);
    
    /**
     * Busca inventarios dentro de un rango de stock específico.
     * 
     * @param minStock Stock mínimo del rango
     * @param maxStock Stock máximo del rango
     * @return Lista de inventarios con stock en el rango especificado
     */
    List<InventarioResponse> buscarPorRangoStock(Integer minStock, Integer maxStock);
    
    // ===== ALERTAS Y CONTROL DE STOCK =====
    
    /**
     * Obtiene todos los inventarios con stock bajo (por debajo del punto mínimo).
     * 
     * @return Lista de alertas de stock bajo
     */
    List<InventarioAlertaResponse> obtenerAlertasStockBajo();
    
    /**
     * Obtiene todos los inventarios en estado crítico.
     * 
     * Stock crítico se define como porcentaje muy bajo respecto al mínimo.
     * 
     * @return Lista de alertas críticas
     */
    List<InventarioAlertaResponse> obtenerAlertasCriticas();
    
    /**
     * Obtiene todos los inventarios agotados (sin stock disponible).
     * 
     * @return Lista de alertas de productos agotados
     */
    List<InventarioAlertaResponse> obtenerAlertasAgotados();
    
    /**
     * Obtiene inventarios sin movimiento durante un período específico.
     * 
     * Útil para identificar productos lentos o estancados.
     * 
     * @param diasSinMovimiento Número de días sin movimiento
     * @return Lista de alertas de inventario sin movimiento
     */
    List<InventarioAlertaResponse> obtenerAlertasSinMovimiento(Integer diasSinMovimiento);
    
    /**
     * Obtiene inventarios que exceden el stock máximo permitido.
     * 
     * @return Lista de inventarios sobre el máximo
     */
    List<InventarioResponse> obtenerInventariosSobreMaximo();
    
    // ===== OPERACIONES DE STOCK =====
    
    /**
     * Realiza un ajuste manual de stock (aumento o disminución).
     * 
     * @param request Datos del ajuste (motivo, cantidad, tipo)
     * @return Respuesta con el ajuste aplicado
     */
    InventarioResponse ajustarStock(AjusteStockRequest request);
    
    /**
     * Actualiza el stock del inventario basado en un movimiento (entrada/salida).
     * 
     * Se invoca automáticamente después de registrar un movimiento de kardex.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad del movimiento
     * @param esEntrada true para entrada de stock, false para salida
     */
    void actualizarStockDesdeMovimiento(Integer productoId, Integer cantidad, boolean esEntrada);
    
    /**
     * Actualiza la fecha del último movimiento del inventario.
     * 
     * @param inventarioId ID del inventario
     */
    void actualizarFechaUltimoMovimiento(Integer inventarioId);
    
    /**
     * Actualiza el contador de días sin venta/movimiento.
     * 
     * @param inventarioId ID del inventario
     * @param dias Número de días a registrar
     */
    void actualizarDiasSinVenta(Integer inventarioId, Integer dias);
    
    // ===== RESÚMENES Y ESTADÍSTICAS =====
    
    /**
     * Obtiene un resumen general del inventario del sistema.
     * 
     * Incluye totales de valor, cantidad de productos, alertas activas, etc.
     * 
     * @return Resumen con estadísticas generales del inventario
     */
    StockResumenResponse obtenerResumenGeneral();
    
    /**
     * Verifica si un inventario necesita ser reordenado.
     * 
     * Considera el punto de reorden y tendencias de demanda.
     * 
     * @param inventarioId ID del inventario a verificar
     * @return true si necesita reorden, false en caso contrario
     */
    boolean necesitaReorden(Integer inventarioId);
    
    /**
     * Verifica si un inventario está por debajo del punto mínimo.
     * 
     * @param inventarioId ID del inventario a verificar
     * @return true si está bajo el mínimo, false en caso contrario
     */
    boolean estaBajoPuntoMinimo(Integer inventarioId);
}
