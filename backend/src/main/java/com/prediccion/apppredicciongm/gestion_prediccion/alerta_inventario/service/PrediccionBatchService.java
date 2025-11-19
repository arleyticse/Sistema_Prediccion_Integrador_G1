package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.ProcesarAlertasRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.ErrorProcesamientoLoteException;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository.IAlertaInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.estacionalidad.service.AnalisisEstacionalidadService;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.repository.IRegistroDemandaRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.ISmartPredictorService;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.request.SmartPrediccionRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.SmartPrediccionResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.utils.SKUGenerator;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.models.RegistroDemanda;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de procesamiento batch de predicciones.
 * 
 * Ejecuta predicciones en paralelo para multiples productos,
 * optimizando el tiempo de procesamiento.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrediccionBatchService implements IPrediccionBatchService {

    private final ISmartPredictorService smartPredictorService;
    private final IAlertaInventarioRepositorio alertaRepositorio;
    private final IOptimizacionBatchService optimizacionBatchService;
    private final IOrdenCompraBatchService ordenCompraBatchService;
    private final AnalisisEstacionalidadService estacionalidadService;
    private final HorizontePrediccionService horizonteService;
    private final IRegistroDemandaRepositorio registroDemandaRepositorio;
    
    private static final int THREAD_POOL_SIZE = 5;

    @Override
    @Transactional
    public ProcesamientoBatchResponse ejecutarPrediccionesBatch(ProcesarAlertasRequest request) {
        log.info("Iniciando procesamiento batch de {} alertas", request.getAlertaIds().size());
        
        LocalDateTime inicio = LocalDateTime.now();
        ProcesamientoBatchResponse response = ProcesamientoBatchResponse.builder()
            .fechaInicio(inicio)
            .totalProcesadas(request.getAlertaIds().size())
            .exitosos(0)
            .fallidos(0)
            .exitoTotal(false)
            .build();

        try {
            // 1. Obtener alertas
            List<AlertaInventario> alertas = alertaRepositorio.findAllById(request.getAlertaIds());
            
            if (alertas.isEmpty()) {
                throw new ErrorProcesamientoLoteException(
                    "No se encontraron alertas con los IDs proporcionados"
                );
            }

            // 2. Validar que todas las alertas tienen producto
            alertas.forEach(alerta -> {
                if (alerta.getProducto() == null) {
                    throw new ErrorProcesamientoLoteException(
                        "Alerta ID " + alerta.getAlertaId() + " no tiene producto asociado"
                    );
                }
            });

            // 3. Configurar thread pool para procesamiento paralelo
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            // 4. Ejecutar predicciones en paralelo
            List<CompletableFuture<ResultadoPrediccion>> futures = alertas.stream()
                .map(alerta -> CompletableFuture.supplyAsync(() -> 
                    procesarPrediccionParaAlerta(alerta, request.getHorizonteTiempo()), 
                    executor
                ))
                .collect(Collectors.toList());

            // 5. Esperar a que todas las predicciones terminen
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.join();

            // 6. Recopilar resultados
            List<ResultadoPrediccion> resultados = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            // 7. Procesar resultados
            for (ResultadoPrediccion resultado : resultados) {
                if (resultado.exitoso) {
                    response.setExitosos(response.getExitosos() + 1);
                    response.getAlertasExitosas().add(resultado.alertaId);
                    if (resultado.prediccionId != null) {
                        response.getPrediccionesGeneradas().add(resultado.prediccionId.longValue());
                    }
                } else {
                    response.setFallidos(response.getFallidos() + 1);
                    response.getAlertasFallidas().add(resultado.alertaId);
                    response.getMensajesError().add(
                        "Alerta ID " + resultado.alertaId + ": " + resultado.mensajeError
                    );
                }
            }

            // 8. Finalizar fase de predicciones
            executor.shutdown();
            
            LocalDateTime fin = LocalDateTime.now();
            response.setFechaFin(fin);
            response.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, fin).toMillis()
            );
            response.setExitoTotal(response.getFallidos() == 0);
            
            String observaciones = String.format(
                "Procesamiento completado: %d exitosos, %d fallidos de %d total. Tiempo: %d ms",
                response.getExitosos(),
                response.getFallidos(),
                response.getTotalProcesadas(),
                response.getTiempoEjecucionMs()
            );
            response.setObservaciones(observaciones);

            log.info(observaciones);
            
            // 9. SI HAY PREDICCIONES EXITOSAS, continuar con optimizacion
            if (!response.getPrediccionesGeneradas().isEmpty()) {
                log.info("Iniciando optimizacion para {} predicciones exitosas", 
                    response.getPrediccionesGeneradas().size());
                
                try {
                    // Convertir Long a Integer para las predicciones
                    List<Integer> prediccionIds = response.getPrediccionesGeneradas().stream()
                        .map(Long::intValue)
                        .collect(Collectors.toList());
                    
                    // Ejecutar optimizaciones EOQ/ROP con nivel de servicio 95%
                    ProcesamientoBatchResponse optimizacionResponse = 
                        optimizacionBatchService.ejecutarOptimizacionesBatch(prediccionIds, 0.95);
                    
                    // Copiar resultados de optimizacion
                    response.getOptimizacionesGeneradas().addAll(
                        optimizacionResponse.getOptimizacionesGeneradas()
                    );
                    
                    log.info("Optimizaciones completadas: {} calculadas", 
                        response.getOptimizacionesGeneradas().size());
                    
                    // 10. SI HAY OPTIMIZACIONES EXITOSAS, generar ordenes de compra
                    if (!response.getOptimizacionesGeneradas().isEmpty()) {
                        log.info("Generando ordenes de compra para {} alertas", 
                            request.getAlertaIds().size());
                        
                        ProcesamientoBatchResponse ordenesResponse = 
                            ordenCompraBatchService.generarOrdenesPorProveedor(
                                request.getAlertaIds(), 
                                request.getUsuarioId()
                            );
                        
                        // Copiar resultados de ordenes
                        response.getOrdenesGeneradas().addAll(
                            ordenesResponse.getOrdenesGeneradas()
                        );
                        
                        log.info("Ordenes generadas: {} ordenes", 
                            response.getOrdenesGeneradas().size());
                        
                        // Actualizar observaciones con informacion completa
                        observaciones = String.format(
                            "Procesamiento completo: %d alertas, %d predicciones, %d optimizaciones, %d ordenes generadas",
                            response.getExitosos(),
                            response.getPrediccionesGeneradas().size(),
                            response.getOptimizacionesGeneradas().size(),
                            response.getOrdenesGeneradas().size()
                        );
                        response.setObservaciones(observaciones);
                        
                        log.info("Flujo completo finalizado exitosamente");
                    } else {
                        log.warn("No se generaron optimizaciones, no se pueden crear ordenes");
                        response.getMensajesError().add(
                            "No se generaron optimizaciones EOQ/ROP para crear órdenes de compra"
                        );
                    }
                    
                } catch (Exception e) {
                    log.error("Error en optimizacion/generacion de ordenes", e);
                    response.getMensajesError().add(
                        "Error en optimización/órdenes: " + e.getMessage()
                    );
                    response.setExitoTotal(false);
                }
            } else {
                log.warn("No se generaron predicciones exitosas, no se pueden crear ordenes");
                response.getMensajesError().add(
                    "No se generaron predicciones exitosas"
                );
            }
            
            return response;

        } catch (Exception e) {
            log.error("Error en procesamiento batch de predicciones", e);
            
            LocalDateTime fin = LocalDateTime.now();
            response.setFechaFin(fin);
            response.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, fin).toMillis()
            );
            response.setFallidos(request.getAlertaIds().size());
            response.setExitoTotal(false);
            response.getMensajesError().add("Error general: " + e.getMessage());
            
            throw new ErrorProcesamientoLoteException(
                "Error al procesar predicciones en lote", e
            );
        }
    }

    /**
     * Procesa una prediccion para una alerta individual.
     * 
     * FLUJO COMPLETO:
     * 1. Analiza estacionalidad del producto (kardex 12 meses)
     * 2. Calcula horizonte óptimo con autocorrelación SMILE ML
     * 3. Ejecuta predicción ML (modo automático)
     * 
     * @param alerta Alerta a procesar
     * @param horizonteTiempo Horizonte de tiempo en dias (null = automatico)
     * @return Resultado del procesamiento
     */
    private ResultadoPrediccion procesarPrediccionParaAlerta(
            AlertaInventario alerta, 
            Integer horizonteTiempo) {
        
        ResultadoPrediccion resultado = new ResultadoPrediccion();
        resultado.alertaId = alerta.getAlertaId();
        resultado.productoId = alerta.getProducto().getProductoId();
        
        try {
            log.debug("Procesando alerta ID: {}, producto: {}", 
                alerta.getAlertaId(), 
                alerta.getProducto().getNombre()
            );

            // PASO 1: Análisis de estacionalidad (guarda en BD automáticamente)
            try {
                log.debug("[ESTACIONALIDAD] Analizando estacionalidad para producto ID: {}", 
                    resultado.productoId);
                
                estacionalidadService.analizarYGuardar(resultado.productoId.longValue());
                
                log.debug("[ESTACIONALIDAD] Estacionalidad analizada para producto ID: {}", 
                    resultado.productoId);
            } catch (Exception e) {
                log.warn("[ESTACIONALIDAD] Advertencia: No se pudo analizar estacionalidad para producto ID: {} - {}", 
                    resultado.productoId, e.getMessage());
                // Continuar con predicción aunque falle estacionalidad
            }

            // PASO 2: Calcular horizonte óptimo usando métodos combinados
            int dias;
            if (horizonteTiempo == null) {
                // Primero intentar método de rotación empresarial
                dias = horizonteService.calcularHorizonteOptimo(alerta);
                
                log.info("[HORIZONTE] Horizonte calculado con método de rotación para producto ID {}: {} días ({} meses)", 
                    resultado.productoId, dias, dias / 30);
                
                // Opcionalmente, podríamos usar autocorrelación para refinar
                // (comentado por ahora, pero disponible para casos específicos)
                // double[] serie = obtenerSerieHistorica(alerta.getProducto());
                // if (serie != null && serie.length >= 14) {
                //     int horizonteML = horizonteService.calcularHorizonteConAutocorrelacion(serie);
                //     log.info("[HORIZONTE] Horizonte con ML (autocorrelación): {} períodos", horizonteML);
                // }
                
            } else {
                dias = horizonteTiempo;
                log.debug("[HORIZONTE] Usando horizonte proporcionado: {} días", dias);
            }

            // PASO 3: Ejecutar prediccion ML (modo automatico con SMILE ML)
            log.info("[PREDICCION] Ejecutando predicción inteligente (SMILE ML) para producto ID: {}", 
                resultado.productoId);
            
            // Obtener producto
            Producto producto = alerta.getProducto();
            
            // Crear request para SmartPredictor con algoritmo AUTO (selección automática)
            SmartPrediccionRequest smartRequest = new SmartPrediccionRequest();
            smartRequest.setIdProducto(producto.getProductoId().longValue());
            smartRequest.setHorizonteTiempo(dias);
            smartRequest.setAlgoritmoSeleccionado("AUTO");
            smartRequest.setDetectarEstacionalidad(true);
            
            // Ejecutar predicción con SMILE ML (mejor algoritmo: OLS, RandomForest, GBM)
            SmartPrediccionResponse smartResponse = smartPredictorService
                .generarPrediccionInteligente(smartRequest);

            Integer prediccionId = null;
            if (smartResponse.getIdPrediccion() != null) {
                prediccionId = Math.toIntExact(smartResponse.getIdPrediccion());
            }

            resultado.exitoso = true;
            resultado.prediccionId = prediccionId;
            resultado.smartResponse = smartResponse; // Guardar respuesta completa para usar en DTO

            // Generar SKU dinámicamente y log
            String sku = SKUGenerator.generarSKU(producto);

            log.info("[PREDICCION] Predicción SMILE ML exitosa | Alerta: {} | Predicción: {} | Producto: {} (SKU: {}) | Algoritmo: {} | MAPE: {:.2f}%", 
                alerta.getAlertaId(), 
                prediccionId,
                producto.getNombre(),
                sku,
                smartResponse.getAlgoritmoUtilizado(),
                smartResponse.getMetricas() != null ? smartResponse.getMetricas().getMape() : 0.0
            );

        } catch (Exception e) {
            log.error("[PREDICCION] Error al procesar alerta ID: {}", 
                alerta.getAlertaId(), e);
            
            resultado.exitoso = false;
            resultado.mensajeError = e.getMessage();
        }
        
        return resultado;
    }

    /**
     * Clase interna para almacenar resultado de una prediccion.
     */
    private static class ResultadoPrediccion {
        Long alertaId;
        Integer productoId;
        Integer prediccionId;
        boolean exitoso;
        String mensajeError;
        SmartPrediccionResponse smartResponse; // Guardar respuesta completa de SMILE ML
    }

    // ============================================
    // MÉTODO AGRUPADO POR PROVEEDOR (FASE 2)
    // ============================================

    /**
     * Procesa alertas y agrupa predicciones por proveedor.
     * 
     * Retorna una estructura completa con:
     * - Predicciones por producto
     * - Datos históricos y predichos para gráficos
     * - Métricas de calidad (MAE, MAPE, RMSE)
     * - Métricas agregadas por proveedor
     * 
     * @param alertaIds IDs de las alertas a procesar
     * @param horizonteTiempo Horizonte en días (null = automático)
     * @return Map con proveedorId como clave y resumen de predicciones como valor
     */
    @Transactional
    public java.util.Map<Long, com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenPrediccionPorProveedor> 
            procesarAlertasAgrupadoPorProveedor(List<Long> alertaIds, Integer horizonteTiempo) {
        
        log.info("Iniciando procesamiento agrupado por proveedor para {} alertas", alertaIds.size());
        
        // 1. Obtener alertas
        List<AlertaInventario> alertas = alertaRepositorio.findAllById(alertaIds);
        
        if (alertas.isEmpty()) {
            throw new ErrorProcesamientoLoteException(
                "No se encontraron alertas con los IDs proporcionados"
            );
        }

        // 2. Agrupar alertas por proveedor
        java.util.Map<Long, List<AlertaInventario>> alertasPorProveedor = new java.util.HashMap<>();
        
        for (AlertaInventario alerta : alertas) {
            if (alerta.getProducto() == null || alerta.getProducto().getProveedorPrincipal() == null) {
                log.warn("Alerta {} sin proveedor, omitiendo", alerta.getAlertaId());
                continue;
            }
            
            Long proveedorId = alerta.getProducto().getProveedorPrincipal().getProveedorId().longValue();
            alertasPorProveedor
                .computeIfAbsent(proveedorId, k -> new ArrayList<>())
                .add(alerta);
        }

        // 3. Procesar cada proveedor
        java.util.Map<Long, com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenPrediccionPorProveedor> resultado = new java.util.HashMap<>();
        
        for (java.util.Map.Entry<Long, List<AlertaInventario>> entry : alertasPorProveedor.entrySet()) {
            Long proveedorId = entry.getKey();
            List<AlertaInventario> alertasProveedor = entry.getValue();
            
            log.info("Procesando proveedor ID {}: {} alertas", proveedorId, alertasProveedor.size());
            
            // Procesar predicciones para este proveedor
            com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenPrediccionPorProveedor resumen = 
                procesarProveedorCompleto(proveedorId, alertasProveedor, horizonteTiempo);
            
            resultado.put(proveedorId, resumen);
        }

        log.info("Procesamiento completado: {} proveedores procesados", resultado.size());
        
        return resultado;
    }

    /**
     * Procesa todas las alertas de un proveedor y genera el resumen completo.
     */
    private com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenPrediccionPorProveedor 
            procesarProveedorCompleto(Long proveedorId, List<AlertaInventario> alertas, Integer horizonteTiempo) {
        
        Proveedor proveedor = alertas.get(0).getProducto().getProveedorPrincipal();
        
        List<com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.PrediccionProductoDTO> predicciones = new ArrayList<>();
        int exitosas = 0;
        int fallidas = 0;
        
        // Procesar cada alerta
        for (AlertaInventario alerta : alertas) {
            try {
                // Generar predicción
                ResultadoPrediccion resultado = procesarPrediccionParaAlerta(alerta, horizonteTiempo);
                
                if (resultado.exitoso && resultado.prediccionId != null && resultado.smartResponse != null) {
                    // Convertir SmartResponse directamente a DTO (sin consultar BD)
                    com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.PrediccionProductoDTO dto = 
                        mapearSmartResponseADTO(resultado.smartResponse, alerta);
                    
                    predicciones.add(dto);
                    exitosas++;
                } else {
                    fallidas++;
                }
                
            } catch (Exception e) {
                log.error("Error procesando alerta {}: {}", alerta.getAlertaId(), e.getMessage());
                fallidas++;
            }
        }

        // Calcular métricas agregadas
        com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.MetricasAgregadasDTO metricas = 
            calcularMetricasAgregadas(predicciones);

        // Construir resumen
        return com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ResumenPrediccionPorProveedor.builder()
            .proveedorId(proveedorId)
            .nombreProveedor(proveedor.getNombreComercial())
            .rucProveedor(proveedor.getRucNit())
            .contactoProveedor(proveedor.getPersonaContacto())
            .emailProveedor(proveedor.getEmail())
            .telefonoProveedor(proveedor.getTelefono())
            .predicciones(predicciones)
            .metricas(metricas)
            .totalAlertas(alertas.size())
            .prediccionesExitosas(exitosas)
            .prediccionesFallidas(fallidas)
            .build();
    }

    /**
     * Mapea directamente SmartPrediccionResponse a DTO con datos completos.
     * 
     * Extrae datos históricos, predicciones y métricas desde la respuesta de SMILE ML
     * sin necesidad de consultar la base de datos.
     * 
     * @param smartResponse Respuesta del servicio SMILE ML
     * @param alerta Alerta asociada (para obtener datos del producto)
     * @return DTO con toda la información de la predicción
     */
    private com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.PrediccionProductoDTO 
            mapearSmartResponseADTO(SmartPrediccionResponse smartResponse, AlertaInventario alerta) {
        
        Producto producto = alerta.getProducto();
        
        // Extraer valores predichos desde SmartPrediccionResponse
        List<Double> valoresPredichos = new ArrayList<>();
        List<String> fechasPredichas = new ArrayList<>();
        
        if (smartResponse.getPrediccionesDetalladas() != null) {
            for (SmartPrediccionResponse.PrediccionDetalle detalle : smartResponse.getPrediccionesDetalladas()) {
                valoresPredichos.add(detalle.getDemandaPredicha());
                fechasPredichas.add(detalle.getFecha());
            }
        }
        
        // Extraer valores históricos desde RegistroDemanda
        List<Double> valoresHistoricos = new ArrayList<>();
        List<String> fechasHistoricas = new ArrayList<>();
        
        try {
            List<RegistroDemanda> registrosHistoricos = registroDemandaRepositorio.findByProducto(producto);
            
            if (registrosHistoricos != null && !registrosHistoricos.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                for (RegistroDemanda registro : registrosHistoricos) {
                    if (registro.getCantidadHistorica() != null && registro.getFechaRegistro() != null) {
                        valoresHistoricos.add(registro.getCantidadHistorica().doubleValue());
                        fechasHistoricas.add(registro.getFechaRegistro().format(formatter));
                    }
                }
                
                log.debug("[HISTORIAL] Extraídos {} registros históricos para producto {}", 
                    valoresHistoricos.size(), producto.getNombre());
            } else {
                log.warn("[HISTORIAL] Advertencia: No se encontraron registros históricos para producto {}", producto.getNombre());
            }
        } catch (Exception e) {
            log.error("[HISTORIAL] Error al extraer valores históricos para producto {}: {}", 
                producto.getNombre(), e.getMessage());
        }
        
        // Extraer métricas desde SmartPrediccionResponse
        Double mae = null;
        Double mape = null;
        Double rmse = null;
        String calidad = "DESCONOCIDA";
        
        if (smartResponse.getMetricas() != null) {
            mae = smartResponse.getMetricas().getMae();
            mape = smartResponse.getMetricas().getMape();
            rmse = smartResponse.getMetricas().getRmse();
            calidad = smartResponse.getMetricas().getCalificacionCalidad();
        }
        
        // Generar SKU dinámicamente
        String sku = SKUGenerator.generarSKU(producto);
        
        // Extraer información de estacionalidad
        Boolean tieneEstacionalidad = null;
        if (smartResponse.getEstacionalidad() != null) {
            tieneEstacionalidad = smartResponse.getEstacionalidad().getTieneEstacionalidad();
        }
        
        return com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.PrediccionProductoDTO.builder()
            .productoId(producto.getProductoId())
            .nombreProducto(producto.getNombre())
            .codigoSKU(sku)
            .prediccionId(smartResponse.getIdPrediccion() != null ? smartResponse.getIdPrediccion().intValue() : null)
            .valoresHistoricos(valoresHistoricos)
            .valoresPredichos(valoresPredichos)
            .fechasHistoricas(fechasHistoricas)
            .fechasPredichas(fechasPredichas)
            .mae(mae)
            .mape(mape)
            .rmse(rmse)
            .calidadPrediccion(calidad)
            .horizonteUsado(smartResponse.getHorizonteTiempo())
            .algoritmoUsado(smartResponse.getAlgoritmoUtilizado())
            .tieneEstacionalidad(tieneEstacionalidad)
            .advertencias(new ArrayList<>())
            .recomendaciones(smartResponse.getRecomendaciones() != null ? smartResponse.getRecomendaciones() : new ArrayList<>())
            .build();
    }

    /**
     * Calcula métricas agregadas para un conjunto de predicciones.
     */
    private com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.MetricasAgregadasDTO 
            calcularMetricasAgregadas(List<com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.PrediccionProductoDTO> predicciones) {
        
        if (predicciones.isEmpty()) {
            return com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.MetricasAgregadasDTO.builder()
                .totalProductos(0)
                .calidadGeneral("SIN_DATOS")
                .build();
        }

        // Calcular promedios
        double sumaMae = 0.0;
        double sumaMape = 0.0;
        int excelentes = 0;
        int buenas = 0;
        int regulares = 0;
        int malas = 0;
        
        for (com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.PrediccionProductoDTO pred : predicciones) {
            if (pred.getMae() != null) {
                sumaMae += pred.getMae();
            }
            if (pred.getMape() != null) {
                sumaMape += pred.getMape();
                
                // Clasificar calidad
                if (pred.getMape() < 10) excelentes++;
                else if (pred.getMape() < 20) buenas++;
                else if (pred.getMape() < 50) regulares++;
                else malas++;
            }
        }

        double maePromedio = sumaMae / predicciones.size();
        double mapePromedio = sumaMape / predicciones.size();
        
        String calidadGeneral = determinarCalidad(mapePromedio);
        
        double porcentajeAceptable = ((double) (excelentes + buenas) / predicciones.size()) * 100;

        return com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.MetricasAgregadasDTO.builder()
            .maePromedio(maePromedio)
            .mapePromedio(mapePromedio)
            .calidadGeneral(calidadGeneral)
            .totalProductos(predicciones.size())
            .prediccionesExcelentes(excelentes)
            .prediccionesBuenas(buenas)
            .prediccionesRegulares(regulares)
            .prediccionesMalas(malas)
            .porcentajeAceptable(porcentajeAceptable)
            .build();
    }

    /**
     * Convierte una respuesta de SmartPredictor a una entidad Prediccion tradicional.
     * 
     * Esto permite mantener compatibilidad con el sistema existente mientras
     * usamos los algoritmos mejorados de SMILE ML en el backend.
     * 
     * @param smartResponse Respuesta del servicio SmartPredictor (SMILE ML)
     * @param productoId ID del producto
     * @return Prediccion entidad compatible con el sistema
     */
    /**
     * Determina la calidad de una predicción basada en MAPE.
     */
    private String determinarCalidad(Double mape) {
        if (mape == null) return "DESCONOCIDA";
        if (mape < 10) return "EXCELENTE";
        if (mape < 20) return "BUENA";
        if (mape < 50) return "REGULAR";
        return "MALA";
    }
}
