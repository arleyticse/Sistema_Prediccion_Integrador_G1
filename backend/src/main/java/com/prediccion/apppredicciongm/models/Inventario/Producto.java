package com.prediccion.apppredicciongm.models.Inventario;

import com.prediccion.apppredicciongm.models.Proveedor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "productos",
    indexes = {
        @Index(name = "idx_producto_nombre", columnList = "nombre"),
        @Index(name = "idx_producto_categoria", columnList = "id_categoria"),
        @Index(name = "idx_producto_unidad_medida", columnList = "id_um"),
        @Index(name = "idx_producto_fecha_registro", columnList = "fecha_registro")
    }
)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer productoId;

    private String nombre;

    @Column(name="costo_adquisicion", precision=10, scale=2)
    private BigDecimal costoAdquisicion;

    @Column(name="costo_mantenimiento", precision=10, scale=2)
    private BigDecimal costoMantenimiento;

    @Column(name="costo_mantenimiento_anual", precision=10, scale=2)
    private BigDecimal costoMantenimientoAnual;

    @Column(name = "costo_pedido", precision=10, scale=2)
    private BigDecimal costoPedido;

    @Column(name="dias_lead_time")
    private Integer diasLeadTime;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "id_categoria", referencedColumnName = "id_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_um", referencedColumnName = "id_um")
    private UnidadMedida unidadMedida;

    @ManyToOne
    @JoinColumn(name = "id_proveedor_principal", referencedColumnName = "id_proveedor")
    private Proveedor proveedorPrincipal;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
