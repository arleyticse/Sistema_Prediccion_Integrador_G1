package com.prediccion.apppredicciongm.services;


import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.prediccion.apppredicciongm.models.Inventario.Kardex;

public interface IKardexService {
    Kardex registrarMovimiento(Kardex movimiento);
    Kardex buscarPorProducto(Integer productoId);
    Page<Kardex> listarMovimientos(int pagina, int tamanioPagina);
    Page<Kardex> listarMovimientosPorProducto(Integer productoId, int pagina, int tamanioPagina);
    Page<Kardex> listarMovimientosPorRangoFechas(Integer productoId, LocalDateTime inicio, LocalDateTime fin, int pagina, int tamanioPagina);
    Integer calcularSaldo(Integer productoId);
    void recalcularSaldos(Integer productoId);
}
