package com.prediccion.apppredicciongm.gestion_prediccion.importacion_datos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de creación/actualización de ImportacionDatos
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportacionCreateRequest {

    @NotNull(message = "El tipo de datos no puede ser nulo")
    private TipoDatosImportacion tipoDatos;

    @NotBlank(message = "El nombre del archivo no puede estar vacío")
    private String nombreArchivo;

    @NotBlank(message = "La ruta del archivo no puede estar vacía")
    private String rutaArchivo;

    private Integer usuarioId;

    private String observaciones;

    /**
     * Enum para tipos de datos a importar
     */
    public enum TipoDatosImportacion {
        PRODUCTOS,
        INVENTARIO,
        KARDEX,
        DEMANDA,
        PROVEEDORES,
        VENTAS,
        COMPRAS,
        CATEGORIAS,
        ESTACIONALIDAD
    }
}
