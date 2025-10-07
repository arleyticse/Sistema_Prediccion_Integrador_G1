package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.enums.EstadoImportacion;
import com.prediccion.apppredicciongm.enums.TipoDatosImportacion;


@Entity
@Table(name = "importaciones_datos")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ImportacionDatos implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_importacion")
    private Long importacionId;

    @Column(name = "tipo_datos", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoDatosImportacion tipoDatos;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "ruta_archivo")
    private String rutaArchivo;

    @Column(name = "fecha_importacion", nullable = false)
    private LocalDateTime fechaImportacion;

    @Column(name = "registros_procesados")
    private Integer registrosProcesados;

    @Column(name = "registros_exitosos")
    private Integer registrosExitosos;

    @Column(name = "registros_fallidos")
    private Integer registrosFallidos;

    @Column(name = "estado_importacion")
    @Enumerated(EnumType.STRING)
    private EstadoImportacion estadoImportacion;

    @Column(name = "tiempo_procesamiento_ms")
    private Long tiempoProcesamiento;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @Column(name = "errores", length = 2000)
    private String errores; // JSON o texto con errores encontrados

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaImportacion = LocalDateTime.now();
        if (estadoImportacion == null) {
            estadoImportacion = EstadoImportacion.EN_PROCESO;
        }
    }

    // Método para calcular tasa de éxito
    public Double getTasaExito() {
        if (registrosProcesados == null || registrosProcesados == 0) {
            return 0.0;
        }
        return (registrosExitosos.doubleValue() / registrosProcesados.doubleValue()) * 100;
    }
}
