package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa la configuración global de la empresa.
 * 
 * Implementa el patrón Singleton a nivel de base de datos:
 * - Solo puede existir un registro con id = 1
 * - Se usa para almacenar información de la empresa y logo
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-11-19
 */
@Entity
@Table(name = "configuracion_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEmpresa {

    /**
     * ID fijo = 1 (singleton pattern).
     * Solo puede existir un registro en la tabla.
     */
    @Id
    @Column(name = "id", nullable = false)
    private Integer id = 1;

    /**
     * Nombre de la empresa.
     */
    @Column(name = "nombre_empresa", nullable = false, length = 255)
    @Size(max = 255, message = "El nombre de la empresa no puede exceder 255 caracteres")
    private String nombreEmpresa;

    /**
     * RUC o número de identificación fiscal.
     */
    @Column(name = "ruc", length = 20)
    @Size(max = 20, message = "El RUC no puede exceder 20 caracteres")
    private String ruc;

    /**
     * Dirección física de la empresa.
     */
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    /**
     * Teléfono de contacto.
     */
    @Column(name = "telefono", length = 50)
    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    private String telefono;

    /**
     * Email de contacto.
     */
    @Column(name = "email", length = 255)
    @Email(message = "Debe proporcionar un email válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    /**
     * Logo de la empresa en formato Base64.
     * 
     * Recomendaciones:
     * - Tamaño máximo: 100KB (~150000 caracteres en base64)
     * - Formatos aceptados: PNG, JPEG, WebP
     * - Dimensiones recomendadas: 200x200px o 300x300px
     */
    @Column(name = "logo_base64", columnDefinition = "TEXT")
    @Size(max = 150000, message = "El logo no puede exceder 100KB (150000 caracteres en base64)")
    private String logoBase64;

    /**
     * Tipo MIME del logo.
     * Ejemplos: image/png, image/jpeg, image/webp
     */
    @Column(name = "logo_mime_type", length = 50)
    @Size(max = 50, message = "El tipo MIME no puede exceder 50 caracteres")
    private String logoMimeType;

    /**
     * Nombre de la persona de contacto.
     */
    @Column(name = "nombre_contacto", length = 255)
    @Size(max = 255, message = "El nombre de contacto no puede exceder 255 caracteres")
    private String nombreContacto;

    /**
     * Cargo de la persona de contacto.
     */
    @Column(name = "cargo_contacto", length = 100)
    @Size(max = 100, message = "El cargo no puede exceder 100 caracteres")
    private String cargoContacto;

    /**
     * Fecha de creación del registro.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última modificación.
     * Se actualiza automáticamente mediante trigger de BD.
     */
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.id = 1; // Asegurar que siempre sea 1
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
