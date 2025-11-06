package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.algorithms;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.ResultadoPrediccionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Clase base abstracta que proporciona funcionalidades comunes para todos los algoritmos de predicción.
 * 
 * Implementa la validación de datos, cálculo de métricas de error y utilidades matemáticas
 * que son compartidas por todos los algoritmos. Los algoritmos concretos deben heredar
 * de esta clase e implementar el método predecir específico.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */
public abstract class AlgoritmoPrediccionBase implements IAlgoritmoPrediccion {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Valida que los datos históricos y parámetros sean suficientes para ejecutar el algoritmo.
     * 
     * @param datosHistoricos Lista de valores históricos
     * @param parametros Parámetros del algoritmo
     * @throws IllegalArgumentException si la validación falla
     */
    @Override
    public void validarDatos(List<Double> datosHistoricos, Map<String, Double> parametros) {
        if (datosHistoricos == null || datosHistoricos.isEmpty()) {
            String mensaje = "Los datos historicos no pueden estar vacios";
            logger.error(mensaje);
            throw new IllegalArgumentException(mensaje);
        }
        
        if (datosHistoricos.size() < getMinimosDatosRequeridos()) {
            String mensaje = String.format(
                "Se requieren al menos %d datos historicos. Se recibieron %d",
                getMinimosDatosRequeridos(),
                datosHistoricos.size()
            );
            logger.error(mensaje);
            throw new IllegalArgumentException(mensaje);
        }
        
        // Validar que no haya valores negativos
        for (int i = 0; i < datosHistoricos.size(); i++) {
            Double valor = datosHistoricos.get(i);
            if (valor == null || valor < 0) {
                String mensaje = String.format(
                    "Valor invalido en posicion %d: %s. Los valores deben ser mayores o iguales a cero",
                    i, valor
                );
                logger.error(mensaje);
                throw new IllegalArgumentException(mensaje);
            }
        }
        
        logger.debug("Validacion de datos exitosa para algoritmo {}", getCodigoAlgoritmo());
    }
    
    /**
     * Calcula el Error Absoluto Medio (MAE) entre valores reales y predichos.
     * 
     * @param valoresReales Lista de valores reales
     * @param valoresPredichos Lista de valores predichos
     * @return MAE calculado
     */
    protected double calcularMAE(List<Double> valoresReales, List<Double> valoresPredichos) {
        if (valoresReales.size() != valoresPredichos.size()) {
            logger.warn("Tamaños diferentes entre valores reales y predichos para MAE");
            return 0.0;
        }
        
        double sumaErrores = 0.0;
        for (int i = 0; i < valoresReales.size(); i++) {
            sumaErrores += Math.abs(valoresReales.get(i) - valoresPredichos.get(i));
        }
        
        return sumaErrores / valoresReales.size();
    }
    
    /**
     * Calcula el Error Porcentual Absoluto Medio (MAPE).
     * 
     * @param valoresReales Lista de valores reales
     * @param valoresPredichos Lista de valores predichos
     * @return MAPE calculado (en porcentaje 0-100)
     */
    protected double calcularMAPE(List<Double> valoresReales, List<Double> valoresPredichos) {
        if (valoresReales.size() != valoresPredichos.size()) {
            logger.warn("Tamaños diferentes entre valores reales y predichos para MAPE");
            return 0.0;
        }
        
        double sumaErroresPorcentuales = 0.0;
        int contadorValidos = 0;
        
        for (int i = 0; i < valoresReales.size(); i++) {
            double real = valoresReales.get(i);
            double predicho = valoresPredichos.get(i);
            
            // Evitar división por cero
            if (real != 0) {
                sumaErroresPorcentuales += Math.abs((real - predicho) / real);
                contadorValidos++;
            }
        }
        
        if (contadorValidos == 0) {
            logger.warn("No hay valores validos para calcular MAPE");
            return 0.0;
        }
        
        return (sumaErroresPorcentuales / contadorValidos) * 100.0;
    }
    
    /**
     * Calcula la Raíz del Error Cuadrático Medio (RMSE).
     * 
     * @param valoresReales Lista de valores reales
     * @param valoresPredichos Lista de valores predichos
     * @return RMSE calculado
     */
    protected double calcularRMSE(List<Double> valoresReales, List<Double> valoresPredichos) {
        if (valoresReales.size() != valoresPredichos.size()) {
            logger.warn("Tamaños diferentes entre valores reales y predichos para RMSE");
            return 0.0;
        }
        
        double sumaErroresCuadrados = 0.0;
        for (int i = 0; i < valoresReales.size(); i++) {
            double error = valoresReales.get(i) - valoresPredichos.get(i);
            sumaErroresCuadrados += error * error;
        }
        
        return Math.sqrt(sumaErroresCuadrados / valoresReales.size());
    }
    
    /**
     * Determina la calidad de la predicción basándose en el MAPE.
     * 
     * @param mape Error Porcentual Absoluto Medio
     * @return String con la calidad: EXCELENTE, BUENA, ACEPTABLE, POBRE
     */
    protected String determinarCalidadPrediccion(double mape) {
        if (mape < 10.0) {
            return "EXCELENTE";
        } else if (mape < 20.0) {
            return "BUENA";
        } else if (mape < 50.0) {
            return "ACEPTABLE";
        } else {
            return "POBRE";
        }
    }
    
    /**
     * Calcula el promedio de una lista de valores.
     * 
     * @param valores Lista de valores
     * @return Promedio calculado
     */
    protected double calcularPromedio(List<Double> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0.0;
        }
        
        double suma = 0.0;
        for (Double valor : valores) {
            suma += valor;
        }
        
        return suma / valores.size();
    }
    
    /**
     * Calcula la desviación estándar de una lista de valores.
     * 
     * @param valores Lista de valores
     * @return Desviación estándar calculada
     */
    protected double calcularDesviacionEstandar(List<Double> valores) {
        if (valores == null || valores.size() < 2) {
            return 0.0;
        }
        
        double promedio = calcularPromedio(valores);
        double sumaCuadrados = 0.0;
        
        for (Double valor : valores) {
            double diferencia = valor - promedio;
            sumaCuadrados += diferencia * diferencia;
        }
        
        return Math.sqrt(sumaCuadrados / (valores.size() - 1));
    }
    
    /**
     * Construye el resultado de la predicción con todas las métricas calculadas.
     * 
     * @param valoresPredichos Lista de valores predichos
     * @param datosHistoricos Datos históricos utilizados
     * @param parametros Parámetros del algoritmo
     * @return ResultadoPrediccionDTO completo
     */
    protected ResultadoPrediccionDTO construirResultado(
            List<Double> valoresPredichos,
            List<Double> datosHistoricos,
            Map<String, Double> parametros) {
        
        // Calcular la demanda total predicha
        double demandaTotal = valoresPredichos.stream().mapToDouble(Double::doubleValue).sum();
        
        // Para calcular métricas, usamos los últimos N datos como prueba
        int tamanioPrueba = Math.min(valoresPredichos.size(), datosHistoricos.size() / 4);
        List<Double> datosRealesPrueba = datosHistoricos.subList(
            datosHistoricos.size() - tamanioPrueba,
            datosHistoricos.size()
        );
        List<Double> prediccionesPrueba = valoresPredichos.subList(0, tamanioPrueba);
        
        double mae = calcularMAE(datosRealesPrueba, prediccionesPrueba);
        double mape = calcularMAPE(datosRealesPrueba, prediccionesPrueba);
        double rmse = calcularRMSE(datosRealesPrueba, prediccionesPrueba);
        String calidad = determinarCalidadPrediccion(mape);
        
        logger.info("Prediccion completada: Algoritmo={}, DemandaTotal={}, MAPE={}%, Calidad={}",
            getCodigoAlgoritmo(), demandaTotal, String.format("%.2f", mape), calidad);
        
        return ResultadoPrediccionDTO.builder()
                .valoresPredichos(valoresPredichos)
                .datosHistoricos(datosHistoricos)
                .demandaTotalPredicha(demandaTotal)
                .mae(mae)
                .mape(mape)
                .rmse(rmse)
                .algoritmoUsado(getCodigoAlgoritmo())
                .nombreAlgoritmo(getNombreAlgoritmo())
                .parametros(parametros)
                .calidadPrediccion(calidad)
                .tieneTendencia(false)
                .tieneEstacionalidad(false)
                .advertencias(new ArrayList<>())
                .recomendaciones(new ArrayList<>())
                .build();
    }
    
    /**
     * Genera recomendaciones basadas en la tendencia de los datos.
     * 
     * @param datosHistoricos Datos históricos
     * @param valoresPredichos Valores predichos
     * @return Lista de recomendaciones
     */
    protected List<String> generarRecomendaciones(List<Double> datosHistoricos, List<Double> valoresPredichos) {
        List<String> recomendaciones = new ArrayList<>();
        
        double promedioHistorico = calcularPromedio(datosHistoricos);
        double promedioPrediccion = calcularPromedio(valoresPredichos);
        
        double cambioProcentual = ((promedioPrediccion - promedioHistorico) / promedioHistorico) * 100;
        
        if (cambioProcentual > 20) {
            recomendaciones.add(String.format(
                "Se espera un incremento de %.1f%% en la demanda. Considere aumentar el stock de seguridad",
                cambioProcentual
            ));
        } else if (cambioProcentual < -20) {
            recomendaciones.add(String.format(
                "Se espera una disminucion de %.1f%% en la demanda. Revise politicas de reorden",
                Math.abs(cambioProcentual)
            ));
        } else {
            recomendaciones.add("Demanda estable. Mantener politicas actuales de inventario");
        }
        
        return recomendaciones;
    }
}