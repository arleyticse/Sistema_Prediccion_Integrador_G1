package com.prediccion.apppredicciongm.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prediccion.apppredicciongm.models.Inventario.Categoria;
import com.prediccion.apppredicciongm.services.ICategoriaServicio;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaControlador {
    
    private final ICategoriaServicio categoriaServicio;

    @Operation(summary = "Obtener todas las categorías")
    @GetMapping()
    public List<Categoria> obtenerCategorias() {
        return categoriaServicio.obtenerCategorias();
    }

    @PostMapping()
    public void crearCategoria(@RequestBody  @Valid Categoria categoria) {
        categoriaServicio.crearCategoria(categoria);
    }

    @DeleteMapping("/{id}")
    public void eliminarCategoria(@PathVariable Integer id) {
        categoriaServicio.eliminarCategoria(id);
    }

   @PutMapping("/{id}")
    public void actualizarCategoria(@PathVariable Integer id, @RequestBody @Valid Categoria categoria) {
        categoriaServicio.actualizarCategoria(id, categoria);
    }
}