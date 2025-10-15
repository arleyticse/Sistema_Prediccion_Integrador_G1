package com.prediccion.apppredicciongm.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.repository.KardexRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexServicio implements IKardexService {

    private final KardexRepositorio kardexRepositorio;
    @Override
    public Kardex buscarPorProducto(Integer productoId) {
        return kardexRepositorio.findByProducto(productoId)
                .orElseThrow(() -> new RuntimeException("Kardex no encontrado para el producto ID: " + productoId));
    }

    @Override
    public Page<Kardex> listarMovimientos(int pagina, int tamanioPagina) {
        return kardexRepositorio.findAll(PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public Kardex registrarMovimiento(Kardex movimiento) {
        return kardexRepositorio.save(movimiento);
    }

    @Override
    public Integer calcularSaldo(Integer productoId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page<Kardex> listarMovimientosPorProducto(Integer productoId, int pagina, int tamanioPagina) {
        return kardexRepositorio.findAllByProducto(productoId, PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public Page<Kardex> listarMovimientosPorRangoFechas(Integer productoId, LocalDateTime inicio, LocalDateTime fin,
            int pagina, int tamanioPagina) {
        return kardexRepositorio.findAllByProductoAndFechaBetween(productoId, inicio, fin, PageRequest.of(pagina, tamanioPagina));
    }

    @Override
    public void recalcularSaldos(Integer productoId) {
        // TODO Auto-generated method stub
        
    }

    
}
