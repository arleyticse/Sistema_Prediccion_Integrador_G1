package com.prediccion.apppredicciongm.gestion_inventario.alertas.service;

import com.prediccion.apppredicciongm.models.AlertaInventarioResponse;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de alertas de inventario
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AlertaInventarioServicio implements IAlertaInventarioServicio {

    private final IInventarioRepositorio inventarioRepositorio;

    @Override
    public List<AlertaInventarioResponse> obtenerAlertasCriticas() {
        log.info("Obteniendo alertas críticas de inventario");
        
        try {
            List<AlertaInventarioResponse> alertas = inventarioRepositorio.findInventariosCriticos()
                .stream()
                .map(this::convertirAAlertaResponse)
                .collect(Collectors.toList());
                
            // Agregar también los agotados
            List<AlertaInventarioResponse> agotados = inventarioRepositorio.findInventariosAgotados()
                .stream()
                .map(this::convertirAAlertaResponse)
                .collect(Collectors.toList());
            
            alertas.addAll(agotados);
            
            log.info("Se encontraron {} alertas críticas", alertas.size());
            return alertas;
                
        } catch (Exception e) {
            log.error("Error obteniendo alertas críticas: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<AlertaInventarioResponse> obtenerAlertasPorProveedor(String proveedor) {
        log.info("Obteniendo alertas para proveedor: {}", proveedor);
        
        try {
            return inventarioRepositorio.findInventariosCriticos()
                .stream()
                .filter(inv -> proveedor.equals(obtenerNombreProveedor(inv)))
                .map(this::convertirAAlertaResponse)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error obteniendo alertas por proveedor {}: {}", proveedor, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<AlertaInventarioResponse> obtenerTodasLasAlertas() {
        log.info("Obteniendo todas las alertas de inventario");
        
        try {
            List<AlertaInventarioResponse> criticas = obtenerAlertasCriticas();
            
            // Agregar también inventarios con stock bajo
            List<AlertaInventarioResponse> bajoStock = inventarioRepositorio.findInventariosBajoStockReorden()
                .stream()
                .map(this::convertirAAlertaResponse)
                .collect(Collectors.toList());
            
            criticas.addAll(bajoStock);
            
            return criticas.stream().distinct().collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error obteniendo todas las alertas: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean necesitaReposicion(Integer productoId) {
        try {
            return inventarioRepositorio.findByProducto(productoId)
                .map(inv -> inv.getStockDisponible() <= inv.getStockMinimo())
                .orElse(false);
                
        } catch (Exception e) {
            log.error("Error verificando reposición para producto {}: {}", productoId, e.getMessage());
            return false;
        }
    }

    /**
     * Convierte Inventario a AlertaInventarioResponse
     */
    private AlertaInventarioResponse convertirAAlertaResponse(Inventario inventario) {
        try {
            String proveedor = obtenerNombreProveedor(inventario);
            
            AlertaInventarioResponse alerta = new AlertaInventarioResponse(
                inventario.getProducto().getProductoId(),
                inventario.getProducto().getNombre(),
                "SKU-" + inventario.getProducto().getProductoId(), // Generar SKU si no existe
                proveedor,
                inventario.getStockDisponible(),
                inventario.getStockMinimo()
            );
            
            alerta.setCategoria(inventario.getProducto().getCategoria().getNombre());
            alerta.setStockMaximo(inventario.getStockMaximo());
            alerta.calcularCantidadSugerida(inventario.getStockMaximo(), 
                inventario.getProducto().getCostoAdquisicion().doubleValue());
            alerta.generarDescripcion();
            
            return alerta;
            
        } catch (Exception e) {
            log.error("Error convirtiendo Inventario a AlertaInventarioResponse: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene nombre del proveedor desde inventario
     */
    private String obtenerNombreProveedor(Inventario inventario) {
        try {
            return inventario.getProducto().getProveedorPrincipal() != null 
                ? inventario.getProducto().getProveedorPrincipal().getRazonSocial()
                : "Sin Proveedor";
        } catch (Exception e) {
            return "Sin Proveedor";
        }
    }
}