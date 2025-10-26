package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

import com.prediccion.apppredicciongm.models.Inventario.Producto;


@Entity
@Table(name = "detalle_orden_compra")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetalleOrdenCompra implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long detalleId;

    @ManyToOne
    @JoinColumn(name = "id_orden_compra", referencedColumnName = "id_orden_compra", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "cantidad_recibida")
    private Integer cantidadRecibida;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "observaciones")
    private String observaciones;

    public void calcularSubtotal() {
        if (cantidadSolicitada != null && precioUnitario != null) {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidadSolicitada));
        }
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calcularSubtotal();
        if (cantidadRecibida == null) {
            cantidadRecibida = 0;
        }
    }
}
