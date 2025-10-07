package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

@Entity
@Table(name = "estacionalidad_producto", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_producto", "mes"}))
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EstacionalidadProducto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estacionalidad")
    private Long estacionalidadId;

    @ManyToOne
    @JoinColumn(name = "id_producto", referencedColumnName = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "mes", nullable = false)
    private Integer mes; // 1-12

    @Column(name = "factor_estacional", precision = 5, scale = 2)
    private BigDecimal factorEstacional; // Ej: 1.5 = 150% de demanda normal

    @Column(name = "demanda_promedio_historica")
    private Integer demandaPromedioHistorica;

    @Column(name = "demanda_maxima")
    private Integer demandaMaxima;

    @Column(name = "demanda_minima")
    private Integer demandaMinima;

    @Column(name = "anio_referencia")
    private Integer anioReferencia;

    @Column(name = "descripcion_temporada")
    private String descripcionTemporada; // Ej: "Navidad", "Verano", "Regreso a clases"

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    // Método para calcular demanda ajustada
    public Integer calcularDemandaAjustada(Integer demandaBase) {
        if (demandaBase == null || factorEstacional == null) {
            return demandaBase;
        }
        return new BigDecimal(demandaBase)
                .multiply(factorEstacional)
                .intValue();
    }
}
