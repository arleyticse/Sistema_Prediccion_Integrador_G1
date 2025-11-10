package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.ProcesarAlertasRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.ProcesamientoBatchResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.ErrorProcesamientoLoteException;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository.IAlertaInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service.PrediccionService;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import com.prediccion.apppredicciongm.models.Prediccion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private final PrediccionService prediccionService;
    private final IAlertaInventarioRepositorio alertaRepositorio;
    private final IOptimizacionBatchService optimizacionBatchService;
    private final IOrdenCompraBatchService ordenCompraBatchService;
    
    private static final int THREAD_POOL_SIZE = 5;
    private static final int HORIZONTE_TIEMPO_DEFAULT = 365; // 12 meses

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
     * @param alerta Alerta a procesar
     * @param horizonteTiempo Horizonte de tiempo en dias
     * @return Resultado del procesamiento
     */
    private ResultadoPrediccion procesarPrediccionParaAlerta(
            AlertaInventario alerta, 
            Integer horizonteTiempo) {
        
        ResultadoPrediccion resultado = new ResultadoPrediccion();
        resultado.alertaId = alerta.getAlertaId();
        resultado.productoId = alerta.getProducto().getProductoId();
        
        try {
            log.debug("Procesando prediccion para alerta ID: {}, producto: {}", 
                alerta.getAlertaId(), 
                alerta.getProducto().getNombre()
            );

            // Usar horizonte por defecto si no se proporciono
            int dias = horizonteTiempo != null ? horizonteTiempo : HORIZONTE_TIEMPO_DEFAULT;

            // Ejecutar prediccion (modo automatico)
            Prediccion prediccion = prediccionService.generarPrediccion(
                alerta.getProducto().getProductoId(), 
                dias
            );

            resultado.exitoso = true;
            resultado.prediccionId = prediccion.getPrediccionId();
            
            log.debug("Prediccion exitosa para alerta ID: {}, prediccion ID: {}", 
                alerta.getAlertaId(), 
                prediccion.getPrediccionId()
            );

        } catch (Exception e) {
            log.error("Error al procesar prediccion para alerta ID: {}", 
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
    }
}
