package com.prediccion.apppredicciongm.gestion_inventario.movimiento.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.request.KardexCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.KardexResponse;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.dto.response.MovimientoResumenResponse;

/**
 * Interfaz de servicio para gestión del Kardex (Movimiento de Inventario).
 * 
 * Define el contrato para registro, búsqueda y análisis de todos los
 * movimientos
 * de inventario (entradas, salidas, ajustes, devoluciones). Proporciona
 * trazabilidad
 * completa del historial de inventario con búsquedas por producto, fecha, tipo,
 * proveedor y usuario.
 * 
 * @version 1.0
 * @since 1.0
 */
public interface IKardexService {

        // ===== OPERACIONES CRUD BÁSICAS =====

        /**
         * Registra un nuevo movimiento en el Kardex.
         * 
         * Crea un registro de movimiento de inventario (entrada, salida, ajuste).
         * Actualiza automáticamente el stock del inventario asociado.
         * 
         * @param request Datos del movimiento a registrar
         * @return Respuesta con el movimiento registrado
         */
        KardexResponse registrarMovimiento(KardexCreateRequest request);

        /**
         * Obtiene un movimiento específico por su ID.
         * 
         * @param kardexId ID del movimiento a obtener
         * @return Respuesta con los datos del movimiento
         */
        KardexResponse obtenerMovimientoPorId(Long kardexId);

        /**
         * Lista todos los movimientos con paginación.
         * 
         * @param pagina        Número de página (0-indexado)
         * @param tamanioPagina Cantidad de movimientos por página
         * @return Página de movimientos
         */
        Page<KardexResponse> listarMovimientos(int pagina, int tamanioPagina);

        /**
         * Elimina un movimiento del historial (operación reversible).
         * 
         * @param kardexId ID del movimiento a eliminar
         */
        void eliminarMovimiento(Long kardexId);

        /**
         * Restaura un movimiento previamente eliminado.
         * 
         * @param kardexId ID del movimiento a restaurar
         */
        void restaurarMovimiento(Long kardexId);

        // ===== BÚSQUEDAS POR PRODUCTO =====

        /**
         * Lista todos los movimientos de un producto específico con paginación.
         * 
         * @param productoId    ID del producto
         * @param pagina        Número de página
         * @param tamanioPagina Cantidad de movimientos por página
         * @return Página de movimientos del producto
         */
        Page<KardexResponse> listarMovimientosPorProducto(Integer productoId, int pagina, int tamanioPagina);

        /**
         * Lista movimientos de un producto en un rango de fechas específico.
         * 
         * @param productoId    ID del producto
         * @param fechaInicio   Fecha inicial (incluida)
         * @param fechaFin      Fecha final (incluida)
         * @param pagina        Número de página
         * @param tamanioPagina Cantidad de movimientos por página
         * @return Página de movimientos del producto en el rango de fechas
         */
        Page<KardexResponse> listarMovimientosPorProductoYFecha(Integer productoId,
                        LocalDateTime fechaInicio, LocalDateTime fechaFin, int pagina, int tamanioPagina);

        /**
         * Obtiene el último movimiento registrado para un producto.
         * 
         * @param productoId ID del producto
         * @return Respuesta con el último movimiento
         */
        KardexResponse obtenerUltimoMovimientoProducto(Integer productoId);

        // ===== BÚSQUEDAS POR TIPO DE MOVIMIENTO =====

        /**
         * Lista movimientos filtrados por tipo (ENTRADA, SALIDA, AJUSTE, etc.) con
         * paginación.
         * 
         * @param tipoMovimiento Tipo de movimiento a filtrar
         * @param pagina         Número de página
         * @param tamanioPagina  Cantidad de movimientos por página
         * @return Página de movimientos del tipo especificado
         */
        Page<KardexResponse> listarPorTipoMovimiento(TipoMovimiento tipoMovimiento, int pagina, int tamanioPagina);

        /**
         * Lista movimientos de un producto filtrados por tipo específico.
         * 
         * @param productoId     ID del producto
         * @param tipoMovimiento Tipo de movimiento
         * @param pagina         Número de página
         * @param tamanioPagina  Cantidad de movimientos por página
         * @return Página de movimientos del producto con tipo especificado
         */
        Page<KardexResponse> listarPorProductoYTipo(Integer productoId, TipoMovimiento tipoMovimiento,
                        int pagina, int tamanioPagina);

        // ===== BÚSQUEDAS POR FECHA =====

        /**
         * Lista movimientos dentro de un rango de fechas específico.
         * 
         * @param fechaInicio   Fecha inicial (incluida)
         * @param fechaFin      Fecha final (incluida)
         * @param pagina        Número de página
         * @param tamanioPagina Cantidad de movimientos por página
         * @return Página de movimientos en el rango de fechas
         */
        Page<KardexResponse> listarPorRangoFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                        int pagina, int tamanioPagina);

        // ===== BÚSQUEDAS POR PROVEEDOR Y USUARIO =====

        /**
         * Lista movimientos registrados por un proveedor específico.
         * 
         * @param proveedorId   ID del proveedor
         * @param pagina        Número de página
         * @param tamanioPagina Cantidad de movimientos por página
         * @return Página de movimientos del proveedor
         */
        Page<KardexResponse> listarPorProveedor(Integer proveedorId, int pagina, int tamanioPagina);

        /**
         * Lista movimientos registrados por un usuario específico.
         * 
         * @param usuarioId     ID del usuario
         * @param pagina        Número de página
         * @param tamanioPagina Cantidad de movimientos por página
         * @return Página de movimientos del usuario
         */
        Page<KardexResponse> listarPorUsuario(Integer usuarioId, int pagina, int tamanioPagina);

        // ===== BÚSQUEDAS ESPECIALIZADAS =====

        /**
         * Busca movimientos por número de documento de soporte (factura, guía, etc.).
         * 
         * @param numeroDocumento Número de documento a buscar
         * @return Lista de movimientos con ese número de documento
         */
        List<KardexResponse> buscarPorNumeroDocumento(String numeroDocumento);

        /**
         * Busca movimientos por número de lote de producto.
         * 
         * Útil para trazabilidad de lotes específicos.
         * 
         * @param lote Número de lote a buscar
         * @return Lista de movimientos del lote
         */
        List<KardexResponse> buscarPorLote(String lote);

        /**
         * Busca productos con fecha de vencimiento próxima en un rango de fechas.
         * 
         * @param fechaInicio Fecha inicial de búsqueda
         * @param fechaFin    Fecha final de búsqueda
         * @return Lista de movimientos con vencimiento próximo
         */
        List<KardexResponse> buscarPorVencimientoProximo(LocalDateTime fechaInicio, LocalDateTime fechaFin);

        /**
         * Obtiene el historial de precios de un producto.
         * 
         * Extrae todos los precios registrados para un producto ordenados por fecha.
         * 
         * @param productoId ID del producto
         * @return Lista de movimientos mostrando variación de precios
         */
        List<KardexResponse> obtenerHistorialPreciosProducto(Integer productoId);

        // ===== RESÚMENES Y ESTADÍSTICAS =====

        /**
         * Obtiene un resumen general de movimientos del sistema.
         * 
         * Incluye totales de entradas, salidas, ajustes y valores.
         * 
         * @return Resumen con estadísticas de movimientos
         */
        MovimientoResumenResponse obtenerResumenMovimientos();

        /**
         * Calcula el saldo actual (stock neto) de un producto.
         * 
         * Suma todas las entradas y resta todas las salidas del producto.
         * 
         * @param productoId ID del producto
         * @return Saldo neto actual del producto
         */
        Integer calcularSaldoActualProducto(Integer productoId);
}
