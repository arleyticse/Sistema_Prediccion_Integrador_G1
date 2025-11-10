package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenOrdenDTO;
import com.prediccion.apppredicciongm.models.OrdenCompra;

import java.util.List;
import java.util.Map;

/**
 * Servicio para generacion automatica de ordenes de compra en lote.
 * 
 * Agrupa productos por proveedor y genera ordenes de compra
 * optimizadas basadas en predicciones y calculos EOQ/ROP.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public interface IOrdenCompraBatchService {

    /**
     * Genera ordenes de compra agrupadas por proveedor.
     * 
     * Proceso:
     * 1. Obtiene alertas y sus productos
     * 2. Identifica proveedor principal de cada producto
     * 3. Agrupa productos por proveedor
     * 4. Obtiene EOQ de cada producto (de optimizacion)
     * 5. Crea una OrdenCompra por cada proveedor
     * 6. Genera DetalleOrdenCompra con cantidad = EOQ
     * 7. Marca alertas como resueltas
     * 
     * @param alertaIds Lista de IDs de alertas a procesar
     * @param usuarioId ID del usuario que genera las ordenes
     * @return Resumen del procesamiento con ordenes generadas
     */
    ProcesamientoBatchResponse generarOrdenesPorProveedor(
        List<Long> alertaIds, 
        Integer usuarioId
    );

    /**
     * Obtiene un preview de las ordenes que se generarian.
     * No persiste en la base de datos, solo retorna la estructura.
     * 
     * @param alertaIds Lista de IDs de alertas
     * @return Mapa con proveedor como clave y lista de productos con cantidades
     */
    Map<String, List<ItemOrdenPreview>> obtenerPreviewOrdenes(List<Long> alertaIds);

    /**
     * Genera una orden de compra para un proveedor especifico.
     * 
     * @param proveedorId ID del proveedor
     * @param items Lista de productos con cantidades
     * @param usuarioId ID del usuario
     * @return Orden de compra generada
     */
    OrdenCompra generarOrdenParaProveedor(
        Integer proveedorId,
        List<ItemOrden> items,
        Integer usuarioId
    );

    /**
     * Obtiene el resumen detallado de ordenes de compra generadas.
     * 
     * Utilizado para mostrar los detalles de las ordenes generadas
     * durante el procesamiento automatico de alertas en el frontend.
     * 
     * @param ordenIds Lista de IDs de ordenes de compra
     * @return Lista de resumenes de ordenes con detalles
     */
    List<ResumenOrdenDTO> obtenerResumenOrdenes(List<Long> ordenIds);

    /**
     * Clase interna para preview de items de orden.
     */
    class ItemOrdenPreview {
        public Integer productoId;
        public String productoNombre;
        public Integer cantidad;
        public Double precioUnitario;
        public Double subtotal;
    }

    /**
     * Clase interna para items de orden a generar.
     */
    class ItemOrden {
        public Integer productoId;
        public Integer cantidad;
        public Long alertaId;
    }
}
