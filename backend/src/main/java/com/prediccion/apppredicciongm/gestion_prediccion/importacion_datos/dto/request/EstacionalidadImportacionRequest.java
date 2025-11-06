package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la importación de datos de estacionalidad desde archivos CSV.
 * <p>
 * Representa los datos de estacionalidad que serán importados al sistema,
 * incluyendo factores estacionales, demandas históricas y descripción de temporadas.
 * </p>
 * 
 * <h3>Campos Requeridos:</h3>
 * <ul>
 *   <li><b>nombreProducto:</b> Nombre del producto (debe existir en la base de datos)</li>
 *   <li><b>mes:</b> Mes del año (1-12)</li>
 *   <li><b>factorEstacional:</b> Factor de ajuste estacional (ej: 1.5 = 150% de demanda normal)</li>
 * </ul>
 * 
 * <h3>Campos Opcionales:</h3>
 * <ul>
 *   <li><b>demandaPromedioHistorica:</b> Demanda promedio histórica para ese mes</li>
 *   <li><b>demandaMaxima:</b> Demanda máxima registrada</li>
 *   <li><b>demandaMinima:</b> Demanda mínima registrada</li>
 *   <li><b>anioReferencia:</b> Año de referencia para los datos históricos</li>
 *   <li><b>descripcionTemporada:</b> Descripción de la temporada (ej: "Navidad", "Verano")</li>
 *   <li><b>observaciones:</b> Notas adicionales sobre la estacionalidad</li>
 * </ul>
 * 
 * <h3>Ejemplo de uso en CSV:</h3>
 * <pre>
 * nombreProducto,mes,factorEstacional,demandaPromedioHistorica,demandaMaxima,demandaMinima,anioReferencia,descripcionTemporada,observaciones
 * Detergente Industrial,12,1.8,500,750,350,2024,Navidad,Alta demanda por fiestas
 * </pre>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 * @see com.prediccion.apppredicciongm.models.EstacionalidadProducto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstacionalidadImportacionRequest {

    /**
     * Nombre del producto al que se asocia la estacionalidad.
     * <p>Debe existir en la tabla de productos.</p>
     */
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre del producto debe tener entre 2 y 200 caracteres")
    private String nombreProducto;

    /**
     * Mes del año (1-12).
     * <p>
     * 1 = Enero, 2 = Febrero, ..., 12 = Diciembre
     * </p>
     */
    @NotNull(message = "El mes es obligatorio")
    @Min(value = 1, message = "El mes debe ser entre 1 y 12")
    @Max(value = 12, message = "El mes debe ser entre 1 y 12")
    private Integer mes;

    /**
     * Factor estacional que ajusta la demanda base.
     * <p>
     * Ejemplos:
     * <ul>
     *   <li>1.0 = demanda normal (100%)</li>
     *   <li>1.5 = 50% más de demanda (150%)</li>
     *   <li>0.7 = 30% menos de demanda (70%)</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "El factor estacional es obligatorio")
    @DecimalMin(value = "0.01", message = "El factor estacional debe ser mayor a 0")
    @DecimalMax(value = "10.0", message = "El factor estacional no puede exceder 10.0")
    @Digits(integer = 2, fraction = 2, message = "El factor estacional debe tener máximo 2 enteros y 2 decimales")
    private BigDecimal factorEstacional;

    /**
     * Demanda promedio histórica para el mes especificado.
     */
    @Min(value = 0, message = "La demanda promedio no puede ser negativa")
    private Integer demandaPromedioHistorica;

    /**
     * Demanda máxima registrada históricamente para ese mes.
     */
    @Min(value = 0, message = "La demanda máxima no puede ser negativa")
    private Integer demandaMaxima;

    /**
     * Demanda mínima registrada históricamente para ese mes.
     */
    @Min(value = 0, message = "La demanda mínima no puede ser negativa")
    private Integer demandaMinima;

    /**
     * Año de referencia para los datos históricos.
     * <p>Por defecto se usa el año actual si no se especifica.</p>
     */
    @Min(value = 2000, message = "El año de referencia debe ser desde 2000")
    @Max(value = 2100, message = "El año de referencia no puede ser mayor a 2100")
    private Integer anioReferencia;

    /**
     * Descripción de la temporada o evento especial.
     * <p>Ejemplos: "Navidad", "Verano", "Regreso a clases", "Día de la Madre"</p>
     */
    @Size(max = 100, message = "La descripción de temporada no puede exceder 100 caracteres")
    private String descripcionTemporada;

    /**
     * Observaciones adicionales sobre el patrón estacional.
     */
    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    private String observaciones;

    /**
     * Número de fila en el archivo CSV.
     * <p>Usado para identificar la fila en caso de errores de validación.</p>
     */
    private Integer numeroFila;

    /**
     * Valida reglas de negocio adicionales de estacionalidad.
     * <p>
     * Verifica:
     * <ul>
     *   <li>Demanda máxima debe ser mayor o igual a demanda mínima</li>
     *   <li>Demanda promedio debe estar entre mínima y máxima</li>
     *   <li>Factor estacional coherente con demandas históricas</li>
     *   <li>Mes válido (1-12)</li>
     * </ul>
     * </p>
     * 
     * @return lista de mensajes de error encontrados (vacía si es válido)
     */
    public List<String> validarReglas() {
        List<String> errores = new ArrayList<>();

        // Validar rango de mes
        if (mes != null && (mes < 1 || mes > 12)) {
            errores.add("El mes debe estar entre 1 y 12");
        }

        // Validar coherencia de demandas
        if (demandaMinima != null && demandaMaxima != null) {
            if (demandaMinima > demandaMaxima) {
                errores.add("La demanda mínima no puede ser mayor que la demanda máxima");
            }
        }

        // Validar demanda promedio está en rango
        if (demandaPromedioHistorica != null && demandaMinima != null && demandaMaxima != null) {
            if (demandaPromedioHistorica < demandaMinima || demandaPromedioHistorica > demandaMaxima) {
                errores.add("La demanda promedio debe estar entre la demanda mínima y máxima");
            }
        }

        // Validar factor estacional razonable
        if (factorEstacional != null) {
            if (factorEstacional.compareTo(new BigDecimal("0.01")) < 0) {
                errores.add("El factor estacional debe ser mayor a 0.01");
            }
            if (factorEstacional.compareTo(new BigDecimal("10.0")) > 0) {
                errores.add("El factor estacional no puede exceder 10.0 (advertencia: valor muy alto)");
            }
        }

        // Validar año de referencia razonable
        if (anioReferencia != null) {
            int anioActual = java.time.Year.now().getValue();
            if (anioReferencia > anioActual + 1) {
                errores.add("El año de referencia no puede ser futuro");
            }
            if (anioReferencia < 2000) {
                errores.add("El año de referencia debe ser desde 2000");
            }
        }

        return errores;
    }

    /**
     * Obtiene el nombre del mes en español basado en el número.
     * 
     * @return nombre del mes o "Desconocido" si el mes es inválido
     */
    public String getNombreMes() {
        if (mes == null) return "Desconocido";
        
        String[] meses = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        
        return (mes >= 1 && mes <= 12) ? meses[mes - 1] : "Desconocido";
    }

    /**
     * Calcula el porcentaje del factor estacional.
     * 
     * @return porcentaje (ej: 1.5 → 150%)
     */
    public String getPorcentajeFactorEstacional() {
        if (factorEstacional == null) return "N/A";
        return factorEstacional.multiply(new BigDecimal("100")).intValue() + "%";
    }
}
