package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

@Entity
@Table(name = "calculo_optimizacion")
public class CalculoObtimizacion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calculo")
    private Integer calculoId;

    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;

    @Column(name = "demanda_anual_estimada")
    private Integer demandaAnualEstimada;

    @Column(name = "EOQ_cantidad_optima")
    private Integer eoqCantidadOptima;

    @Column(name = "ROP_punto_reorden")
    private Integer ropPuntoReorden;

    @Column(name="stock_seguridad_sugerido" )
    private Integer stockSeguridadSugerido;

    @Column(name = "costo_total_inventario" , precision=10, scale=2)
    private BigDecimal costoTotalInventario;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    private Producto producto;
}
