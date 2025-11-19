package com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

// Imports para los modelos y repositorios
import com.prediccion.apppredicciongm.models.OrdenCompra;
import com.prediccion.apppredicciongm.models.DetalleOrdenCompra;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.repository.IOrdenCompraRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.repository.IProveedorRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.dto.response.*;
import com.prediccion.apppredicciongm.gestion_prediccion.orden_compra.errors.OrdenCompraNoEncontradaException;
import com.prediccion.apppredicciongm.models.Inventario.Producto;

import java.util.stream.Collectors;

/**
 * Servicio principal para órdenes de compra automáticas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrdenCompraService implements IOrdenCompraService {

    private final IOrdenCompraRepositorio ordenCompraRepositorio;
    private final IPrediccionRepositorio prediccionRepositorio;
    private final IProveedorRepositorio proveedorRepositorio;

    @Override
    public OrdenCompra generarOrdenAutomatica(Integer prediccionId) {
        return generarOrdenDesdePredicion(prediccionId);
    }

    @Override
    public boolean validarOrdenCompra(Integer productoId) {
        // Implementación básica - siempre retorna true para testing
        log.info("[ORDEN][VALIDACION] Validando orden de compra para producto ID: {}", productoId);
        return true;
    }

    @Override
    public Page<OrdenCompra> obtenerOrdenesPorProducto(Integer productoId, Pageable pageable) {
        log.info("[ORDEN][CONSULTA] Obteniendo órdenes para producto ID: {}", productoId);
        // Implementación básica
        List<OrdenCompra> todasLasOrdenes = ordenCompraRepositorio.findAll();
        return new PageImpl<>(todasLasOrdenes, pageable, todasLasOrdenes.size());
    }

    @Override
    public Page<OrdenCompra> obtenerTodasLasOrdenes(Pageable pageable) {
        log.info("[ORDEN][CONSULTA] Obteniendo todas las órdenes con paginación");
        return ordenCompraRepositorio.findAll(pageable);
    }

    @Override
    public OrdenCompra obtenerUltimaOrden(Integer productoId) {
        log.info("[ORDEN][CONSULTA] Obteniendo última orden para producto ID: {}", productoId);
        return ordenCompraRepositorio.findAll().stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public void confirmarOrden(Long ordenId) {
        log.info("[ORDEN][CONFIRMACION] Confirmando orden ID: {}", ordenId);
        Optional<OrdenCompra> ordenOpt = ordenCompraRepositorio.findById(ordenId);
        if (ordenOpt.isPresent()) {
            OrdenCompra orden = ordenOpt.get();
            orden.setEstadoOrden(EstadoOrdenCompra.APROBADA);
            ordenCompraRepositorio.save(orden);
            log.info("[ORDEN][CONFIRMACION] Orden {} confirmada exitosamente", ordenId);
        } else {
            log.error("[ORDEN][ERROR] Orden no encontrada: {}", ordenId);
            throw new RuntimeException("Orden no encontrada: " + ordenId);
        }
    }

    @Override
    public void cancelarOrden(Long ordenId) {
        log.info("[ORDEN][CANCELACION] Cancelando orden ID: {}", ordenId);
        Optional<OrdenCompra> ordenOpt = ordenCompraRepositorio.findById(ordenId);
        if (ordenOpt.isPresent()) {
            OrdenCompra orden = ordenOpt.get();
            orden.setEstadoOrden(EstadoOrdenCompra.CANCELADA);
            ordenCompraRepositorio.save(orden);
            log.info("[ORDEN][CANCELACION] Orden {} cancelada exitosamente", ordenId);
        } else {
            log.error("[ORDEN][ERROR] Orden no encontrada: {}", ordenId);
            throw new RuntimeException("Orden no encontrada: " + ordenId);
        }
    }

    @Override
    public OrdenCompra generarOrdenDesdePredicion(Integer prediccionId) {
        try {
            log.info("[ORDEN] Generando orden automática desde predicción ID: {}", prediccionId);
            
            // Buscar la predicción
            Optional<Prediccion> prediccionOpt = prediccionRepositorio.findById(prediccionId);
            if (!prediccionOpt.isPresent()) {
                throw new RuntimeException("Predicción no encontrada: " + prediccionId);
            }
            
            Prediccion prediccion = prediccionOpt.get();
            
            if (prediccion.getProducto() == null) {
                throw new RuntimeException("Producto no encontrado en predicción");
            }

            // Obtener el primer proveedor disponible
            List<Proveedor> proveedores = proveedorRepositorio.findAll();
            if (proveedores.isEmpty()) {
                throw new RuntimeException("No hay proveedores disponibles");
            }
            Proveedor proveedor = proveedores.get(0);

            // Calcular cantidad basada en predicción
            Integer cantidadPedido = 1;
            if (prediccion.getDemandaPredichaTotal() != null) {
                cantidadPedido = Math.max(prediccion.getDemandaPredichaTotal(), 1);
            }
            
            // Calcular precio total estimado
            BigDecimal precioUnitario = BigDecimal.valueOf(10.0);
            BigDecimal totalOrden = precioUnitario.multiply(BigDecimal.valueOf(cantidadPedido));

            // Crear la orden
            OrdenCompra orden = new OrdenCompra();
            orden.setProveedor(proveedor);
            orden.setTotalOrden(totalOrden);
            orden.setGeneradaAutomaticamente(true);
            orden.setEstadoOrden(EstadoOrdenCompra.PENDIENTE);
            orden.setObservaciones(String.format(
                "Orden generada automáticamente desde predicción %d. Demanda predicha: %d", 
                prediccionId, prediccion.getDemandaPredichaTotal()
            ));
            orden.setNumeroOrden("AUTO-" + System.currentTimeMillis());
            orden.setFechaOrden(LocalDate.now());
            orden.setFechaEntregaEsperada(LocalDate.now().plusDays(7));

            // Crear detalle de la orden
            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setProducto(prediccion.getProducto());
            detalle.setCantidadSolicitada(cantidadPedido);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(totalOrden);
            detalle.setOrdenCompra(orden);
            
            // Crear lista de detalles
            List<DetalleOrdenCompra> detalles = new ArrayList<>();
            detalles.add(detalle);
            orden.setDetalles(detalles);

            // Guardar la orden
            OrdenCompra ordenGuardada = ordenCompraRepositorio.save(orden);
            
            log.info("[ORDEN] Orden automática creada: {} para {} unidades", 
                    ordenGuardada.getNumeroOrden(), cantidadPedido);
            
            return ordenGuardada;
            
        } catch (Exception e) {
            log.error("[ORDEN] Error generando orden automática: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar orden automática: " + e.getMessage());
        }
    }

    @Override
    public List<OrdenCompra> obtenerOrdenesBorrador() {
        log.info("[ORDEN][CONSULTA] Obteniendo órdenes en estado BORRADOR");
        List<OrdenCompra> ordenes = ordenCompraRepositorio.findByEstadoOrden(EstadoOrdenCompra.BORRADOR);
        log.info("[ORDEN][CONSULTA] Se encontraron {} órdenes en BORRADOR", ordenes.size());
        return ordenes;
    }

    @Override
    public void aprobarOrdenesBorrador(List<Long> ordenIds) {
        log.info("[ORDEN][APROBACION] Aprobando {} órdenes BORRADOR", ordenIds.size());
        
        for (Long ordenId : ordenIds) {
            Optional<OrdenCompra> ordenOpt = ordenCompraRepositorio.findById(ordenId);
            
            if (!ordenOpt.isPresent()) {
                log.warn("[ORDEN][APROBACION] Advertencia: Orden {} no encontrada, se omite", ordenId);
                continue;
            }
            
            OrdenCompra orden = ordenOpt.get();
            
            if (orden.getEstadoOrden() != EstadoOrdenCompra.BORRADOR) {
                log.warn("[ORDEN][APROBACION] Advertencia: Orden {} no está en BORRADOR (estado actual: {}), se omite", 
                        ordenId, orden.getEstadoOrden());
                continue;
            }
            
            orden.setEstadoOrden(EstadoOrdenCompra.PENDIENTE);
            ordenCompraRepositorio.save(orden);
            
            log.info("[ORDEN][APROBACION] Orden {} aprobada: BORRADOR → PENDIENTE", ordenId);
        }
        
        log.info("[ORDEN][APROBACION] Proceso de aprobación completado");
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenOrdenCompraDTO obtenerResumenOrdenCompra(Long ordenId) {
        
        OrdenCompra orden = ordenCompraRepositorio.findById(ordenId)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada con ID: " + ordenId));
        
        // Construir datos de la empresa (información estática o de configuración)
        DatosEmpresaDTO datosEmpresa = DatosEmpresaDTO.builder()
                .razonSocial("Sistema de Predicción y Gestión")
                .nombreComercial("Predicción GM")
                .ruc("9999999999999") // Configurable
                .direccion("Dirección de la empresa")
                .ciudad("Ciudad")
                .pais("Ecuador")
                .telefono("0999999999")
                .email("info@empresa.com")
                .build();
        
        // Construir datos del proveedor
        DatosProveedorDTO datosProveedor = DatosProveedorDTO.builder()
                .razonSocial(orden.getProveedor().getRazonSocial())
                .nombreComercial(orden.getProveedor().getNombreComercial())
                .rucNit(orden.getProveedor().getRucNit())
                .direccion(orden.getProveedor().getDireccion())
                .ciudad(orden.getProveedor().getCiudad())
                .pais(orden.getProveedor().getPais())
                .telefono(orden.getProveedor().getTelefono())
                .email(orden.getProveedor().getEmail())
                .personaContacto(orden.getProveedor().getPersonaContacto())
                .build();
        
        // Construir lista de detalles
        List<DetalleProductoOrdenDTO> detalles = orden.getDetalles().stream()
                .map(detalle -> DetalleProductoOrdenDTO.builder()
                        .nombreProducto(detalle.getProducto().getNombre())
                        .unidadMedida(detalle.getProducto().getUnidadMedida().toString())
                        .cantidadSolicitada(detalle.getCantidadSolicitada())
                        .precioUnitario(detalle.getPrecioUnitario())
                        .subtotal(detalle.getSubtotal())
                        .build())
                .collect(Collectors.toList());
        
        // Calcular totales
        BigDecimal subtotalBD = orden.getTotalOrden() != null ? orden.getTotalOrden() : BigDecimal.ZERO;
        BigDecimal impuestos = BigDecimal.ZERO; // Si se implementa IVA, calcular aquí
        BigDecimal total = subtotalBD.add(impuestos);
        
        return ResumenOrdenCompraDTO.builder()
                .ordenCompraId(orden.getOrdenCompraId())
                .numeroOrden(orden.getNumeroOrden())
                .estadoOrden(orden.getEstadoOrden())
                .fechaOrden(orden.getFechaOrden())
                .fechaEntregaEsperada(orden.getFechaEntregaEsperada())
                .fechaEntregaReal(orden.getFechaEntregaReal())
                .empresa(datosEmpresa)
                .proveedor(datosProveedor)
                .detalles(detalles)
                .subtotal(subtotalBD)
                .impuestos(impuestos)
                .totalOrden(total)
                .generadaAutomaticamente(orden.getGeneradaAutomaticamente())
                .observaciones(orden.getObservaciones())
                .fechaCreacion(orden.getFechaRegistro())
                .usuarioCreador(orden.getUsuario() != null ? orden.getUsuario().getNombre() : null)
                .build();
    }
}