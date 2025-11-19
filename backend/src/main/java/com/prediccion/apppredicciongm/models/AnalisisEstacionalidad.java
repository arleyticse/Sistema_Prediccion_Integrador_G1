package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad para análisis automático de estacionalidad de productos
 */
@Entity
@Table(name = "analisis_estacionalidad", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AnalisisEstacionalidad implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_analisis")
    private Long analisisId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "tiene_estacionalidad", nullable = false)
    private Boolean tieneEstacionalidad;

    @Column(name = "intensidad_estacionalidad", precision = 6, scale = 4)
    private BigDecimal intensidadEstacionalidad;

    @Column(name = "mes_mayor_demanda")
    private Integer mesMayorDemanda;

    @Column(name = "mes_menor_demanda")
    private Integer mesMenorDemanda;

    // Coeficientes estacionales por mes (1.0 = promedio)
    @Column(name = "coeficiente_enero", precision = 6, scale = 4)
    private BigDecimal coeficienteEnero;

    @Column(name = "coeficiente_febrero", precision = 6, scale = 4)
    private BigDecimal coeficienteFebrero;

    @Column(name = "coeficiente_marzo", precision = 6, scale = 4)
    private BigDecimal coeficienteMarzo;

    @Column(name = "coeficiente_abril", precision = 6, scale = 4)
    private BigDecimal coeficienteAbril;

    @Column(name = "coeficiente_mayo", precision = 6, scale = 4)
    private BigDecimal coeficienteMayo;

    @Column(name = "coeficiente_junio", precision = 6, scale = 4)
    private BigDecimal coeficienteJunio;

    @Column(name = "coeficiente_julio", precision = 6, scale = 4)
    private BigDecimal coeficienteJulio;

    @Column(name = "coeficiente_agosto", precision = 6, scale = 4)
    private BigDecimal coeficienteAgosto;

    @Column(name = "coeficiente_septiembre", precision = 6, scale = 4)
    private BigDecimal coeficienteSeptiembre;

    @Column(name = "coeficiente_octubre", precision = 6, scale = 4)
    private BigDecimal coeficienteOctubre;

    @Column(name = "coeficiente_noviembre", precision = 6, scale = 4)
    private BigDecimal coeficienteNoviembre;

    @Column(name = "coeficiente_diciembre", precision = 6, scale = 4)
    private BigDecimal coeficienteDiciembre;

    @Column(name = "fecha_analisis", nullable = false)
    private LocalDate fechaAnalisis;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (fechaAnalisis == null) {
            fechaAnalisis = LocalDate.now();
        }
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Obtiene el coeficiente estacional para un mes específico
     */
    public BigDecimal getCoeficienteParaMes(int mes) {
        return switch (mes) {
            case 1 -> coeficienteEnero;
            case 2 -> coeficienteFebrero;
            case 3 -> coeficienteMarzo;
            case 4 -> coeficienteAbril;
            case 5 -> coeficienteMayo;
            case 6 -> coeficienteJunio;
            case 7 -> coeficienteJulio;
            case 8 -> coeficienteAgosto;
            case 9 -> coeficienteSeptiembre;
            case 10 -> coeficienteOctubre;
            case 11 -> coeficienteNoviembre;
            case 12 -> coeficienteDiciembre;
            default -> BigDecimal.ONE;
        };
    }

    /**
     * Obtiene el nombre del mes con mayor demanda
     */
    public String getNombreMesMayorDemanda() {
        if (mesMayorDemanda == null) return null;
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mesMayorDemanda];
    }

    /**
     * Obtiene el nombre del mes con menor demanda
     */
    public String getNombreMesMenorDemanda() {
        if (mesMenorDemanda == null) return null;
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mesMenorDemanda];
    }
}