package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.algorithms;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.ResultadoPrediccionDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementación del algoritmo de Promedio Móvil Simple (SMA - Simple Moving Average).
 *
 * Este algoritmo calcula la predicción como el promedio de las últimas N observaciones,
 * donde N es el tamaño de la ventana especificada. Asigna el mismo peso a todas las observaciones,
 * por lo que es ideal para productos con demanda estable y sin tendencias marcadas.
 *
 * Características:
 * - Simplicidad y facilidad de interpretación
 * - Bajo costo computacional
 * - Suaviza fluctuaciones aleatorias
 * - No detecta tendencias ni estacionalidad
 * - Responde lentamente a cambios repentinos en la demanda
 *
 * Uso recomendado:
 * - Productos con demanda constante (arroz, azúcar, aceite)
 * - Situaciones donde se requiere una predicción rápida y sencilla
 * - Cuando no hay patrones claros ni estacionales
 *
 * Parámetros requeridos:
 * - ventana: Número de períodos históricos a promediar (típicamente 7, 14 o 30)
 *
 * Fórmula:
 * Predicción(t+1) = (Venta(t) + Venta(t-1) + ... + Venta(t-n+1)) / n
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */

@Component
public class SimpleMovingAverageAlgorithm extends AlgoritmoPrediccionBase {
    
    private static final String CODIGO = "SMA";
    private static final String NOMBRE = "Promedio Movil Simple";
    private static final int MINIMO_DATOS = 7;
    private static final String PARAMETRO_VENTANA = "ventana";
    private static final int VENTANA_DEFAULT = 14;
    
    @Override
    public ResultadoPrediccionDTO predecir(
            List<Double> datosHistoricos,
            int horizonteTiempo,
            Map<String, Double> parametros) {
        
        logger.info("Iniciando prediccion con {}: {} datos historicos, horizonte {} periodos",
            NOMBRE, datosHistoricos.size(), horizonteTiempo);
        
        // Validar datos
        validarDatos(datosHistoricos, parametros);
        
        // Obtener tamaño de ventana
        int ventana = obtenerVentana(parametros, datosHistoricos.size());
        logger.debug("Usando ventana de {} periodos", ventana);
        
        // Generar predicciones
        List<Double> predicciones = new ArrayList<>();
        List<Double> datosExtendidos = new ArrayList<>(datosHistoricos);
        
        for (int i = 0; i < horizonteTiempo; i++) {
            // Calcular promedio de los últimos N valores
            int inicio = Math.max(0, datosExtendidos.size() - ventana);
            List<Double> ventanaActual = datosExtendidos.subList(inicio, datosExtendidos.size());
            double promedioMovil = calcularPromedio(ventanaActual);
            
            predicciones.add(promedioMovil);
            
            // Agregar la predicción a los datos para el siguiente cálculo
            datosExtendidos.add(promedioMovil);
        }
        
        logger.debug("Generadas {} predicciones. Primera: {}, Ultima: {}",
            predicciones.size(),
            String.format("%.2f", predicciones.get(0)),
            String.format("%.2f", predicciones.get(predicciones.size() - 1)));
        
        // Construir resultado
        ResultadoPrediccionDTO resultado = construirResultado(predicciones, datosHistoricos, parametros);
        
        // Agregar advertencias específicas
        List<String> advertencias = new ArrayList<>();
        if (datosHistoricos.size() < 30) {
            advertencias.add("Datos historicos limitados. Se recomienda al menos 30 observaciones para mayor precision");
        }
        
        // Calcular variabilidad
        double desviacion = calcularDesviacionEstandar(datosHistoricos);
        double promedio = calcularPromedio(datosHistoricos);
        double coeficienteVariacion = (desviacion / promedio) * 100;
        
        if (coeficienteVariacion > 30) {
            advertencias.add(String.format(
                "Alta variabilidad en los datos (CV=%.1f%%). Considere usar algoritmos mas avanzados",
                coeficienteVariacion
            ));
        }
        
        resultado.setAdvertencias(advertencias);
        
        // Generar recomendaciones
        resultado.setRecomendaciones(generarRecomendaciones(datosHistoricos, predicciones));
        
        logger.info("Prediccion {} completada exitosamente", CODIGO);
        
        return resultado;
    }
    
    /**
     * Obtiene el tamaño de ventana de los parámetros o usa el valor por defecto.
     * 
     * @param parametros Mapa de parámetros
     * @param tamanoDatos Tamaño de los datos históricos
     * @return Tamaño de ventana validado
     */
    private int obtenerVentana(Map<String, Double> parametros, int tamanoDatos) {
        int ventana = VENTANA_DEFAULT;
        
        if (parametros != null && parametros.containsKey(PARAMETRO_VENTANA)) {
            ventana = parametros.get(PARAMETRO_VENTANA).intValue();
        }
        
        // Validar que la ventana no sea mayor que los datos disponibles
        if (ventana > tamanoDatos) {
            logger.warn("Ventana solicitada ({}) mayor que datos disponibles ({}). Ajustando a {}",
                ventana, tamanoDatos, tamanoDatos);
            ventana = tamanoDatos;
        }
        
        // Validar que la ventana sea al menos 3
        if (ventana < 3) {
            logger.warn("Ventana muy pequeña ({}). Ajustando a 3", ventana);
            ventana = 3;
        }
        
        return ventana;
    }
    
    @Override
    public String getCodigoAlgoritmo() {
        return CODIGO;
    }
    
    @Override
    public String getNombreAlgoritmo() {
        return NOMBRE;
    }
    
    @Override
    public int getMinimosDatosRequeridos() {
        return MINIMO_DATOS;
    }
    
    /**
     * Valida los parámetros específicos del algoritmo SMA.
     * 
     * @param datosHistoricos Lista de valores históricos
     * @param parametros Parámetros del algoritmo
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @Override
    public void validarDatos(List<Double> datosHistoricos, Map<String, Double> parametros) {
        // Validación base
        super.validarDatos(datosHistoricos, parametros);
        
        // Validar parámetro de ventana si está presente
        if (parametros != null && parametros.containsKey(PARAMETRO_VENTANA)) {
            double ventana = parametros.get(PARAMETRO_VENTANA);
            
            if (ventana < 1) {
                String mensaje = String.format(
                    "El parametro 'ventana' debe ser mayor a 0. Valor recibido: %.0f",
                    ventana
                );
                logger.error(mensaje);
                throw new IllegalArgumentException(mensaje);
            }
            
            if (ventana > datosHistoricos.size()) {
                String mensaje = String.format(
                    "El parametro 'ventana' (%.0f) no puede ser mayor que el numero de datos historicos (%d)",
                    ventana, datosHistoricos.size()
                );
                logger.error(mensaje);
                throw new IllegalArgumentException(mensaje);
            }
        }
    }
}