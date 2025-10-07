package com.prediccion.apppredicciongm.models.Inventario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.models.Usuario;

/**
 * Entidad Kardex (Cardex) para registro de movimientos de inventario
 * Mantiene trazabilidad completa de entradas y salidas
 */
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

    @Column(name = "cantidad_entrada")
    private Integer cantidadEntrada;

    @Column(name = "cantidad_salida")
    private Integer cantidadSalida;

    @Column(name = "saldo_cantidad", nullable = false)
    private Integer saldoCantidad;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total_entrada", precision = 12, scale = 2)
    private BigDecimal costoTotalEntrada;

    @Column(name = "costo_total_salida", precision = 12, scale = 2)
    private BigDecimal costoTotalSalida;

    @Column(name = "saldo_valorizado", precision = 12, scale = 2)
    private BigDecimal saldoValorizado;

    @Column(name = "lote")
    private String lote;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @Column(name = "proveedor")
    private String proveedor;

    @Column(name = "cliente")
    private String cliente;

    @Column(name = "motivo")
    private String motivo; 

    @Column(name = "referencia")
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

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
        int entrada = cantidadEntrada != null ? cantidadEntrada : 0;
        int salida = cantidadSalida != null ? cantidadSalida : 0;
        return entrada - salida;
    }
}
