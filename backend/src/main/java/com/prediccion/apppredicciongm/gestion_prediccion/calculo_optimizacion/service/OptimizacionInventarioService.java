package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalcularOptimizacionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.OptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del servicio de optimización de inventario
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OptimizacionInventarioService implements IOptimizacionInventarioService {
    
    private final IPrediccionRepositorio prediccionRepositorio;
    
    // Tabla de valores Z para distribución normal
    private static final Map<Double, Double> TABLA_Z = new HashMap<>() {{
        put(0.80, 0.84);
        put(0.85, 1.04);
        put(0.90, 1.28);
        put(0.925, 1.44);
        put(0.95, 1.65);
        put(0.975, 1.96);
        put(0.99, 2.33);
        put(0.995, 2.58);
    }};
    
    @Override
    @Transactional
    public OptimizacionResponse calcularOptimizacion(CalcularOptimizacionRequest request) {
        log.info("Calculando optimización para predicción ID: {}", request.getPrediccionId());
        
        // 1. Obtener la predicción desde la base de datos
        Prediccion prediccion = prediccionRepositorio.findById(request.getPrediccionId().intValue())
            .orElseThrow(() -> new IllegalArgumentException("Predicción no encontrada: " + request.getPrediccionId()));
        
        // 1.1. Obtener producto asociado para extraer costos desde la BD
        Producto producto = prediccion.getProducto();
        if (producto == null) {
            throw new IllegalArgumentException("La predicción no tiene un producto asociado");
        }
        log.debug("Producto asociado: {} (ID: {})", producto.getNombre(), producto.getProductoId());
        
        // 1.2. Obtener parámetros de costos desde la BD o request (request tiene prioridad)
        Double costoPedido = obtenerCostoPedido(request, producto);
        Double costoAlmacenamiento = obtenerCostoAlmacenamiento(request, producto);
        Double costoUnitario = obtenerCostoUnitario(request, producto);
        Integer tiempoEntregaDias = obtenerTiempoEntrega(request, producto);
        
        log.info("Parámetros de cálculo - Costo Pedido: {}, Costo Almacenamiento: {}, Costo Unitario: {}, Lead Time: {} días",
                 costoPedido, costoAlmacenamiento, costoUnitario, tiempoEntregaDias);
        
        // 2. Extraer datos de demanda
        Double demandaPredichaTotal = prediccion.getDemandaPredichaTotal().doubleValue();
        Integer horizonteTiempo = prediccion.getHorizonteTiempo();
        
        // Convertir demanda del horizonte a demanda anual
        Double demandaAnual = convertirADemandaAnual(demandaPredichaTotal, horizonteTiempo);
        Double demandaDiaria = demandaAnual / 365.0;
        
        log.debug("Demanda predicha: {} unidades en {} días", demandaPredichaTotal, horizonteTiempo);
        log.debug("Demanda anual proyectada: {} unidades", demandaAnual);
        log.debug("Demanda diaria promedio: {} unidades", demandaDiaria);
        
        // 3. Calcular desviación estándar si no se proveyó
        Double desviacionEstandar = request.getDesviacionEstandarDemanda();
        if (desviacionEstandar == null || desviacionEstandar == 0.0) {
            desviacionEstandar = calcularDesviacionEstandar(prediccion);
            log.debug("Desviación estándar calculada: {}", desviacionEstandar);
        }
        
        // 4. Obtener factor Z según nivel de servicio
        Double factorZ = obtenerFactorZ(request.getNivelServicioDeseado());
        log.debug("Factor Z para nivel de servicio {}%: {}", 
                  request.getNivelServicioDeseado() * 100, factorZ);
        
        // 5. Calcular EOQ (Economic Order Quantity)
        Double eoq = calcularEOQ(
            demandaAnual,
            costoPedido,
            costoAlmacenamiento
        );
        log.debug("EOQ calculado: {} unidades", eoq);
        
        // 6. Calcular Stock de Seguridad
        Double stockSeguridad = calcularStockSeguridad(
            factorZ,
            desviacionEstandar,
            tiempoEntregaDias
        );
        log.debug("Stock de seguridad calculado: {} unidades", stockSeguridad);
        
        // 7. Calcular ROP (Reorder Point)
        Double rop = calcularROP(
            demandaDiaria,
            tiempoEntregaDias,
            stockSeguridad
        );
        log.debug("ROP calculado: {} unidades", rop);
        
        // 8. Calcular métricas de costos
        Double numeroOptimoPedidos = demandaAnual / eoq;
        Double cicloOptimoDias = 365.0 / numeroOptimoPedidos;
        
        Double costoOrdenamiento = numeroOptimoPedidos * costoPedido;
        Double costoAlmacenamientoAnual = (eoq / 2.0) * costoAlmacenamiento;
        Double costoStockSeguridad = stockSeguridad * costoAlmacenamiento;
        Double costoTotalAnual = costoOrdenamiento + costoAlmacenamientoAnual + costoStockSeguridad;
        
        // 9. Calcular coeficiente de variación
        Double coeficienteVariacion = desviacionEstandar / demandaDiaria;
        
        // 10. Generar recomendaciones
        String recomendacion = generarRecomendacion(
            eoq, rop, stockSeguridad, numeroOptimoPedidos, cicloOptimoDias, coeficienteVariacion
        );
        
        String advertencia = generarAdvertencias(
            coeficienteVariacion, tiempoEntregaDias, stockSeguridad, demandaDiaria
        );
        
        String nivelConfianza = determinarNivelConfianza(coeficienteVariacion, prediccion);
        
        // 11. Obtener usuario actual
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 12. Construir response
        OptimizacionResponse response = OptimizacionResponse.builder()
            .prediccionId(request.getPrediccionId())
            .productoId(prediccion.getProducto() != null ? prediccion.getProducto().getProductoId().longValue() : null)
            .productoNombre(prediccion.getProducto() != null ? prediccion.getProducto().getNombre() : "Producto")
            .codigoProducto(prediccion.getProducto() != null ? "PROD-" + prediccion.getProducto().getProductoId() : "N/A")
            // EOQ
            .cantidadEconomicaPedido(eoq)
            .numeroOptimoPedidos(numeroOptimoPedidos)
            .cicloOptimoDias(cicloOptimoDias)
            // ROP
            .puntoReorden(rop)
            .stockSeguridad(stockSeguridad)
            .stockMaximo(eoq + stockSeguridad)
            // Costos
            .costoTotalAnual(costoTotalAnual)
            .costoOrdenamiento(costoOrdenamiento)
            .costoAlmacenamientoAnual(costoAlmacenamientoAnual)
            .costoStockSeguridad(costoStockSeguridad)
            // Análisis de demanda
            .demandaAnual(demandaAnual)
            .demandaDiaria(demandaDiaria)
            .desviacionEstandarDemanda(desviacionEstandar)
            .coeficienteVariacion(coeficienteVariacion)
            // Parámetros (usando los valores finales determinados)
            .costoPedido(costoPedido)
            .costoAlmacenamiento(costoAlmacenamiento)
            .costoUnitario(costoUnitario)
            .tiempoEntregaDias(tiempoEntregaDias)
            .nivelServicioDeseado(request.getNivelServicioDeseado())
            .factorZ(factorZ)
            // Recomendaciones
            .recomendacion(recomendacion)
            .advertencia(advertencia)
            .nivelConfianza(nivelConfianza)
            // Metadatos
            .fechaCalculo(LocalDateTime.now())
            .calculadoPor(usuario)
            .build();
        
        // 13. Persistir en base de datos (opcional - para histórico)
        // guardarCalculoOptimizacion(response, prediccion);
        
        log.info("Optimización calculada exitosamente para predicción ID: {}", request.getPrediccionId());
        return response;
    }
    
    @Override
    public Double calcularEOQ(Double demandaAnual, Double costoPedido, Double costoAlmacenamiento) {
        if (demandaAnual <= 0 || costoPedido <= 0 || costoAlmacenamiento <= 0) {
            throw new IllegalArgumentException("Demanda, costo de pedido y costo de almacenamiento deben ser positivos");
        }
        
        // Fórmula EOQ: √((2 × D × S) / H)
        Double eoq = Math.sqrt((2.0 * demandaAnual * costoPedido) / costoAlmacenamiento);
        return Math.round(eoq * 100.0) / 100.0; // Redondear a 2 decimales
    }
    
    @Override
    public Double calcularROP(Double demandaDiaria, Integer tiempoEntregaDias, Double stockSeguridad) {
        if (demandaDiaria <= 0 || tiempoEntregaDias <= 0) {
            throw new IllegalArgumentException("Demanda diaria y tiempo de entrega deben ser positivos");
        }
        
        // Fórmula ROP: d × L + SS
        Double rop = (demandaDiaria * tiempoEntregaDias) + stockSeguridad;
        return Math.round(rop * 100.0) / 100.0; // Redondear a 2 decimales
    }
    
    @Override
    public Double calcularStockSeguridad(Double factorZ, Double desviacionEstandar, Integer tiempoEntregaDias) {
        if (factorZ <= 0 || desviacionEstandar < 0 || tiempoEntregaDias <= 0) {
            throw new IllegalArgumentException("Parámetros de stock de seguridad inválidos");
        }
        
        // Fórmula SS: Z × σ × √L
        Double ss = factorZ * desviacionEstandar * Math.sqrt(tiempoEntregaDias);
        return Math.round(ss * 100.0) / 100.0; // Redondear a 2 decimales
    }
    
    @Override
    public Double obtenerFactorZ(Double nivelServicio) {
        // Buscar el valor más cercano en la tabla
        Double mejorNivel = TABLA_Z.keySet().stream()
            .min((a, b) -> Double.compare(
                Math.abs(a - nivelServicio),
                Math.abs(b - nivelServicio)
            ))
            .orElse(0.95); // Default 95%
        
        return TABLA_Z.get(mejorNivel);
    }
    
    @Override
    public OptimizacionResponse obtenerOptimizacionPorPrediccion(Long prediccionId) {
        log.info("Obteniendo optimización guardada para predicción ID: {}", prediccionId);
        
        // Nota: Este método retornaría el último cálculo guardado en BD
        // Por ahora retorna null ya que no se está persistiendo
        log.warn("Funcionalidad de obtener optimización guardada no implementada aún");
        return null;
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private Double convertirADemandaAnual(Double demandaPredicha, Integer horizonteDias) {
        // Convertir la demanda del horizonte a demanda anual
        return (demandaPredicha / horizonteDias) * 365.0;
    }
    
    private Double calcularDesviacionEstandar(Prediccion prediccion) {
        // Aproximación: usar 20% de la demanda promedio como desviación estándar
        Double demandaPromedio = prediccion.getDemandaPredichaTotal().doubleValue() / prediccion.getHorizonteTiempo();
        return demandaPromedio * 0.20; // 20% de variabilidad asumida
    }
    
    private String generarRecomendacion(Double eoq, Double rop, Double stockSeguridad,
                                       Double numeroOptimoPedidos, Double cicloOptimoDias,
                                       Double coeficienteVariacion) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("**Cantidad Óptima de Pedido:** Ordene %.0f unidades cada vez. ", eoq));
        sb.append(String.format("Este es el equilibrio perfecto entre costos de pedido y almacenamiento.\n\n", eoq));
        
        sb.append(String.format("**Punto de Reorden:** Haga un nuevo pedido cuando el inventario llegue a %.0f unidades. ", rop));
        sb.append(String.format("Esto considera el tiempo de entrega del proveedor y un stock de seguridad de %.0f unidades.\n\n", stockSeguridad));
        
        sb.append(String.format("**Frecuencia:** Realice aproximadamente %.1f pedidos al año (cada %.0f días).\n\n", 
                                numeroOptimoPedidos, cicloOptimoDias));
        
        // Recomendación según variabilidad
        if (coeficienteVariacion < 0.15) {
            sb.append("**Demanda Estable:** Su producto tiene baja variabilidad. Puede mantener inventarios ajustados.");
        } else if (coeficienteVariacion < 0.30) {
            sb.append("**Demanda Moderada:** Considere revisar el stock de seguridad mensualmente.");
        } else {
            sb.append("**Demanda Volátil:** Se recomienda aumentar el stock de seguridad y revisar semanalmente.");
        }
        
        return sb.toString();
    }
    
    private String generarAdvertencias(Double coeficienteVariacion, Integer leadTime,
                                      Double stockSeguridad, Double demandaDiaria) {
        StringBuilder sb = new StringBuilder();
        
        // Advertencia por variabilidad alta
        if (coeficienteVariacion > 0.30) {
            sb.append(" Alta variabilidad en la demanda detectada (CV > 30%). ");
            sb.append("Considere aumentar el stock de seguridad o revisar pronósticos con mayor frecuencia.\n\n");
        }
        
        // Advertencia por lead time largo
        if (leadTime > 30) {
            sb.append("Tiempo de entrega largo (").append(leadTime).append(" días). ");
            sb.append("Se requiere planificación anticipada y mayor stock de seguridad.\n\n");
        }
        
        // Advertencia por stock de seguridad alto
        Double ratioSS = stockSeguridad / demandaDiaria;
        if (ratioSS > 30) {
            sb.append("Stock de seguridad elevado (más de 30 días de demanda). ");
            sb.append("Verifique la precisión de las proyecciones y considere negociar tiempos de entrega más cortos.\n\n");
        }
        
        if (sb.length() == 0) {
            return "Sin advertencias. Los parámetros están dentro de rangos normales.";
        }
        
        return sb.toString();
    }
    
    private String determinarNivelConfianza(Double coeficienteVariacion, Prediccion prediccion) {
        // Basado solo en coeficiente de variación
        if (coeficienteVariacion < 0.15) {
            return "ALTO";
        } else if (coeficienteVariacion < 0.30) {
            return "MEDIO";
        } else {
            return "BAJO";
        }
    }
    
    // ==================== MÉTODOS DE OBTENCIÓN DE PARÁMETROS CON FALLBACK A BD ====================
    
    /**
     * Obtiene el costo de pedido desde el request o desde la BD del producto
     * Prioridad: request > BD > excepción
     */
    private Double obtenerCostoPedido(CalcularOptimizacionRequest request, Producto producto) {
        if (request.getCostoPedido() != null && request.getCostoPedido() > 0) {
            log.debug("Usando costo de pedido del request: {}", request.getCostoPedido());
            return request.getCostoPedido();
        }
        
        if (producto.getCostoPedido() != null && producto.getCostoPedido().doubleValue() > 0) {
            log.info("Usando costo de pedido de la BD: {}", producto.getCostoPedido());
            return producto.getCostoPedido().doubleValue();
        }
        
        throw new IllegalArgumentException(
            "Costo de pedido no disponible. Configure el campo 'costoPedido' en el producto o proporcione el valor en el request."
        );
    }
    
    /**
     * Obtiene el costo de almacenamiento desde el request o desde la BD del producto
     * Prioridad: request > BD > excepción
     */
    private Double obtenerCostoAlmacenamiento(CalcularOptimizacionRequest request, Producto producto) {
        if (request.getCostoAlmacenamiento() != null && request.getCostoAlmacenamiento() > 0) {
            log.debug("Usando costo de almacenamiento del request: {}", request.getCostoAlmacenamiento());
            return request.getCostoAlmacenamiento();
        }
        
        if (producto.getCostoMantenimientoAnual() != null && producto.getCostoMantenimientoAnual().doubleValue() > 0) {
            log.info("Usando costo de almacenamiento de la BD (costoMantenimientoAnual): {}", producto.getCostoMantenimientoAnual());
            return producto.getCostoMantenimientoAnual().doubleValue();
        }
        
        throw new IllegalArgumentException(
            "Costo de almacenamiento no disponible. Configure el campo 'costoMantenimientoAnual' en el producto o proporcione el valor en el request."
        );
    }
    
    /**
     * Obtiene el costo unitario desde el request o desde la BD del producto
     * Prioridad: request > BD > excepción
     */
    private Double obtenerCostoUnitario(CalcularOptimizacionRequest request, Producto producto) {
        if (request.getCostoUnitario() != null && request.getCostoUnitario() > 0) {
            log.debug("Usando costo unitario del request: {}", request.getCostoUnitario());
            return request.getCostoUnitario();
        }
        
        if (producto.getCostoAdquisicion() != null && producto.getCostoAdquisicion().doubleValue() > 0) {
            log.info("Usando costo unitario de la BD (costoAdquisicion): {}", producto.getCostoAdquisicion());
            return producto.getCostoAdquisicion().doubleValue();
        }
        
        throw new IllegalArgumentException(
            "Costo unitario no disponible. Configure el campo 'costoAdquisicion' en el producto o proporcione el valor en el request."
        );
    }
    
    /**
     * Obtiene el tiempo de entrega desde el request o desde la BD del producto
     * Prioridad: request > BD > excepción
     */
    private Integer obtenerTiempoEntrega(CalcularOptimizacionRequest request, Producto producto) {
        if (request.getTiempoEntregaDias() != null && request.getTiempoEntregaDias() > 0) {
            log.debug("Usando tiempo de entrega del request: {} días", request.getTiempoEntregaDias());
            return request.getTiempoEntregaDias();
        }
        
        if (producto.getDiasLeadTime() != null && producto.getDiasLeadTime() > 0) {
            log.info("Usando tiempo de entrega de la BD (diasLeadTime): {} días", producto.getDiasLeadTime());
            return producto.getDiasLeadTime();
        }
        
        throw new IllegalArgumentException(
            "Tiempo de entrega no disponible. Configure el campo 'diasLeadTime' en el producto o proporcione el valor en el request."
        );
    }
}
