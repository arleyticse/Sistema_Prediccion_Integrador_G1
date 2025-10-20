package com.prediccion.apppredicciongm.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Controlador para catálogos estáticos usados por el frontend (selects, dropdowns)
 */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoControlador {

    @GetMapping("/tipos-movimiento")
    public List<TipoDto> obtenerTiposMovimiento() {
        return Arrays.stream(TipoMovimiento.values())
                .map(t -> new TipoDto(t.name(), t.getDescripcion()))
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    static class TipoDto {
        private String valor;
        private String descripcion;
    }
}
