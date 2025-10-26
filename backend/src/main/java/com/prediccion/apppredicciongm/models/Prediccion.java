package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

@Entity
@Table(name="prediccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prediccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prediccion")
    private Integer prediccionId;

    @Column(name = "fecha_ejecucion")
    private LocalDateTime fechaEjecucion;

    @Column(name = "horizonte_tiempo")
    private Integer horizonteTiempo;

    @Column(name = "algoritmo_usado")
    private String algoritmoUsado;

    @Column(name = "demanda_predicha_total")
    private Integer demandaPredichaTotal;

    @Column(name="metricas_error" )
    private BigDecimal metricasError;

    @ManyToOne
    @JoinColumn(name="id_producto", referencedColumnName = "id_producto")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name="id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name="id_parametro", referencedColumnName = "id_parametro")
    private ParametroAlgoritmo parametroAlgoritmo;
}
