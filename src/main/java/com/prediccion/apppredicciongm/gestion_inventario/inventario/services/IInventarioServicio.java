package com.prediccion.apppredicciongm.gestion_inventario.inventario.services;


import java.math.BigDecimal;

import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.models.Inventario.Inventario;

public interface IInventarioServicio {
    Inventario crearInventario(Inventario inventario);
    Inventario actualizarInventario(Integer inventarioId, Inventario inventario);
    void eliminarInventario(Integer inventarioId);
    Page<Inventario> listarInventarios(int pagina, int tamanioPagina);
    Inventario obtenerInventarioPorId(Integer inventarioId);
    Page<Inventario> buscarPorProducto(Integer productoId, int pagina, int tamanioPagina);
    void ajustarStock(Integer productoId, Integer cantidad);
    boolean necesitaReorden(Integer productoId);
    BigDecimal calcularValorInventario(Integer productoId);
}
