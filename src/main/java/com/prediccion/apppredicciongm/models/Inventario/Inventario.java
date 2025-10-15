package com.prediccion.apppredicciongm.models.Inventario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.enums.EstadoInventario;

@Entity
@Table(name = "inventario")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Inventario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Integer inventarioId;

    @OneToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", unique = true)
    private Producto producto;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    @Column(name = "stock_reservado")
    private Integer stockReservado;

    @Column(name = "stock_en_transito")
    private Integer stockEnTransito;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @Column(name = "punto_reorden")
    private Integer puntoReorden;

    @Column(name = "ubicacion_almacen")
    private String ubicacionAlmacen;

    @Column(name = "fecha_ultimo_movimiento")
    private LocalDateTime fechaUltimoMovimiento;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @Column(name = "dias_sin_venta")
    private Integer diasSinVenta;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoInventario estado;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    public Integer getStockTotal() {
        return stockDisponible + stockReservado + stockEnTransito;
    }

    public boolean necesitaReorden() {
        return stockDisponible <= puntoReorden;
    }

    public boolean bajoPuntoMinimo() {
        return stockDisponible < stockMinimo;
    }

    @PrePersist
    protected void onCreate() {
        fechaUltimaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaUltimaActualizacion = LocalDateTime.now();
    }
}
