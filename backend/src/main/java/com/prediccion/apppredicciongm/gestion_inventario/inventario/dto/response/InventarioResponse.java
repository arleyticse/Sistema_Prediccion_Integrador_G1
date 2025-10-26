package com.prediccion.apppredicciongm.gestion_inventario.inventario.dto.response;

import com.prediccion.apppredicciongm.enums.EstadoInventario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventarioResponse {
    
    private Integer inventarioId;
    private Integer productoId;
    private String nombreProducto;
    private String categoriaNombre;
    private String unidadMedida;
    private Integer stockDisponible;
    private Integer stockReservado;
    private Integer stockEnTransito;
    private Integer stockTotal;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private Integer puntoReorden;
    private String ubicacionAlmacen;
    private LocalDateTime fechaUltimoMovimiento;
    private LocalDateTime fechaUltimaActualizacion;
    private Integer diasSinVenta;
    private EstadoInventario estado;
    private Boolean necesitaReorden;
    private Boolean bajoPuntoMinimo;
    private String observaciones;
}
