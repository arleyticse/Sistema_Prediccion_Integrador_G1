package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProveedorBasicoDTO;
import com.prediccion.apppredicciongm.models.Proveedor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para identificar el proveedor principal de un producto.
 * 
 * Analiza el historial de compras (kardex) para determinar cual es
 * el proveedor mas frecuente o el ultimo utilizado.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProveedorService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Obtiene el proveedor principal de un producto.
     * 
     * Estrategia:
     * 1. Busca el proveedor mas frecuente en movimientos de tipo ENTRADA
     * 2. Si no hay historial, busca el proveedor configurado en el producto
     * 3. Si no hay proveedor, retorna null
     * 
     * @param productoId ID del producto
     * @return Informacion basica del proveedor o null si no hay
     */
    public ProveedorBasicoDTO obtenerProveedorPrincipal(Integer productoId) {
        log.debug("Buscando proveedor principal para producto ID: {}", productoId);
        
        try {
            // Consulta el proveedor mas frecuente en el kardex (movimientos de entrada)
            String sql = """
                SELECT 
                    p.id_proveedor,
                    p.nombre_comercial,
                    p.tiempo_entrega_dias,
                    p.persona_contacto,
                    p.telefono
                FROM kardex k
                INNER JOIN proveedores p ON k.id_proveedor = p.id_proveedor
                WHERE k.id_producto = ?
                    AND k.tipo_movimiento = 'ENTRADA'
                GROUP BY p.id_proveedor, p.nombre_comercial, 
                    p.tiempo_entrega_dias, p.persona_contacto, p.telefono
                ORDER BY COUNT(*) DESC, MAX(k.fecha_movimiento) DESC
                LIMIT 1
            """;
            
            List<ProveedorBasicoDTO> resultados = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> ProveedorBasicoDTO.builder()
                    .proveedorId(rs.getInt("id_proveedor"))
                    .nombreComercial(rs.getString("nombre_comercial"))
                    .tiempoEntregaDias(rs.getInt("tiempo_entrega_dias"))
                    .contacto(rs.getString("persona_contacto"))
                    .telefono(rs.getString("telefono"))
                    .build(),
                productoId
            );
            
            if (!resultados.isEmpty()) {
                log.debug("Proveedor principal encontrado: {}", 
                    resultados.get(0).getNombreComercial());
                return resultados.get(0);
            }
            
            // Si no hay historial, buscar proveedor por defecto del producto
            log.debug("No se encontro historial de compras, buscando proveedor por defecto");
            return obtenerProveedorPorDefecto(productoId);
            
        } catch (Exception e) {
            log.error("Error al obtener proveedor principal para producto ID: {}", 
                productoId, e);
            return null;
        }
    }

    /**
     * Obtiene el proveedor por defecto de un producto.
     * Busca en la configuracion del producto o el primer proveedor disponible.
     * 
     * @param productoId ID del producto
     * @return Proveedor por defecto o null
     */
    private ProveedorBasicoDTO obtenerProveedorPorDefecto(Integer productoId) {
        try {
            // Buscar el primer proveedor asociado al producto en el kardex
            String sql = """
                SELECT DISTINCT
                    p.id_proveedor,
                    p.nombre_comercial,
                    p.tiempo_entrega_dias,
                    p.persona_contacto,
                    p.telefono
                FROM kardex k
                INNER JOIN proveedores p ON k.id_proveedor = p.id_proveedor
                WHERE k.id_producto = ?
                LIMIT 1
            """;
            
            List<ProveedorBasicoDTO> resultados = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> ProveedorBasicoDTO.builder()
                    .proveedorId(rs.getInt("id_proveedor"))
                    .nombreComercial(rs.getString("nombre_comercial"))
                    .tiempoEntregaDias(rs.getInt("tiempo_entrega_dias"))
                    .contacto(rs.getString("persona_contacto"))
                    .telefono(rs.getString("telefono"))
                    .build(),
                productoId
            );
            
            return resultados.isEmpty() ? null : resultados.get(0);
            
        } catch (Exception e) {
            log.error("Error al obtener proveedor por defecto para producto ID: {}", 
                productoId, e);
            return null;
        }
    }

    /**
     * Obtiene todos los proveedores que han suministrado un producto.
     * 
     * @param productoId ID del producto
     * @return Lista de proveedores ordenada por frecuencia
     */
    public List<ProveedorBasicoDTO> obtenerProveedoresDelProducto(Integer productoId) {
        log.debug("Obteniendo proveedores del producto ID: {}", productoId);
        
        String sql = """
            SELECT 
                p.id_proveedor,
                p.nombre_comercial,
                p.tiempo_entrega_dias,
                p.persona_contacto,
                p.telefono,
                COUNT(*) as frecuencia
            FROM kardex k
            INNER JOIN proveedores p ON k.id_proveedor = p.id_proveedor
            WHERE k.id_producto = ?
                AND k.tipo_movimiento = 'ENTRADA'
            GROUP BY p.id_proveedor, p.nombre_comercial, 
                p.tiempo_entrega_dias, p.persona_contacto, p.telefono
            ORDER BY frecuencia DESC
        """;
        
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> ProveedorBasicoDTO.builder()
                .proveedorId(rs.getInt("id_proveedor"))
                .nombreComercial(rs.getString("nombre_comercial"))
                .tiempoEntregaDias(rs.getInt("tiempo_entrega_dias"))
                .contacto(rs.getString("persona_contacto"))
                .telefono(rs.getString("telefono"))
                .build(),
            productoId
        );
    }
}
