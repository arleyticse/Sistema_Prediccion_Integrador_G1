package com.prediccion.apppredicciongm.models.Inventario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "unidad_medida")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnidadMedida implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_um")
    private Integer unidadMedidaId;

    private String nombre;

    private String abreviatura;

    private String descripcion;
}
