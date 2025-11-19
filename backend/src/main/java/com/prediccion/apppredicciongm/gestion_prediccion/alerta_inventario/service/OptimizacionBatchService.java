package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.ErrorProcesamientoLoteException;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoOptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service.IOptimizacionInventarioService;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.SmartPrediccionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.models.Prediccion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Implementacion del servicio de procesamiento batch de optimizaciones.
 * 
 * Calcula EOQ/ROP para multiples productos en paralelo,
 * optimizando el tiempo de procesamiento.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OptimizacionBatchService implements IOptimizacionBatchService {

    private final IOptimizacionInventarioService optimizacionService;
    private final IPrediccionRepositorio prediccionRepositorio;
    
    private static final int THREAD_POOL_SIZE = 5;
    private static final Double NIVEL_SERVICIO_DEFAULT = 0.95; // 95%

    @Override
    @Transactional
    public ProcesamientoBatchResponse ejecutarOptimizacionesBatch(
            List<Integer> prediccionIds, 
            Double nivelServicio) {
        
        log.info("Iniciando procesamiento batch de {} optimizaciones", prediccionIds.size());
        
        LocalDateTime inicio = LocalDateTime.now();
        ProcesamientoBatchResponse response = ProcesamientoBatchResponse.builder()
            .fechaInicio(inicio)
            .totalProcesadas(prediccionIds.size())
            .exitosos(0)
            .fallidos(0)
            .exitoTotal(false)
            .build();

        try {
            // 1. Validar nivel de servicio
            Double nivelServicioFinal = nivelServicio != null ? nivelServicio : NIVEL_SERVICIO_DEFAULT;
            
            if (nivelServicioFinal < 0.80 || nivelServicioFinal > 0.99) {
                throw new ErrorProcesamientoLoteException(
                    "Nivel de servicio debe estar entre 0.80 y 0.99"
                );
            }

            // 2. Configurar thread pool
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            // 3. Ejecutar optimizaciones en paralelo
            List<CompletableFuture<ResultadoOptimizacion>> futures = prediccionIds.stream()
                .map(prediccionId -> CompletableFuture.supplyAsync(() -> 
                    procesarOptimizacionParaPrediccion(prediccionId, nivelServicioFinal), 
                    executor
                ))
                .collect(Collectors.toList());

            // 4. Esperar a que todas terminen
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.join();

            // 5. Recopilar resultados
            List<ResultadoOptimizacion> resultados = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            // 6. Procesar resultados
            for (ResultadoOptimizacion resultado : resultados) {
                if (resultado.exitoso) {
                    response.setExitosos(response.getExitosos() + 1);
                    response.getAlertasExitosas().add(resultado.prediccionId.longValue());
                    if (resultado.calculoId != null) {
                        response.getOptimizacionesGeneradas().add(resultado.calculoId.longValue());
                    }
                } else {
                    response.setFallidos(response.getFallidos() + 1);
                    response.getAlertasFallidas().add(resultado.prediccionId.longValue());
                    response.getMensajesError().add(
                        "Prediccion ID " + resultado.prediccionId + ": " + resultado.mensajeError
                    );
                }
            }

            // 7. Finalizar
            executor.shutdown();
            
            LocalDateTime fin = LocalDateTime.now();
            response.setFechaFin(fin);
            response.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, fin).toMillis()
            );
            response.setExitoTotal(response.getFallidos() == 0);
            
            String observaciones = String.format(
                "Optimizaciones completadas: %d exitosas, %d fallidas de %d total. Tiempo: %d ms",
                response.getExitosos(),
                response.getFallidos(),
                response.getTotalProcesadas(),
                response.getTiempoEjecucionMs()
            );
            response.setObservaciones(observaciones);

            log.info(observaciones);
            return response;

        } catch (Exception e) {
            log.error("Error en procesamiento batch de optimizaciones", e);
            
            LocalDateTime fin = LocalDateTime.now();
            response.setFechaFin(fin);
            response.setTiempoEjecucionMs(
                java.time.Duration.between(inicio, fin).toMillis()
            );
            response.setFallidos(prediccionIds.size());
            response.setExitoTotal(false);
            response.getMensajesError().add("Error general: " + e.getMessage());
            
            throw new ErrorProcesamientoLoteException(
                "Error al procesar optimizaciones en lote", e
            );
        }
    }

    @Override
    public CalculoOptimizacionResponse ejecutarOptimizacionIndividual(
            Integer prediccionId, 
            Double nivelServicio) {
        
        log.info("Ejecutando optimizacion para prediccion ID: {}", prediccionId);
        
        Prediccion prediccion = prediccionRepositorio.findById(prediccionId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Prediccion no encontrada: " + prediccionId
            ));




                double nivelServicioFinal = nivelServicio != null ? nivelServicio : NIVEL_SERVICIO_DEFAULT;

                SmartPrediccionResponse smartResponse = construirSmartResponse(prediccion);
                smartResponse.setMetadatos(Map.of("nivelServicioOverride", nivelServicioFinal));

                return optimizacionService.calcularEOQROPDesdePrediccion(
                    smartResponse,
                    prediccion.getProducto().getProductoId().longValue()
                );
    }

            private SmartPrediccionResponse construirSmartResponse(Prediccion prediccion) {
                Integer horizonte = prediccion.getHorizonteTiempo() != null
                    ? prediccion.getHorizonteTiempo()
                    : 30;

                Double demandaTotal = prediccion.getDemandaPredichaTotal() != null
                    ? prediccion.getDemandaPredichaTotal().doubleValue()
                    : 0.0;

                SmartPrediccionResponse.SmartPrediccionResponseBuilder builder = SmartPrediccionResponse.builder()
                    .idPrediccion(prediccion.getPrediccionId() != null ? prediccion.getPrediccionId().longValue() : null)
                    .idProducto(prediccion.getProducto() != null ? prediccion.getProducto().getProductoId().longValue() : null)
                    .nombreProducto(prediccion.getProducto() != null ? prediccion.getProducto().getNombre() : null)
                    .fechaEjecucion(prediccion.getFechaEjecucion())
                    .horizonteTiempo(horizonte)
                    .algoritmoUtilizado(prediccion.getAlgoritmoUsado())
                    .demandaTotalPredicha(demandaTotal);

                if (prediccion.getMetricasError() != null) {
                    builder.metricas(SmartPrediccionResponse.MetricasCalidad.builder()
                        .mape(prediccion.getMetricasError().doubleValue())
                        .build());
                }

                return builder.build();
            }

    /**
     * Procesa una optimizacion para una prediccion individual.
     * 
     * @param prediccionId ID de la prediccion
     * @param nivelServicio Nivel de servicio deseado
     * @return Resultado del procesamiento
     */
    private ResultadoOptimizacion procesarOptimizacionParaPrediccion(
            Integer prediccionId, 
            Double nivelServicio) {
        
        ResultadoOptimizacion resultado = new ResultadoOptimizacion();
        resultado.prediccionId = prediccionId;
        
        try {
            log.debug("Procesando optimizacion para prediccion ID: {}", prediccionId);

            // Validar que la prediccion existe
            Prediccion prediccion = prediccionRepositorio.findById(prediccionId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Prediccion no encontrada: " + prediccionId
                ));

            double nivelServicioFinal = nivelServicio != null ? nivelServicio : NIVEL_SERVICIO_DEFAULT;

            SmartPrediccionResponse smartResponse = construirSmartResponse(prediccion);
            smartResponse.setMetadatos(Map.of("nivelServicioOverride", nivelServicioFinal));

            CalculoOptimizacionResponse calculo = optimizacionService.calcularEOQROPDesdePrediccion(
                smartResponse,
                prediccion.getProducto().getProductoId().longValue()
            );

            resultado.exitoso = calculo != null;
            if (calculo != null) {
                resultado.calculoId = calculo.getCalculoId();
                resultado.eoq = calculo.getEoqCantidadOptima() != null
                    ? calculo.getEoqCantidadOptima().doubleValue()
                    : null;
                resultado.rop = calculo.getRopPuntoReorden() != null
                    ? calculo.getRopPuntoReorden().doubleValue()
                    : null;
            }
            
            log.debug("Optimizacion {} para prediccion ID: {}, EOQ: {}, ROP: {}", 
                resultado.exitoso ? "exitosa" : "sin resultados",
                prediccionId,
                resultado.eoq,
                resultado.rop);

        } catch (Exception e) {
            log.error("Error al procesar optimizacion para prediccion ID: {}", 
                prediccionId, e);
            
            resultado.exitoso = false;
            resultado.mensajeError = e.getMessage();
        }
        
        return resultado;
    }

    /**
     * Clase interna para almacenar resultado de una optimizacion.
     */
    private static class ResultadoOptimizacion {
        Integer prediccionId;
        Integer calculoId;
        Double eoq;
        Double rop;
        boolean exitoso;
        String mensajeError;
    }
}
