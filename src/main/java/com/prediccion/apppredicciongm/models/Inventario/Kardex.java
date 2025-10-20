package com.prediccion.apppredicciongm.models.Inventario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.models.Usuario;

@Entity
@Table(name = "kardex", indexes = {
        @Index(name = "idx_kardex_producto", columnList = "id_producto"),
        @Index(name = "idx_kardex_fecha", columnList = "fecha_movimiento")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Kardex implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kardex")
    private Long kardexId;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "tipo_movimiento", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "saldo_cantidad", nullable = false)
    private Integer saldoCantidad;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "lote")
    private String lote;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "referencia")
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario",nullable = true)
    private Usuario usuario;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "anulado", nullable = false)
    private boolean anulado = false;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }

    public Integer getMovimientoNeto() {
        if (tipoMovimiento.esEntrada()) {
            return cantidad != null ? cantidad : 0;
        } else if (tipoMovimiento.esSalida()) {
            return cantidad != null ? -cantidad : 0;
        }
        return 0;
    }
}
