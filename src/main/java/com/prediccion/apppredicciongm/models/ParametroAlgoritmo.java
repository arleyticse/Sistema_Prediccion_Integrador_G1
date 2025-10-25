package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para almacenar parámetros de algoritmos de predicción.
 * Permite configurar parámetros flexibles para diferentes algoritmos.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Entity
@Table(name = "parametro_algoritmo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroAlgoritmo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Integer parametroId;

    @Column(name = "nombre_parametro", nullable = false)
    private String nombreParametro;

    @Column(name = "valor_parametro", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorParametro;

    @Column(name = "tipo_algoritmo", nullable = false)
    private String tipoAlgoritmo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "valor_minimo", precision = 10, scale = 2)
    private BigDecimal valorMinimo;

    @Column(name = "valor_maximo", precision = 10, scale = 2)
    private BigDecimal valorMaximo;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Pre-persiste: Inicializa la fecha de creación
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    /**
     * Pre-actualización: Actualiza la fecha de modificación
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
