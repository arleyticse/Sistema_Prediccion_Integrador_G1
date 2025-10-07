package com.prediccion.apppredicciongm.models.Inventario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.enums.TipoAlerta;
import com.prediccion.apppredicciongm.models.Usuario;

/**
 * Entidad para alertas automáticas del sistema de inventario
 */
@Entity
@Table(name = "alertas_inventario", indexes = {
    @Index(name = "idx_alerta_estado", columnList = "estado"),
    @Index(name = "idx_alerta_fecha", columnList = "fecha_generacion")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlertaInventario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long alertaId;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    private Producto producto;

    @Column(name = "tipo_alerta", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoAlerta tipoAlerta;

    @Column(name = "nivel_criticidad", nullable = false)
    @Enumerated(EnumType.STRING)
    private NivelCriticidad nivelCriticidad;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "cantidad_sugerida")
    private Integer cantidadSugerida;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoAlerta estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario_asignado", referencedColumnName = "id_usuario")
    private Usuario usuarioAsignado;

    @Column(name = "accion_tomada", length = 500)
    private String accionTomada;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaGeneracion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoAlerta.PENDIENTE;
        }
    }
}
