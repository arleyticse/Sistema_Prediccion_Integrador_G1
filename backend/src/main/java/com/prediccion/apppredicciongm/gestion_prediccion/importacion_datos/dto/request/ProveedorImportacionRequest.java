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
 * DTO para la importación de proveedores desde archivos CSV.
 * <p>
 * Representa los datos de un proveedor que serán importados al sistema,
 * incluyendo información básica, de contacto, comercial y logística.
 * </p>
 * 
 * <h3>Campos Requeridos:</h3>
 * <ul>
 *   <li><b>razonSocial:</b> Nombre legal/fiscal del proveedor (obligatorio)</li>
 *   <li><b>estado:</b> Estado activo/inactivo del proveedor (SI/NO)</li>
 * </ul>
 * 
 * <h3>Campos Opcionales:</h3>
 * <ul>
 *   <li><b>nombreComercial:</b> Nombre comercial del proveedor</li>
 *   <li><b>rucNit:</b> Número de identificación tributaria (RUC/NIT)</li>
 *   <li><b>telefono:</b> Teléfono de contacto</li>
 *   <li><b>email:</b> Correo electrónico de contacto</li>
 *   <li><b>direccion:</b> Dirección física del proveedor</li>
 *   <li><b>ciudad:</b> Ciudad donde opera el proveedor</li>
 *   <li><b>pais:</b> País del proveedor</li>
 *   <li><b>personaContacto:</b> Nombre del contacto principal</li>
 *   <li><b>tiempoEntregaDias:</b> Tiempo estimado de entrega en días</li>
 *   <li><b>diasCredito:</b> Días de crédito otorgados</li>
 *   <li><b>calificacion:</b> Calificación del proveedor (0.0 - 10.0)</li>
 *   <li><b>observaciones:</b> Notas adicionales sobre el proveedor</li>
 * </ul>
 * 
 * <h3>Ejemplo de uso en CSV:</h3>
 * <pre>
 * razonSocial,nombreComercial,rucNit,telefono,email,direccion,ciudad,pais,personaContacto,tiempoEntregaDias,diasCredito,calificacion,estado,observaciones
 * Distribuidora ABC S.A.,ABC Distribuciones,20123456789,987654321,ventas@abc.com,Av. Principal 123,Lima,Perú,Juan Pérez,7,30,8.5,SI,Proveedor confiable
 * </pre>
 * 
 * @author Sistema de Predicción GM
 * @version 1.0
 * @see com.prediccion.apppredicciongm.models.Proveedor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorImportacionRequest {

    /**
     * Razón social o nombre legal del proveedor.
     * <p>Campo obligatorio, único identificador legal del proveedor.</p>
     */
    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 3, max = 200, message = "La razón social debe tener entre 3 y 200 caracteres")
    private String razonSocial;

    /**
     * Nombre comercial del proveedor.
     * <p>Campo opcional, puede ser diferente a la razón social.</p>
     */
    @Size(max = 200, message = "El nombre comercial no puede exceder 200 caracteres")
    private String nombreComercial;

    /**
     * Número de RUC o NIT del proveedor.
     * <p>Identificación tributaria única del proveedor.</p>
     */
    @Pattern(regexp = "^[0-9]{8,20}$", 
             message = "RUC/NIT debe contener entre 8 y 20 dígitos numéricos")
    private String rucNit;

    /**
     * Teléfono de contacto del proveedor.
     * <p>Puede incluir código de país y extensión.</p>
     */
    @Pattern(regexp = "^[+]?[0-9\\s()-]{7,20}$", 
             message = "Teléfono debe tener formato válido (7-20 caracteres)")
    private String telefono;

    /**
     * Correo electrónico de contacto del proveedor.
     */
    @Email(message = "Email debe tener formato válido")
    @Size(max = 100, message = "Email no puede exceder 100 caracteres")
    private String email;

    /**
     * Dirección física del proveedor.
     */
    @Size(max = 300, message = "Dirección no puede exceder 300 caracteres")
    private String direccion;

    /**
     * Ciudad donde opera el proveedor.
     */
    @Size(max = 100, message = "Ciudad no puede exceder 100 caracteres")
    private String ciudad;

    /**
     * País del proveedor.
     */
    @Size(max = 100, message = "País no puede exceder 100 caracteres")
    private String pais;

    /**
     * Nombre de la persona de contacto principal.
     */
    @Size(max = 100, message = "Persona de contacto no puede exceder 100 caracteres")
    private String personaContacto;

    /**
     * Tiempo de entrega estimado en días.
     * <p>Indica cuántos días tarda el proveedor en entregar los productos.</p>
     */
    @Min(value = 0, message = "Tiempo de entrega no puede ser negativo")
    @Max(value = 365, message = "Tiempo de entrega no puede exceder 365 días")
    private Integer tiempoEntregaDias;

    /**
     * Días de crédito otorgados por el proveedor.
     * <p>Plazo de pago en días (0 = pago inmediato).</p>
     */
    @Min(value = 0, message = "Días de crédito no puede ser negativo")
    @Max(value = 365, message = "Días de crédito no puede exceder 365 días")
    private Integer diasCredito;

    /**
     * Calificación del proveedor.
     * <p>Escala del 0.0 al 10.0 que representa la evaluación del proveedor.</p>
     */
    @DecimalMin(value = "0.0", message = "Calificación mínima es 0.0")
    @DecimalMax(value = "10.0", message = "Calificación máxima es 10.0")
    @Digits(integer = 2, fraction = 2, message = "Calificación debe tener máximo 2 enteros y 2 decimales")
    private BigDecimal calificacion;

    /**
     * Estado del proveedor (SI = activo, NO = inactivo).
     * <p>Campo obligatorio que determina si el proveedor está activo.</p>
     */
    @NotBlank(message = "Estado es obligatorio")
    @Pattern(regexp = "(?i)^(SI|NO|ACTIVO|INACTIVO|TRUE|FALSE|1|0)$", 
             message = "Estado debe ser: SI/NO, ACTIVO/INACTIVO, TRUE/FALSE o 1/0")
    private String estado;

    /**
     * Observaciones adicionales sobre el proveedor.
     */
    @Size(max = 500, message = "Observaciones no puede exceder 500 caracteres")
    private String observaciones;

    /**
     * Número de fila en el archivo CSV.
     * <p>Usado para identificar la fila en caso de errores de validación.</p>
     */
    private Integer numeroFila;

    /**
     * Valida reglas de negocio adicionales del proveedor.
     * <p>
     * Verifica:
     * <ul>
     *   <li>Si tiene días de crédito, debe tener RUC/NIT</li>
     *   <li>Si tiene calificación, debe estar en rango válido</li>
     *   <li>Email válido si se proporciona</li>
     *   <li>Al menos un medio de contacto (teléfono o email)</li>
     * </ul>
     * </p>
     * 
     * @return lista de mensajes de error encontrados (vacía si es válido)
     */
    public List<String> validarReglas() {
        List<String> errores = new ArrayList<>();

        // Si tiene días de crédito, debe tener RUC/NIT
        if (diasCredito != null && diasCredito > 0) {
            if (rucNit == null || rucNit.trim().isEmpty()) {
                errores.add("Si otorga crédito, debe proporcionar RUC/NIT");
            }
        }

        // Si tiene calificación debe estar en rango
        if (calificacion != null) {
            if (calificacion.compareTo(BigDecimal.ZERO) < 0 || 
                calificacion.compareTo(new BigDecimal("10.0")) > 0) {
                errores.add("Calificación debe estar entre 0.0 y 10.0");
            }
        }

        // Debe tener al menos un medio de contacto
        boolean tieneContacto = false;
        if (telefono != null && !telefono.trim().isEmpty()) {
            tieneContacto = true;
        }
        if (email != null && !email.trim().isEmpty()) {
            tieneContacto = true;
        }
        
        if (!tieneContacto) {
            errores.add("Debe proporcionar al menos un medio de contacto (teléfono o email)");
        }

        // Validar formato de estado
        if (estado != null && !estado.trim().isEmpty()) {
            String estadoUpper = estado.trim().toUpperCase();
            if (!estadoUpper.matches("^(SI|NO|ACTIVO|INACTIVO|TRUE|FALSE|1|0)$")) {
                errores.add("Estado debe ser: SI/NO, ACTIVO/INACTIVO, TRUE/FALSE o 1/0");
            }
        }

        return errores;
    }

    /**
     * Convierte el valor string del estado a booleano.
     * 
     * @return true si el proveedor está activo, false en caso contrario
     */
    public boolean convertirEstado() {
        if (estado == null || estado.trim().isEmpty()) {
            return true; // Por defecto activo
        }
        
        String estadoUpper = estado.trim().toUpperCase();
        return estadoUpper.equals("SI") || 
               estadoUpper.equals("ACTIVO") || 
               estadoUpper.equals("TRUE") || 
               estadoUpper.equals("1");
    }
}
