package com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.service;

import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.request.CalculoObtimizacionCreateRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.dto.response.CalculoOptimizacionResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.errors.CalculoObtimizacionNoEncontradoException;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.mapper.CalculoObtimizacionMapper;
import com.prediccion.apppredicciongm.gestion_prediccion.calculo_optimizacion.repository.ICalculoObtimizacionRepositorio;
import com.prediccion.apppredicciongm.models.CalculoObtimizacion;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Servicio para gestión de CalculoObtimizacion
 * Implementa lógica de cálculo de EOQ (Economic Order Quantity)
 * y ROP (Reorder Point) para optimización de inventario
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CalculoObtimizacionService implements ICalculoObtimizacionServicio {

    private final ICalculoObtimizacionRepositorio calculoRepositorio;
    private final IProductoRepositorio productoRepositorio;
    private final CalculoObtimizacionMapper mapper;

    private static final String CALCULO_NO_ENCONTRADO = "Cálculo de optimización no encontrado con ID: ";
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";

    /**
     * Calcula EOQ y ROP para un producto
     * Fórmulas:
     * - EOQ = √(2DS/H) donde D=demanda, S=costo pedido, H=costo mantenimiento
     * - ROP = L × D / 365 donde L=lead time, D=demanda anual
     */
    @Override
    public CalculoOptimizacionResponse calcularObtimizacion(Integer productoId, CalculoObtimizacionCreateRequest request) {
        log.info("Calculando optimización para producto: {}", productoId);

        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RuntimeException(PRODUCTO_NO_ENCONTRADO + productoId));

        // Cálculo de EOQ: √(2 * D * S / H)
        double demanda = request.getDemandaAnualEstimada().doubleValue();
        double costoPedido = request.getCostoPedido().doubleValue();
        double costoMantenimiento = request.getCostoMantenimiento().doubleValue();

        double eoqCalculo = Math.sqrt((2 * demanda * costoPedido) / costoMantenimiento);
        Integer eoqCantidadOptima = (int) Math.ceil(eoqCalculo);

        // Cálculo de ROP: L * D / 365
        double leadTime = request.getDiasLeadTime().doubleValue();
        double ropCalculo = (leadTime * demanda) / 365;
        Integer ropPuntoReorden = (int) Math.ceil(ropCalculo);

        // Stock de seguridad
        Integer stockSeguridad = request.getStockSeguridad() != null ? request.getStockSeguridad() : ropPuntoReorden / 2;

        // Cálculo de número de órdenes anuales
        Integer numeroOrdenesAnuales = (int) Math.ceil(demanda / eoqCantidadOptima);

        // Días entre lotes
        Integer diasEntreLotes = (int) Math.ceil(365.0 / numeroOrdenesAnuales);

        // Costo total de inventario
        BigDecimal costoOrdenesAnuales = BigDecimal.valueOf(numeroOrdenesAnuales)
                .multiply(request.getCostoPedido());
        BigDecimal costoMantenimientoInventario = BigDecimal.valueOf(eoqCantidadOptima / 2.0)
                .multiply(request.getCostoMantenimiento());
        BigDecimal costoTotalInventario = costoOrdenesAnuales.add(costoMantenimientoInventario)
                .setScale(2, RoundingMode.HALF_UP);

        // Crear y guardar cálculo
        CalculoObtimizacion calculo = CalculoObtimizacion.builder()
                .producto(producto)
                .demandaAnualEstimada(request.getDemandaAnualEstimada())
                .eoqCantidadOptima(eoqCantidadOptima)
                .ropPuntoReorden(ropPuntoReorden)
                .stockSeguridadSugerido(stockSeguridad)
                .costoTotalInventario(costoTotalInventario)
                .costoMantenimiento(request.getCostoMantenimiento())
                .costoPedido(request.getCostoPedido())
                .diasLeadTime(request.getDiasLeadTime())
                .costoUnitario(request.getCostoUnitario())
                .numeroOrdenesAnuales(numeroOrdenesAnuales)
                .diasEntreLotes(diasEntreLotes)
                .observaciones(request.getObservaciones())
                .fechaCalculo(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        CalculoObtimizacion guardado = calculoRepositorio.save(calculo);

        log.info("Optimización calculada - EOQ: {}, ROP: {}, Costo Total: {}", 
            eoqCantidadOptima, ropPuntoReorden, costoTotalInventario);

        return mapper.toResponse(guardado);
    }

    /**
     * Obtiene un cálculo por ID
     */
    @Override
    @Transactional(readOnly = true)
    public CalculoOptimizacionResponse obtenerCalculoPorId(Integer calculoId) {
        log.debug("Obteniendo cálculo ID: {}", calculoId);

        CalculoObtimizacion calculo = calculoRepositorio.findById(calculoId)
                .orElseThrow(() -> new CalculoObtimizacionNoEncontradoException(
                    CALCULO_NO_ENCONTRADO + calculoId));

        return mapper.toResponse(calculo);
    }

    /**
     * Obtiene el último cálculo de un producto
     */
    @Override
    @Transactional(readOnly = true)
    public CalculoOptimizacionResponse obtenerUltimoCalculoPorProducto(Integer productoId) {
        log.debug("Obteniendo último cálculo para producto: {}", productoId);

        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RuntimeException(PRODUCTO_NO_ENCONTRADO + productoId));

        CalculoObtimizacion calculo = calculoRepositorio.findFirstByProductoOrderByFechaCalculoDesc(producto)
                .orElseThrow(() -> new CalculoObtimizacionNoEncontradoException(
                    "No hay cálculos para el producto: " + productoId));

        return mapper.toResponse(calculo);
    }

    /**
     * Lista cálculos de un producto
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CalculoOptimizacionResponse> listarCalculosPorProducto(Integer productoId, Pageable pageable) {
        log.debug("Listando cálculos para producto: {}", productoId);

        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new RuntimeException(PRODUCTO_NO_ENCONTRADO + productoId));

        return calculoRepositorio.findByProductoOrderByFechaCalculoDesc(producto, pageable)
                .map(mapper::toResponse);
    }

    /**
     * Lista todos los cálculos
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CalculoOptimizacionResponse> listarTodosLosCalculos(Pageable pageable) {
        log.debug("Listando todos los cálculos");

        return calculoRepositorio.findAll(pageable)
                .map(mapper::toResponse);
    }

    /**
     * Actualiza un cálculo existente
     */
    @Override
    public CalculoOptimizacionResponse actualizarCalculo(Integer calculoId, CalculoObtimizacionCreateRequest request) {
        log.info("Actualizando cálculo ID: {}", calculoId);

        CalculoObtimizacion calculo = calculoRepositorio.findById(calculoId)
                .orElseThrow(() -> new CalculoObtimizacionNoEncontradoException(
                    CALCULO_NO_ENCONTRADO + calculoId));

        calculo.setDemandaAnualEstimada(request.getDemandaAnualEstimada());
        calculo.setCostoMantenimiento(request.getCostoMantenimiento());
        calculo.setCostoPedido(request.getCostoPedido());
        calculo.setDiasLeadTime(request.getDiasLeadTime());
        calculo.setCostoUnitario(request.getCostoUnitario());
        calculo.setStockSeguridad(request.getStockSeguridad());
        calculo.setObservaciones(request.getObservaciones());
        calculo.setFechaActualizacion(LocalDateTime.now());

        // Recalcular EOQ y ROP
        double demanda = request.getDemandaAnualEstimada().doubleValue();
        double eoqCalculo = Math.sqrt((2 * demanda * request.getCostoPedido().doubleValue()) 
                / request.getCostoMantenimiento().doubleValue());
        double ropCalculo = (request.getDiasLeadTime() * demanda) / 365;

        calculo.setEoqCantidadOptima((int) Math.ceil(eoqCalculo));
        calculo.setRopPuntoReorden((int) Math.ceil(ropCalculo));

        CalculoObtimizacion actualizado = calculoRepositorio.save(calculo);

        log.info("Cálculo actualizado correctamente");

        return mapper.toResponse(actualizado);
    }

    /**
     * Elimina un cálculo
     */
    @Override
    public void eliminarCalculo(Integer calculoId) {
        log.info("Eliminando cálculo ID: {}", calculoId);

        if (!calculoRepositorio.existsById(calculoId)) {
            throw new CalculoObtimizacionNoEncontradoException(
                CALCULO_NO_ENCONTRADO + calculoId);
        }

        calculoRepositorio.deleteById(calculoId);
        log.info("Cálculo eliminado correctamente");
    }

    /**
     * Recalcula para todas las predicciones
     */
    @Override
    public void recalcularParaTodasLasPredicciones() {
        log.info("Recalculando optimizaciones para todos los productos");
        // Implementar lógica según necesidad
        log.info("Recalculo completado");
    }
}
