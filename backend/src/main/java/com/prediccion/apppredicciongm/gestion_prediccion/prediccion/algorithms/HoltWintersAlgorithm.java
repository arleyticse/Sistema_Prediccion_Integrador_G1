package com.prediccion.apppredicciongm.gestion_prediccion.prediccion.algorithms;

import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.dto.response.ResultadoPrediccionDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del algoritmo Holt-Winters (Suavizado Exponencial Triple - Versión Aditiva).
 *
 * Este es el algoritmo más completo de la familia de suavizados exponenciales, capaz de capturar
 * nivel, tendencia y estacionalidad en los datos. Es ideal para productos con patrones estacionales
 * marcados, como panetón (Navidad), helados (verano) o chocolates (San Valentín).
 *
 * Características:
 * - Captura nivel + tendencia + estacionalidad
 * - Tres parámetros de control (alpha, beta, gamma)
 * - Requiere al menos 2 ciclos estacionales completos de datos
 * - Alta precisión para productos estacionales
 * - Mayor complejidad computacional
 * - Versión aditiva: adecuada cuando la estacionalidad es de amplitud constante
 *
 * Uso recomendado:
 * - Productos con patrones estacionales (helados, panetón, chocolates)
 * - Productos afectados por fechas o campañas específicas (fiestas, temporadas)
 * - Cuando se tienen al menos 2 años de histórico mensual o varios ciclos completos
 *
 * Parámetros requeridos:
 * - alpha: Factor de suavizado del nivel (0 < alpha < 1). Típico: 0.3-0.5
 * - beta: Factor de suavizado de la tendencia (0 < beta < 1). Típico: 0.1-0.3
 * - gamma: Factor de suavizado de estacionalidad (0 < gamma < 1). Típico: 0.3-0.5
 * - periodo: Longitud del ciclo estacional (7=semanal, 30=mensual, 365=anual)
 *
 * Fórmulas (versión aditiva):
 * Nivel(t) = alpha * (Venta(t) - Estacional(t-L)) + (1 - alpha) * (Nivel(t-1) + Tendencia(t-1))
 * Tendencia(t) = beta * (Nivel(t) - Nivel(t-1)) + (1 - beta) * Tendencia(t-1)
 * Estacional(t) = gamma * (Venta(t) - Nivel(t)) + (1 - gamma) * Estacional(t-L)
 * Predicción(t+h) = Nivel(t) + h * Tendencia(t) + Estacional(t+h-L)
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-03
 */

@Component
public class HoltWintersAlgorithm extends AlgoritmoPrediccionBase {
    
    private static final String CODIGO = "HOLT_WINTERS";
    private static final String NOMBRE = "Holt-Winters (Triple Exponencial)";
    private static final int MINIMO_DATOS = 14; // Al menos 2 periodos completos
    
    private static final String PARAMETRO_ALPHA = "alpha";
    private static final String PARAMETRO_BETA = "beta";
    private static final String PARAMETRO_GAMMA = "gamma";
    private static final String PARAMETRO_PERIODO = "periodo";
    
    private static final double ALPHA_DEFAULT = 0.4;
    private static final double BETA_DEFAULT = 0.2;
    private static final double GAMMA_DEFAULT = 0.3;
    private static final int PERIODO_DEFAULT = 7; // Semanal por defecto
    
    @Override
    public ResultadoPrediccionDTO predecir(
            List<Double> datosHistoricos,
            int horizonteTiempo,
            Map<String, Double> parametros) {
        
        logger.info("Iniciando prediccion con {}: {} datos historicos, horizonte {} periodos",
            NOMBRE, datosHistoricos.size(), horizonteTiempo);
        
        // Validar datos
        validarDatos(datosHistoricos, parametros);
        
        // Obtener parámetros
        double alpha = obtenerParametro(parametros, PARAMETRO_ALPHA, ALPHA_DEFAULT);
        double beta = obtenerParametro(parametros, PARAMETRO_BETA, BETA_DEFAULT);
        double gamma = obtenerParametro(parametros, PARAMETRO_GAMMA, GAMMA_DEFAULT);
        int periodo = (int) obtenerParametro(parametros, PARAMETRO_PERIODO, PERIODO_DEFAULT);
        
        logger.debug("Parametros: alpha={}, beta={}, gamma={}, periodo={}",
            String.format("%.2f", alpha),
            String.format("%.2f", beta),
            String.format("%.2f", gamma),
            periodo);
        
        // Validar que hay suficientes datos para el período
        int ciclosCompletos = datosHistoricos.size() / periodo;
        if (ciclosCompletos < 2) {
            logger.warn("Datos insuficientes para periodo {}. Se tienen {} datos ({} ciclos completos). Se requieren al menos {} datos (2 ciclos)",
                periodo, datosHistoricos.size(), ciclosCompletos, periodo * 2);
            // Ajustar período si es necesario
            periodo = Math.max(7, datosHistoricos.size() / 2);
            logger.info("Ajustando periodo a {}", periodo);
        }
        
        // Inicializar componentes
        Map<String, List<Double>> componentes = inicializarComponentes(datosHistoricos, periodo);
        List<Double> nivel = componentes.get("nivel");
        List<Double> tendencia = componentes.get("tendencia");
        List<Double> estacional = componentes.get("estacional");
        
        logger.debug("Inicializacion: Nivel={}, Tendencia={}, {} factores estacionales",
            String.format("%.2f", nivel.get(0)),
            String.format("%.2f", tendencia.get(0)),
            estacional.size());
        
        // Aplicar Holt-Winters a los datos históricos
        for (int t = periodo; t < datosHistoricos.size(); t++) {
            double observacion = datosHistoricos.get(t);
            double nivelAnterior = nivel.get(t - 1);
            double tendenciaAnterior = tendencia.get(t - 1);
            double estacionalAnterior = estacional.get(t - periodo);
            
            // Actualizar nivel
            double nivelNuevo = alpha * (observacion - estacionalAnterior) +
                               (1 - alpha) * (nivelAnterior + tendenciaAnterior);
            nivel.add(nivelNuevo);
            
            // Actualizar tendencia
            double tendenciaNueva = beta * (nivelNuevo - nivelAnterior) +
                                   (1 - beta) * tendenciaAnterior;
            tendencia.add(tendenciaNueva);
            
            // Actualizar estacionalidad
            double estacionalNuevo = gamma * (observacion - nivelNuevo) +
                                    (1 - gamma) * estacionalAnterior;
            estacional.add(estacionalNuevo);
        }
        
        // Generar predicciones
        List<Double> predicciones = new ArrayList<>();
        int ultimoIndice = nivel.size() - 1;
        double ultimoNivel = nivel.get(ultimoIndice);
        double ultimaTendencia = tendencia.get(ultimoIndice);
        
        for (int h = 1; h <= horizonteTiempo; h++) {
            // Índice del factor estacional correspondiente
            int indiceEstacional = estacional.size() - periodo + ((h - 1) % periodo);
            double factorEstacional = estacional.get(indiceEstacional);
            
            // Predicción: nivel + h*tendencia + factor estacional
            double prediccion = ultimoNivel + (h * ultimaTendencia) + factorEstacional;
            
            // Asegurar que la predicción no sea negativa
            prediccion = Math.max(0, prediccion);
            
            predicciones.add(prediccion);
        }
        
        logger.debug("Generadas {} predicciones. Primera: {}, Ultima: {}",
            predicciones.size(),
            String.format("%.2f", predicciones.get(0)),
            String.format("%.2f", predicciones.get(predicciones.size() - 1)));
        
        // Construir resultado
        ResultadoPrediccionDTO resultado = construirResultado(predicciones, datosHistoricos, parametros);
        
        // Marcar que tiene tendencia y estacionalidad
        resultado.setTieneTendencia(true);
        resultado.setTieneEstacionalidad(true);
        resultado.setPeriodoEstacional(periodo);
        
        // Agregar advertencias específicas
        List<String> advertencias = generarAdvertencias(datosHistoricos, periodo, ciclosCompletos);
        resultado.setAdvertencias(advertencias);
        
        // Generar recomendaciones
        List<String> recomendaciones = generarRecomendacionesHoltWinters(
            datosHistoricos, predicciones, ultimaTendencia, estacional, periodo
        );
        resultado.setRecomendaciones(recomendaciones);
        
        logger.info("Prediccion {} completada exitosamente", CODIGO);
        
        return resultado;
    }
    
    /**
     * Inicializa los componentes de nivel, tendencia y estacionalidad.
     * 
     * @param datos Datos históricos
     * @param periodo Longitud del ciclo estacional
     * @return Mapa con las listas inicializadas
     */
    private Map<String, List<Double>> inicializarComponentes(List<Double> datos, int periodo) {
        Map<String, List<Double>> componentes = new HashMap<>();
        
        // Inicializar nivel con el promedio del primer ciclo
        int tamañoPrimerCiclo = Math.min(periodo, datos.size());
        double nivelInicial = calcularPromedio(datos.subList(0, tamañoPrimerCiclo));
        
        List<Double> nivel = new ArrayList<>();
        for (int i = 0; i < periodo; i++) {
            nivel.add(nivelInicial);
        }
        
        // Inicializar tendencia
        List<Double> tendencia = new ArrayList<>();
        double tendenciaInicial = 0.0;
        
        // Calcular tendencia inicial si hay suficientes datos
        if (datos.size() >= periodo * 2) {
            double promedioPrimerCiclo = calcularPromedio(datos.subList(0, periodo));
            double promedioSegundoCiclo = calcularPromedio(datos.subList(periodo, periodo * 2));
            tendenciaInicial = (promedioSegundoCiclo - promedioPrimerCiclo) / periodo;
        }
        
        for (int i = 0; i < periodo; i++) {
            tendencia.add(tendenciaInicial);
        }
        
        // Inicializar factores estacionales
        List<Double> estacional = new ArrayList<>();
        
        // Calcular factores estacionales promedio
        int numeroCiclos = Math.min(datos.size() / periodo, 4); // Máximo 4 ciclos para inicialización
        
        for (int i = 0; i < periodo; i++) {
            double suma = 0.0;
            int contador = 0;
            
            for (int ciclo = 0; ciclo < numeroCiclos; ciclo++) {
                int indice = ciclo * periodo + i;
                if (indice < datos.size()) {
                    suma += datos.get(indice) - nivelInicial;
                    contador++;
                }
            }
            
            double factorEstacional = contador > 0 ? suma / contador : 0.0;
            estacional.add(factorEstacional);
        }
        
        componentes.put("nivel", nivel);
        componentes.put("tendencia", tendencia);
        componentes.put("estacional", estacional);
        
        return componentes;
    }
    
    /**
     * Genera advertencias específicas para Holt-Winters.
     * 
     * @param datos Datos históricos
     * @param periodo Período estacional
     * @param ciclosCompletos Número de ciclos completos en los datos
     * @return Lista de advertencias
     */
    private List<String> generarAdvertencias(List<Double> datos, int periodo, int ciclosCompletos) {
        List<String> advertencias = new ArrayList<>();
        
        if (ciclosCompletos < 2) {
            advertencias.add(String.format(
                "ADVERTENCIA: Solo se detectaron %d ciclos completos. Holt-Winters requiere al menos 2 ciclos para resultados optimos",
                ciclosCompletos
            ));
        } else if (ciclosCompletos < 3) {
            advertencias.add("Se recomienda tener al menos 3 ciclos estacionales completos para mayor precision");
        }
        
        if (datos.size() < MINIMO_DATOS) {
            advertencias.add(String.format(
                "Datos historicos limitados (%d observaciones). Se recomienda al menos %d para Holt-Winters",
                datos.size(), MINIMO_DATOS
            ));
        }
        
        // Detectar si realmente hay estacionalidad
        double varianzaTotal = calcularVarianza(datos);
        if (varianzaTotal < 0.1) {
            advertencias.add("La variabilidad de los datos es muy baja. Considere usar un algoritmo mas simple (SMA o SES)");
        }
        
        return advertencias;
    }
    
    /**
     * Genera recomendaciones específicas basadas en tendencia y estacionalidad.
     * 
     * @param datosHistoricos Datos históricos
     * @param predicciones Predicciones generadas
     * @param tendencia Último valor de tendencia
     * @param factoresEstacionales Factores estacionales
     * @param periodo Período estacional
     * @return Lista de recomendaciones
     */
    private List<String> generarRecomendacionesHoltWinters(
            List<Double> datosHistoricos,
            List<Double> predicciones,
            double tendencia,
            List<Double> factoresEstacionales,
            int periodo) {
        
        List<String> recomendaciones = new ArrayList<>();
        
        // Analizar tendencia
        if (Math.abs(tendencia) < 0.1) {
            recomendaciones.add("Tendencia estable detectada. La demanda se mantiene constante a largo plazo");
        } else if (tendencia > 0.1) {
            recomendaciones.add(String.format(
                "Tendencia CRECIENTE detectada (%.2f unidades/periodo). Considere incrementar stock de seguridad",
                tendencia
            ));
        } else {
            recomendaciones.add(String.format(
                "Tendencia DECRECIENTE detectada (%.2f unidades/periodo). Revise politicas de reorden",
                tendencia
            ));
        }
        
        // Analizar estacionalidad
        double maxEstacional = factoresEstacionales.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minEstacional = factoresEstacionales.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double rangoEstacional = maxEstacional - minEstacional;
        
        if (rangoEstacional > 5) {
            recomendaciones.add(String.format(
                "Patron estacional FUERTE detectado (rango: %.1f unidades). Planifique inventario segun temporada",
                rangoEstacional
            ));
            
            // Identificar períodos pico y valle
            int indicePico = factoresEstacionales.indexOf(maxEstacional);
            int indiceValle = factoresEstacionales.indexOf(minEstacional);
            
            recomendaciones.add(String.format(
                "Periodo de mayor demanda: posicion %d en el ciclo. Periodo de menor demanda: posicion %d",
                indicePico + 1, indiceValle + 1
            ));
        } else {
            recomendaciones.add("Patron estacional moderado. Ajustes menores de inventario seran suficientes");
        }
        
        // Comparar predicción con histórico
        double promedioHistorico = calcularPromedio(datosHistoricos);
        double promedioPrediccion = calcularPromedio(predicciones);
        double cambio = ((promedioPrediccion - promedioHistorico) / promedioHistorico) * 100;
        
        if (Math.abs(cambio) > 10) {
            recomendaciones.add(String.format(
                "Se espera un cambio de %.1f%% en la demanda promedio. Ajuste stock en consecuencia",
                cambio
            ));
        }
        
        return recomendaciones;
    }
    
    /**
     * Calcula la varianza de una lista de valores.
     * 
     * @param valores Lista de valores
     * @return Varianza calculada
     */
    private double calcularVarianza(List<Double> valores) {
        if (valores == null || valores.size() < 2) {
            return 0.0;
        }
        
        double promedio = calcularPromedio(valores);
        double sumaCuadrados = 0.0;
        
        for (Double valor : valores) {
            double diferencia = valor - promedio;
            sumaCuadrados += diferencia * diferencia;
        }
        
        return sumaCuadrados / valores.size();
    }
    
    /**
     * Obtiene un parámetro del mapa o devuelve el valor por defecto.
     * 
     * @param parametros Mapa de parámetros
     * @param nombre Nombre del parámetro
     * @param valorDefault Valor por defecto
     * @return Valor del parámetro
     */
    private double obtenerParametro(Map<String, Double> parametros, String nombre, double valorDefault) {
        if (parametros != null && parametros.containsKey(nombre)) {
            return parametros.get(nombre);
        }
        return valorDefault;
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
    
    @Override
    public void validarDatos(List<Double> datosHistoricos, Map<String, Double> parametros) {
        // Validación base
        super.validarDatos(datosHistoricos, parametros);
        
        // Validar parámetros alpha, beta, gamma
        if (parametros != null) {
            validarParametroRango(parametros, PARAMETRO_ALPHA, 0.0, 1.0);
            validarParametroRango(parametros, PARAMETRO_BETA, 0.0, 1.0);
            validarParametroRango(parametros, PARAMETRO_GAMMA, 0.0, 1.0);
            
            if (parametros.containsKey(PARAMETRO_PERIODO)) {
                double periodo = parametros.get(PARAMETRO_PERIODO);
                if (periodo < 2) {
                    String mensaje = String.format(
                        "El parametro 'periodo' debe ser mayor o igual a 2. Valor recibido: %.0f",
                        periodo
                    );
                    logger.error(mensaje);
                    throw new IllegalArgumentException(mensaje);
                }
            }
        }
    }
    
    /**
     * Valida que un parámetro esté en el rango especificado.
     * 
     * @param parametros Mapa de parámetros
     * @param nombre Nombre del parámetro
     * @param min Valor mínimo permitido
     * @param max Valor máximo permitido
     * @throws IllegalArgumentException si el parámetro está fuera del rango
     */
    private void validarParametroRango(Map<String, Double> parametros, String nombre, double min, double max) {
        if (parametros.containsKey(nombre)) {
            double valor = parametros.get(nombre);
            if (valor <= min || valor >= max) {
                String mensaje = String.format(
                    "El parametro '%s' debe estar entre %.2f y %.2f (exclusivo). Valor recibido: %.2f",
                    nombre, min, max, valor
                );
                logger.error(mensaje);
                throw new IllegalArgumentException(mensaje);
            }
        }
    }
}