package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "parametro_algoritmo")
public class ParametroAlgoritmo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Integer parametroId;

    @Column(name = "nombre_parametro")
    private String nombreParametro;

    @Column(name = "valor_parametro", precision=10, scale=2)
    private BigDecimal valorParametro;

    @Column(name = "tipo_algoritmo")
    private String tipoAlgoritmo;
}
