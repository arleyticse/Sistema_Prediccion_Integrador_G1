package com.prediccion.apppredicciongm.models.Inventario;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "categorias")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer categoriaId;

    @NotNull(message = "El nombre de la categoría no puede ser nulo")
    @Column(name = "nombre", unique = true)
    private String nombre;
}
