package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.prediccion.apppredicciongm.models.Inventario.Producto;

@Entity
@Table(name="registro_demanda")
public class RegistroDemanda implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name="id_registro")
    private Integer registroDemandaId;

    @Column(name="fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name="cantidad_historica")
    private Integer cantidadHistorica;

    @Column(name="periodo_registro" )
    private String periodoRegistro;

    @ManyToOne
    @JoinColumn(name="id_producto", referencedColumnName = "id_producto")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name="id_usuario", referencedColumnName = "id_usuario")
    private Usuario usuario;
}
