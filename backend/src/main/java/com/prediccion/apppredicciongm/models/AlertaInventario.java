package com.prediccion.apppredicciongm.models;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad que representa una alerta de inventario en el sistema.
 * 
 * Las alertas se generan automaticamente cuando se detectan condiciones
 * que requieren atencion del usuario, como stock bajo, punto de reorden
 * alcanzado, predicciones vencidas, entre otros.
 * 
 * El sistema utiliza estas alertas para automatizar el proceso de
 * generacion de predicciones y ordenes de compra.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Entity
@Table(
    name = "alertas_inventario",
    indexes = {
        @Index(name = "idx_alerta_tipo", columnList = "tipo_alerta"),
        @Index(name = "idx_alerta_estado", columnList = "estado"),
        @Index(name = "idx_alerta_criticidad", columnList = "nivel_criticidad"),
        @Index(name = "idx_alerta_producto", columnList = "id_producto"),
        @Index(name = "idx_alerta_fecha_generacion", columnList = "fecha_generacion")
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertaInventario implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * Identificador unico de la alerta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long alertaId;

    /**
     * Tipo de alerta generada.
     * Define la condicion especifica que genero la alerta.
     */
    @Column(name = "tipo_alerta", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoAlerta tipoAlerta;

    /**
     * Nivel de criticidad de la alerta.
     * Permite priorizar las alertas segun su urgencia.
     */
    @Column(name = "nivel_criticidad", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NivelCriticidad nivelCriticidad;

    /**
     * Mensaje descriptivo de la alerta.
     * Explica la situacion detectada de forma legible para el usuario.
     */
    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    /**
     * Producto asociado a la alerta.
     * Relacion Many-to-One con la entidad Producto.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    private Producto producto;

    /**
     * Stock actual del producto al momento de generar la alerta.
     */
    @Column(name = "stock_actual")
    private Integer stockActual;

    /**
     * Stock minimo configurado para el producto.
     */
    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    /**
     * Cantidad sugerida para reponer el inventario.
     * Calculada en base al EOQ o reglas de negocio.
     */
    @Column(name = "cantidad_sugerida")
    private Integer cantidadSugerida;

    /**
     * Usuario asignado para atender la alerta.
     * Permite asignar responsabilidades especificas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_asignado", referencedColumnName = "id_usuario")
    private Usuario usuarioAsignado;

    /**
     * Estado actual de la alerta en su ciclo de vida.
     */
    @Column(name = "estado", length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoAlerta estado;

    /**
     * Fecha y hora en que se genero la alerta.
     */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    /**
     * Fecha y hora en que se resolvio la alerta.
     */
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    /**
     * Descripcion de la accion tomada para resolver la alerta.
     * Ejemplo: "Orden de compra generada", "Ajuste de inventario realizado".
     */
    @Column(name = "accion_tomada", length = 500)
    private String accionTomada;

    /**
     * Observaciones adicionales sobre la alerta.
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Metodo ejecutado antes de persistir la entidad.
     * Establece valores predeterminados para fecha de generacion y estado.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoAlerta.PENDIENTE;
        }
    }

    /**
     * Marca la alerta como resuelta.
     * 
     * @param accion Descripcion de la accion tomada para resolver la alerta
     */
    public void resolver(String accion) {
        this.estado = EstadoAlerta.RESUELTA;
        this.fechaResolucion = LocalDateTime.now();
        this.accionTomada = accion;
    }

    /**
     * Marca la alerta como en proceso.
     * 
     * @param usuario Usuario que comienza a atender la alerta
     */
    public void marcarEnProceso(Usuario usuario) {
        this.estado = EstadoAlerta.EN_PROCESO;
        this.usuarioAsignado = usuario;
    }

    /**
     * Marca la alerta como ignorada.
     * 
     * @param motivo Razon por la cual se ignora la alerta
     */
    public void ignorar(String motivo) {
        this.estado = EstadoAlerta.IGNORADA;
        this.fechaResolucion = LocalDateTime.now();
        this.observaciones = motivo;
    }
}
