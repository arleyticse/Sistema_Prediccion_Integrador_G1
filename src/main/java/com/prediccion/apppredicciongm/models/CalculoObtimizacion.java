package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

/**
 * Entidad JPA para almacenar cálculos de optimización de inventario.
 * Contiene los resultados de cálculos EOQ y ROP para productos.
 * 
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-21
 */
@Entity
@Table(name = "calculo_optimizacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculoObtimizacion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calculo")
    private Integer calculoId;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto")
    private Producto producto;

    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;

    @Column(name = "demanda_anual_estimada")
    private Integer demandaAnualEstimada;

    @Column(name = "EOQ_cantidad_optima")
    private Integer eoqCantidadOptima;

    @Column(name = "ROP_punto_reorden")
    private Integer ropPuntoReorden;

    @Column(name = "stock_seguridad_sugerido")
    private Integer stockSeguridadSugerido;

    @Column(name = "costo_total_inventario", precision = 10, scale = 2)
    private BigDecimal costoTotalInventario;

    @Column(name = "costo_mantenimiento", precision = 10, scale = 2)
    private BigDecimal costoMantenimiento;

    @Column(name = "costo_pedido", precision = 10, scale = 2)
    private BigDecimal costoPedido;

    @Column(name = "dias_lead_time")
    private Integer diasLeadTime;

    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "numero_ordenes_anuales")
    private Integer numeroOrdenesAnuales;

    @Column(name = "dias_entre_lotes")
    private Integer diasEntreLotes;

    @Column(name = "stock_seguridad")
    private Integer stockSeguridad;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
