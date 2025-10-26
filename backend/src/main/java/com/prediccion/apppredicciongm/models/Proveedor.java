package com.prediccion.apppredicciongm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "proveedores")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Proveedor implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer proveedorId;

    @NotNull(message = "La razón social es obligatoria")
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @NotNull(message = "El nombre comercial es obligatorio")
    @Column(name = "nombre_comercial")
    private String nombreComercial;

    @NotNull(message = "El RUC/NIT es obligatorio")
    @Column(name = "ruc_nit", unique = true)
    private String rucNit;

    @NotNull(message = "El teléfono es obligatorio")
    @Column(name = "telefono")
    private String telefono;

    @NotNull(message = "El email es obligatorio")
    @Column(name = "email")
    private String email;

    @NotNull(message = "La dirección es obligatoria")
    @Column(name = "direccion")
    private String direccion;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "pais")
    private String pais;

    @Column(name = "persona_contacto")
    private String personaContacto;

    @NotNull(message = "El tiempo de entrega es obligatorio")
    @Column(name = "tiempo_entrega_dias")
    private Integer tiempoEntregaDias;

    @Column(name = "dias_credito")
    private Integer diasCredito;

    @Column(name = "calificacion", precision = 3, scale = 2)
    private java.math.BigDecimal calificacion; // De 0 a 5

    @Column(name = "estado")
    private Boolean estado;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (estado == null) {
            estado = true;
        }
    }
}
