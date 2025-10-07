package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.prediccion.apppredicciongm.enums.EstadoOrdenCompra;


@Entity
@Table(name = "ordenes_compra")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrdenCompra implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_compra")
    private Long ordenCompraId;

    @Column(name = "numero_orden", unique = true, nullable = false)
    private String numeroOrden;

    @ManyToOne
    @JoinColumn(name = "id_proveedor", referencedColumnName = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "fecha_orden", nullable = false)
    private LocalDate fechaOrden;

    @Column(name = "fecha_entrega_esperada")
    private LocalDate fechaEntregaEsperada;

    @Column(name = "fecha_entrega_real")
    private LocalDate fechaEntregaReal;

    @Column(name = "estado_orden")
    @Enumerated(EnumType.STRING)
    private EstadoOrdenCompra estadoOrden;

    @Column(name = "total_orden", precision = 12, scale = 2)
    private BigDecimal totalOrden;

    @Column(name = "generada_automaticamente")
    private Boolean generadaAutomaticamente;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrdenCompra> detalles;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (fechaOrden == null) {
            fechaOrden = LocalDate.now();
        }
        if (estadoOrden == null) {
            estadoOrden = EstadoOrdenCompra.PENDIENTE;
        }
    }
}
