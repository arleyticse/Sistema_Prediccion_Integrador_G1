package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.service;

import com.prediccion.apppredicciongm.auth.repository.IUsuarioRepository;
import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.algorithms.IAlgoritmoPrediccion;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.ResultadoPrediccionDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.Prediccion;
import com.prediccion.apppredicciongm.models.Usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de predicción de demanda.
 * Orquesta la ejecución de algoritmos de predicción, gestiona datos históricos
 * del kardex y persiste los resultados en la base de datos.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */
@Service
@Transactional
public class PrediccionServiceImpl implements IPrediccionService {

    private static final Logger logger = LoggerFactory.getLogger(PrediccionServiceImpl.class);

    @Autowired
    private IPrediccionRepositorio prediccionRepositorio;

    @Autowired
    private IProductoRepositorio productoRepositorio;

    @Autowired
    private IKardexRepositorio kardexRepositorio;

    @Autowired
    private IUsuarioRepository usuarioRepositorio;

    @Autowired
    private Map<String, IAlgoritmoPrediccion> algoritmos;
    
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Genera una predicción utilizando un algoritmo específico.
     * 
     * @param productoId ID del producto
     * @param codigoAlgoritmo Código del algoritmo (SMA, SES, HOLT_WINTERS)
     * @param horizonteTiempo Número de períodos a predecir
     * @param parametros Parámetros específicos del algoritmo
     * @return Predicción generada y guardada en BD
     * @throws IllegalArgumentException Si el producto no existe o el algoritmo no está disponible
     */
    public Prediccion generarPrediccion(Integer productoId, String codigoAlgoritmo, 
                                       Integer horizonteTiempo, Map<String, Double> parametros) {
        logger.info("Iniciando generacion de prediccion para producto ID: {} con algoritmo: {}", 
                   productoId, codigoAlgoritmo);

        // Validar producto existe
        Producto producto = productoRepositorio.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Producto no encontrado con ID: " + productoId));

        // Validar algoritmo existe
        IAlgoritmoPrediccion algoritmo = algoritmos.get(codigoAlgoritmo);
        if (algoritmo == null) {
            throw new IllegalArgumentException(
                "Algoritmo no disponible: " + codigoAlgoritmo + 
                ". Disponibles: " + algoritmos.keySet());
        }

        // Obtener datos históricos de ventas del kardex
        List<Double> datosHistoricos = obtenerDatosHistoricosVentas(productoId);
        
        logger.info("Datos historicos obtenidos: {} registros de ventas", datosHistoricos.size());

        // Validar que hay suficientes datos
        int minimosDatos = algoritmo.getMinimosDatosRequeridos();
        if (datosHistoricos.size() < minimosDatos) {
            throw new IllegalStateException(
                String.format("Datos insuficientes para el algoritmo %s. " +
                            "Se requieren al menos %d registros, se encontraron %d",
                            algoritmo.getNombreAlgoritmo(), minimosDatos, datosHistoricos.size()));
        }

        // Ejecutar algoritmo de predicción
        if (parametros == null) {
            parametros = new HashMap<>();
        }
        
        logger.debug("Ejecutando algoritmo {} con parametros: {}", 
                    algoritmo.getNombreAlgoritmo(), parametros);
        
        ResultadoPrediccionDTO resultado = algoritmo.predecir(
            datosHistoricos, horizonteTiempo, parametros);

        // Calcular demanda total predicha
        int demandaTotalPredicha = resultado.getValoresPredichos().stream()
            .mapToInt(Double::intValue)
            .sum();

        logger.info("Prediccion completada. Demanda total predicha: {}", demandaTotalPredicha);
        logger.info("Metricas - MAE: {}, MAPE: {}%, RMSE: {}", 
                   resultado.getMae(), resultado.getMape(), resultado.getRmse());
        logger.info("Calidad de prediccion: {}", resultado.getCalidadPrediccion());

        // Crear entidad Prediccion
        Prediccion prediccion = new Prediccion();
        prediccion.setProducto(producto);
        prediccion.setFechaEjecucion(LocalDateTime.now());
        prediccion.setHorizonteTiempo(horizonteTiempo);
        prediccion.setAlgoritmoUsado(resultado.getAlgoritmoUsado());
        prediccion.setDemandaPredichaTotal(demandaTotalPredicha);
        
        // Guardar MAPE como métrica principal de error
        prediccion.setMetricasError(BigDecimal.valueOf(resultado.getMape()));

        // Obtener usuario actual del contexto de seguridad
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepositorio.findByEmail(username).orElse(null);
        if (usuario != null) {
            prediccion.setUsuario(usuario);
        }

        // Guardar predicción en BD
        prediccion = prediccionRepositorio.save(prediccion);
        logger.info("Prediccion guardada exitosamente con ID: {}", prediccion.getPrediccionId());

        // Log de advertencias y recomendaciones
        if (!resultado.getAdvertencias().isEmpty()) {
            logger.info("Advertencias generadas:");
            resultado.getAdvertencias().forEach(adv -> logger.info("  - {}", adv));
        }
        
        if (!resultado.getRecomendaciones().isEmpty()) {
            logger.info("Recomendaciones generadas:");
            resultado.getRecomendaciones().forEach(rec -> logger.info("  - {}", rec));
        }

        return prediccion;
    }

    /**
     * Genera una predicción con parámetros por defecto del algoritmo.
     * 
     * @param productoId ID del producto
     * @param diasProcesar Horizonte de tiempo (días)
     * @return Predicción generada
     */
    @Override
    public Prediccion generarPrediccion(Integer productoId, int diasProcesar) {
        logger.info("Generando prediccion con algoritmo por defecto (SMA) para producto: {}", 
                   productoId);
        
        // Usar SMA como algoritmo por defecto con parámetros estándar
        Map<String, Double> parametros = new HashMap<>();
        parametros.put("ventana", 14.0);
        
        return generarPrediccion(productoId, "SMA", diasProcesar, parametros);
    }

    /**
     * Obtiene datos históricos de ventas de un producto desde el kardex.
     * Filtra solo movimientos de tipo SALIDA_VENTA y extrae las cantidades.
     * 
     * @param productoId ID del producto
     * @return Lista de cantidades vendidas ordenadas cronológicamente
     */
    private List<Double> obtenerDatosHistoricosVentas(Integer productoId) {
        logger.debug("Obteniendo datos historicos de ventas para producto ID: {}", productoId);

        // Obtener todos los movimientos del producto
        Pageable pageable = PageRequest.of(0, 1000); // Limitar a últimos 1000 movimientos
        Page<Kardex> kardexPage = kardexRepositorio.findByProductoAndTipo(
            productoId, TipoMovimiento.SALIDA_VENTA, pageable);

        List<Kardex> movimientosVenta = kardexPage.getContent();
        
        logger.debug("Encontrados {} movimientos de venta", movimientosVenta.size());

        // Extraer cantidades y convertir a Double
        List<Double> ventas = movimientosVenta.stream()
            .filter(k -> !k.isAnulado()) // Excluir movimientos anulados
            .sorted((k1, k2) -> k1.getFechaMovimiento().compareTo(k2.getFechaMovimiento())) // Ordenar cronológicamente
            .map(k -> Double.valueOf(k.getCantidad()))
            .collect(Collectors.toList());

        if (ventas.isEmpty()) {
            logger.warn("No se encontraron movimientos de venta para el producto ID: {}", productoId);
        }

        return ventas;
    }

    /**
     * Obtiene la última predicción realizada para un producto.
     * 
     * @param productoId ID del producto
     * @return Última predicción o null si no existe
     */
    @Override
    public Prediccion obtenerUltimaPrediccion(Integer productoId) {
        logger.debug("Buscando ultima prediccion para producto ID: {}", productoId);

        Producto producto = productoRepositorio.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Producto no encontrado con ID: " + productoId));

        return prediccionRepositorio.findFirstByProductoOrderByFechaEjecucionDesc(producto)
            .orElse(null);
    }

    /**
     * Obtiene todas las predicciones de un producto con paginación.
     * 
     * @param productoId ID del producto
     * @param pageable Configuración de paginación
     * @return Lista de predicciones del producto
     */
    @Override
    public List<Prediccion> obtenerPrediccionesByProducto(Integer productoId, Pageable pageable) {
        logger.debug("Obteniendo predicciones para producto ID: {} con paginacion", productoId);

        Producto producto = productoRepositorio.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Producto no encontrado con ID: " + productoId));

        return prediccionRepositorio.findByProductoOrderByFechaEjecucionDesc(producto);
    }

    /**
     * Obtiene todas las predicciones con paginación.
     * 
     * @param pageable Configuración de paginación
     * @return Página de predicciones
     */
    @Override
    public Page<Prediccion> obtenerTodasLasPredicciones(Pageable pageable) {
        logger.debug("Obteniendo todas las predicciones con paginacion");
        return prediccionRepositorio.findAll(pageable);
    }

    /**
     * Reconstruye una PrediccionResponse completa desde una entidad Prediccion existente.
     * Regenera los datos extendidos (valores predichos, métricas, advertencias) 
     * ejecutando nuevamente el algoritmo con parámetros predeterminados.
     * 
     * @param prediccion Entidad Prediccion de la base de datos
     * @return PrediccionResponse completo con todos los datos extendidos
     */
    public PrediccionResponse reconstruirPrediccionCompleta(Prediccion prediccion) {
        logger.debug("Reconstruyendo prediccion completa para ID: {}", prediccion.getPrediccionId());
        
        try {
            // Obtener datos del producto
            Producto producto = prediccion.getProducto();
            
            // Obtener datos históricos del kardex (solo salidas por venta)
            // Usando paginación grande para obtener todos los movimientos
            Pageable pageable = PageRequest.of(0, 10000);
            Page<Kardex> page = kardexRepositorio.findByProductoAndTipo(
                producto.getProductoId(), 
                TipoMovimiento.SALIDA_VENTA,
                pageable
            );
            List<Kardex> movimientos = page.getContent();
            
            // Convertir a datos históricos (ordenados por fecha)
            List<Double> datosHistoricos = movimientos.stream()
                .sorted((m1, m2) -> m1.getFechaMovimiento().compareTo(m2.getFechaMovimiento()))
                .map(m -> m.getCantidad().doubleValue())
                .collect(Collectors.toList());
            
            if (datosHistoricos.isEmpty()) {
                logger.warn("No hay datos históricos para producto {}, retornando respuesta básica", 
                           producto.getProductoId());
                return crearRespuestaBasica(prediccion);
            }
            
            // Obtener el algoritmo usado
            String codigoAlgoritmo = prediccion.getAlgoritmoUsado();
            
            // Mapear códigos antiguos (SMA, SES, HOLT_WINTERS) a nombres de bean
            String beanName = mapearCodigoABean(codigoAlgoritmo);
            
            Map<String, IAlgoritmoPrediccion> algoritmosMap = applicationContext.getBeansOfType(IAlgoritmoPrediccion.class);
            
            IAlgoritmoPrediccion algoritmo = algoritmosMap.get(beanName);
            
            if (algoritmo == null) {
                logger.warn("Algoritmo {} (bean: {}) no encontrado, retornando respuesta básica", 
                           codigoAlgoritmo, beanName);
                return crearRespuestaBasica(prediccion);
            }
            
            // Usar parámetros predeterminados del algoritmo
            Map<String, Double> parametros = new HashMap<>();
            // Los algoritmos ya tienen sus valores por defecto
            
            // Ejecutar el algoritmo para regenerar los datos
            ResultadoPrediccionDTO resultado = algoritmo.predecir(
                datosHistoricos,
                prediccion.getHorizonteTiempo(),
                parametros
            );
            
            // Crear DTO del producto con datos para optimización
            PrediccionResponse.ProductoBasicoDTO productoDTO = null;
            if (producto != null) {
                productoDTO = PrediccionResponse.ProductoBasicoDTO.builder()
                    .productoId(producto.getProductoId())
                    .nombre(producto.getNombre())
                    .costoAdquisicion(producto.getCostoAdquisicion() != null ? 
                                     producto.getCostoAdquisicion().doubleValue() : null)
                    .costoPedido(producto.getCostoPedido() != null ? 
                                producto.getCostoPedido().doubleValue() : null)
                    .costoMantenimientoAnual(producto.getCostoMantenimientoAnual() != null ? 
                                            producto.getCostoMantenimientoAnual().doubleValue() : null)
                    .diasLeadTime(producto.getDiasLeadTime())
                    .build();
            }
            
            // Construir la respuesta completa
            PrediccionResponse response = PrediccionResponse.builder()
                .prediccionId(prediccion.getPrediccionId().longValue())
                .productoId(producto.getProductoId())
                .productoNombre(producto.getNombre())
                .algoritmo(algoritmo.getNombreAlgoritmo())
                .horizonteTiempo(prediccion.getHorizonteTiempo())
                .precision(prediccion.getMetricasError() != null ? 
                          prediccion.getMetricasError().doubleValue() : null)
                .fechaGeneracion(prediccion.getFechaEjecucion())
                .demandaPredichaTotal(prediccion.getDemandaPredichaTotal())
                .valoresPredichos(resultado.getValoresPredichos())
                .datosHistoricos(resultado.getDatosHistoricos())
                .mae(resultado.getMae())
                .mape(resultado.getMape())
                .rmse(resultado.getRmse())
                .calidadPrediccion(resultado.getCalidadPrediccion())
                .advertencias(resultado.getAdvertencias() != null ? resultado.getAdvertencias() : new ArrayList<>())
                .recomendaciones(resultado.getRecomendaciones() != null ? resultado.getRecomendaciones() : new ArrayList<>())
                .tieneTendencia(resultado.getTieneTendencia())
                .tieneEstacionalidad(resultado.getTieneEstacionalidad())
                .producto(productoDTO)
                .build();
            
            // Establecer el estado de la predicción
            response.establecerEstado(false); // No marcamos como fallida si llegamos aquí
            
            return response;
                
        } catch (Exception e) {
            logger.error("Error al reconstruir prediccion completa: {}", e.getMessage(), e);
            return crearRespuestaBasica(prediccion);
        }
    }
    
    /**
     * Mapea códigos de algoritmo antiguos a nombres de bean de Spring.
     * 
     * @param codigo Código del algoritmo (SMA, SES, HOLT_WINTERS o nombre de bean)
     * @return Nombre del bean de Spring
     */
    private String mapearCodigoABean(String codigo) {
        if (codigo == null) return "";
        
        // Si ya es un nombre de bean válido, retornarlo
        if (codigo.contains("Algorithm")) {
            return codigo;
        }
        
        // Mapear códigos cortos a nombres de bean
        switch (codigo.toUpperCase()) {
            case "SMA":
                return "simpleMovingAverageAlgorithm";
            case "SES":
                return "simpleExponentialSmoothingAlgorithm";
            case "HOLT_WINTERS":
            case "HOLT-WINTERS":
            case "HW":
                return "holtWintersAlgorithm";
            default:
                logger.warn("Código de algoritmo desconocido: {}", codigo);
                return codigo; // Retornar el código original
        }
    }
    
    /**
     * Crea una respuesta básica cuando no se pueden regenerar los datos extendidos.
     * 
     * @param prediccion Entidad Prediccion
     * @return PrediccionResponse con datos básicos
     */
    private PrediccionResponse crearRespuestaBasica(Prediccion prediccion) {
        Producto producto = prediccion.getProducto();
        
        // Crear DTO del producto con datos para optimización
        PrediccionResponse.ProductoBasicoDTO productoDTO = null;
        if (producto != null) {
            productoDTO = PrediccionResponse.ProductoBasicoDTO.builder()
                .productoId(producto.getProductoId())
                .nombre(producto.getNombre())
                .costoAdquisicion(producto.getCostoAdquisicion() != null ? 
                                 producto.getCostoAdquisicion().doubleValue() : null)
                .costoPedido(producto.getCostoPedido() != null ? 
                            producto.getCostoPedido().doubleValue() : null)
                .costoMantenimientoAnual(producto.getCostoMantenimientoAnual() != null ? 
                                        producto.getCostoMantenimientoAnual().doubleValue() : null)
                .diasLeadTime(producto.getDiasLeadTime())
                .build();
        }
        
        PrediccionResponse response = PrediccionResponse.builder()
            .prediccionId(prediccion.getPrediccionId().longValue())
            .productoId(producto.getProductoId())
            .productoNombre(producto.getNombre())
            .algoritmo(prediccion.getAlgoritmoUsado())
            .horizonteTiempo(prediccion.getHorizonteTiempo())
            .precision(prediccion.getMetricasError() != null ? 
                      prediccion.getMetricasError().doubleValue() : null)
            .fechaGeneracion(prediccion.getFechaEjecucion())
            .demandaPredichaTotal(prediccion.getDemandaPredichaTotal())
            .valoresPredichos(new ArrayList<>())
            .datosHistoricos(new ArrayList<>())
            .advertencias(new ArrayList<>())
            .recomendaciones(new ArrayList<>())
            .producto(productoDTO)
            .build();
        
        // Establecer el estado de la predicción
        response.establecerEstado(false);
        
        return response;
    }

    /**
     * Actualiza la precisión de una predicción existente.
     * 
     * @param prediccionId ID de la predicción
     * @param nuevaPrecision Nueva precisión (MAPE)
     */
    @Override
    public void actualizarPrecision(Long prediccionId, double nuevaPrecision) {
        logger.info("Actualizando precision de prediccion ID: {} a {}", prediccionId, nuevaPrecision);

        Prediccion prediccion = prediccionRepositorio.findById(prediccionId.intValue())
            .orElseThrow(() -> new IllegalArgumentException(
                "Prediccion no encontrada con ID: " + prediccionId));

        prediccion.setMetricasError(BigDecimal.valueOf(nuevaPrecision));
        prediccionRepositorio.save(prediccion);

        logger.info("Precision actualizada exitosamente");
    }

    /**
     * Elimina una predicción de la base de datos.
     * 
     * @param prediccionId ID de la predicción
     */
    @Override
    public void eliminarPrediccion(Long prediccionId) {
        logger.info("Eliminando prediccion ID: {}", prediccionId);

        if (!prediccionRepositorio.existsById(prediccionId.intValue())) {
            throw new IllegalArgumentException(
                "Prediccion no encontrada con ID: " + prediccionId);
        }

        prediccionRepositorio.deleteById(prediccionId.intValue());
        logger.info("Prediccion eliminada exitosamente");
    }

    /**
     * Obtiene los algoritmos disponibles en el sistema.
     * 
     * @return Mapa de algoritmos con su código como clave
     */
    public Map<String, String> obtenerAlgoritmosDisponibles() {
        return algoritmos.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getNombreAlgoritmo()
            ));
    }

    /**
     * Genera una predicción completa con todos los detalles del ResultadoPrediccionDTO.
     * Este método devuelve directamente un PrediccionResponse con métricas, advertencias y recomendaciones.
     * 
     * @param productoId ID del producto
     * @param codigoAlgoritmo Código del algoritmo (simpleMovingAverageAlgorithm, simpleExponentialSmoothingAlgorithm, holtWintersAlgorithm)
     * @param horizonteTiempo Número de períodos a predecir
     * @param parametros Parámetros específicos del algoritmo
     * @return PrediccionResponse completo con todos los detalles
     */
    public com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse generarPrediccionCompleta(
            Integer productoId, String codigoAlgoritmo, Integer horizonteTiempo, Map<String, Double> parametros) {
        
        logger.info("Iniciando generacion de prediccion completa para producto ID: {} con algoritmo: {}", 
                   productoId, codigoAlgoritmo);

        // Validar producto existe
        Producto producto = productoRepositorio.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Producto no encontrado con ID: " + productoId));

        // Validar algoritmo existe
        IAlgoritmoPrediccion algoritmo = algoritmos.get(codigoAlgoritmo);
        if (algoritmo == null) {
            throw new IllegalArgumentException(
                "Algoritmo no disponible: " + codigoAlgoritmo + 
                ". Disponibles: " + algoritmos.keySet());
        }

        // Obtener datos históricos de ventas del kardex
        List<Double> datosHistoricos = obtenerDatosHistoricosVentas(productoId);
        
        logger.info("Datos historicos obtenidos: {} registros de ventas", datosHistoricos.size());

        // Validar que hay suficientes datos
        int minimosDatos = algoritmo.getMinimosDatosRequeridos();
        if (datosHistoricos.size() < minimosDatos) {
            throw new IllegalStateException(
                String.format("Datos insuficientes para el algoritmo %s. " +
                            "Se requieren al menos %d registros, se encontraron %d",
                            algoritmo.getNombreAlgoritmo(), minimosDatos, datosHistoricos.size()));
        }

        // Ejecutar algoritmo de predicción
        if (parametros == null) {
            parametros = new HashMap<>();
        }
        
        logger.debug("Ejecutando algoritmo {} con parametros: {}", 
                    algoritmo.getNombreAlgoritmo(), parametros);
        
        ResultadoPrediccionDTO resultado = algoritmo.predecir(
            datosHistoricos, horizonteTiempo, parametros);

        // Calcular demanda total predicha
        int demandaTotalPredicha = resultado.getValoresPredichos().stream()
            .mapToInt(Double::intValue)
            .sum();

        logger.info("Prediccion completada. Demanda total predicha: {}", demandaTotalPredicha);
        logger.info("Metricas - MAE: {}, MAPE: {}%, RMSE: {}", 
                   resultado.getMae(), resultado.getMape(), resultado.getRmse());
        logger.info("Calidad de prediccion: {}", resultado.getCalidadPrediccion());

        // Crear y guardar entidad Prediccion en BD
        Prediccion prediccion = new Prediccion();
        prediccion.setProducto(producto);
        prediccion.setFechaEjecucion(LocalDateTime.now());
        prediccion.setHorizonteTiempo(horizonteTiempo);
        prediccion.setAlgoritmoUsado(resultado.getAlgoritmoUsado());
        prediccion.setDemandaPredichaTotal(demandaTotalPredicha);
        prediccion.setMetricasError(BigDecimal.valueOf(resultado.getMape()));

        // Obtener usuario actual del contexto de seguridad
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepositorio.findByEmail(username).orElse(null);
        if (usuario != null) {
            prediccion.setUsuario(usuario);
        }

        // Guardar predicción en BD
        prediccion = prediccionRepositorio.save(prediccion);
        logger.info("Prediccion guardada exitosamente con ID: {}", prediccion.getPrediccionId());

        // Crear DTO del producto con datos para optimización
        PrediccionResponse.ProductoBasicoDTO productoDTO = PrediccionResponse.ProductoBasicoDTO.builder()
            .productoId(producto.getProductoId())
            .nombre(producto.getNombre())
            .costoAdquisicion(producto.getCostoAdquisicion() != null ? 
                             producto.getCostoAdquisicion().doubleValue() : null)
            .costoPedido(producto.getCostoPedido() != null ? 
                        producto.getCostoPedido().doubleValue() : null)
            .costoMantenimientoAnual(producto.getCostoMantenimientoAnual() != null ? 
                                    producto.getCostoMantenimientoAnual().doubleValue() : null)
            .diasLeadTime(producto.getDiasLeadTime())
            .build();

        // Construir PrediccionResponse completo con todos los datos
        com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse response = 
            com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.PrediccionResponse.builder()
                .prediccionId(prediccion.getPrediccionId().longValue())
                .productoId(producto.getProductoId())
                .productoNombre(producto.getNombre())
                .demandaPredichaTotal(demandaTotalPredicha)
                .algoritmo(resultado.getAlgoritmoUsado())
                .horizonteTiempo(horizonteTiempo)
                .fechaGeneracion(LocalDateTime.now())
                .vigenciaHasta(LocalDateTime.now().plusDays(30))
                // Datos extendidos del resultado
                .valoresPredichos(resultado.getValoresPredichos())
                .datosHistoricos(resultado.getDatosHistoricos())
                .mae(resultado.getMae())
                .mape(resultado.getMape())
                .rmse(resultado.getRmse())
                .calidadPrediccion(resultado.getCalidadPrediccion())
                .advertencias(resultado.getAdvertencias() != null ? resultado.getAdvertencias() : List.of())
                .recomendaciones(resultado.getRecomendaciones() != null ? resultado.getRecomendaciones() : List.of())
                .tieneTendencia(resultado.getTieneTendencia())
                .tieneEstacionalidad(resultado.getTieneEstacionalidad())
                .producto(productoDTO)
                .descripcion(String.format("Predicción generada con %s para %d períodos", 
                                          resultado.getNombreAlgoritmo(), horizonteTiempo))
                .build();

        // Establecer el estado de la predicción (exitosa)
        response.establecerEstado(false);

        // Log de advertencias y recomendaciones
        if (!resultado.getAdvertencias().isEmpty()) {
            logger.info("Advertencias generadas:");
            resultado.getAdvertencias().forEach(adv -> logger.info("  - {}", adv));
        }
        
        if (!resultado.getRecomendaciones().isEmpty()) {
            logger.info("Recomendaciones generadas:");
            resultado.getRecomendaciones().forEach(rec -> logger.info("  - {}", rec));
        }

        return response;
    }
}
