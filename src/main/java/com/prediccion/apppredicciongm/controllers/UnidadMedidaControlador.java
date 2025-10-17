package com.prediccion.apppredicciongm.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prediccion.apppredicciongm.models.Inventario.UnidadMedida;
import com.prediccion.apppredicciongm.services.IUnidadeMedidaServicio;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
public class UnidadMedidaControlador {
    
    private final IUnidadeMedidaServicio unidadMedidaServicio;

    @GetMapping()
    public List<UnidadMedida> obtenerUnidadesMedida() {
        return unidadMedidaServicio.obtenerUnidadesMedida();
    }

    @PostMapping()
    public void crearUnidadMedida(@RequestBody UnidadMedida unidadMedida) {
        unidadMedidaServicio.crearUnidadMedida(unidadMedida);   
    }
    
    @DeleteMapping("/{id}")
    public void eliminarUnidadMedida(@PathVariable Integer id) {
        unidadMedidaServicio.eliminarUnidadMedida(id);
    }

    @PutMapping("/{id}")
    public void actualizarUnidadMedida(@PathVariable Integer id, @RequestBody UnidadMedida unidadMedida) {
        unidadMedidaServicio.actualizarUnidadMedida(id, unidadMedida);
    }
}
