package com.prediccion.apppredicciongm.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prediccion.apppredicciongm.models.Proveedor;
import com.prediccion.apppredicciongm.services.IProveedorServicio;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorControlador {
    private final IProveedorServicio proveedorServicio;

    @Operation(summary = "Obtener todos los proveedores")
    @GetMapping()
    public List<Proveedor> obtenerProveedores() {
        return proveedorServicio.obtenerProveedores();
    }

    @PostMapping()
    public void crearProveedor(@RequestBody @Valid Proveedor proveedor) {
        proveedorServicio.crearProveedor(proveedor);
    }

    @DeleteMapping("/{id}")
    public void eliminarProveedor(@PathVariable Integer id) {
        proveedorServicio.eliminarProveedor(id);
    }

    @PutMapping("/{id}")
    public void actualizarProveedor(@PathVariable Integer id, @RequestBody @Valid Proveedor proveedor) {
        proveedorServicio.actualizarProveedor(id, proveedor);
    }
}
