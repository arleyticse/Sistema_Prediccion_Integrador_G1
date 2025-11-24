package com.prediccion.apppredicciongm.gestion_reportes.service;

import com.prediccion.apppredicciongm.gestion_inventario.inventario.repository.IInventarioRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.prediccion.repository.IPrediccionRepositorio;
import com.prediccion.apppredicciongm.gestion_reportes.dto.response.ReporteInventarioDTO;
import com.prediccion.apppredicciongm.gestion_reportes.dto.response.ReportePrediccionDTO;
import com.prediccion.apppredicciongm.models.Inventario.Inventario;
import com.prediccion.apppredicciongm.models.Prediccion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteServiceImpl implements IReporteService {

    private final IPrediccionRepositorio prediccionRepositorio;
    private final IInventarioRepositorio inventarioRepositorio;

    @Override
    @Transactional(readOnly = true)
    public ReportePrediccionDTO generarReportePredicciones(LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de predicciones desde {} hasta {}", fechaInicio, fechaFin);

        LocalDateTime fechaInicioDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime fechaFinDateTime = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDateTime.now();

        List<Prediccion> predicciones = prediccionRepositorio.findByFechaEjecucionBetween(fechaInicioDateTime, fechaFinDateTime);

        if (predicciones.isEmpty()) {
            log.warn("No se encontraron predicciones en el periodo especificado");
            return construirReporteVacio();
        }

        ReportePrediccionDTO.ResumenGeneralPrediccion resumen = construirResumenPredicciones(predicciones, fechaInicioDateTime, fechaFinDateTime);
        List<ReportePrediccionDTO.PrediccionDetalle> detalles = construirDetallesPredicciones(predicciones);
        ReportePrediccionDTO.EstadisticasPrediccion estadisticas = construirEstadisticasPredicciones(predicciones);
        List<ReportePrediccionDTO.ProductoConPrediccion> topProductos = construirTopProductos(predicciones);

        return ReportePrediccionDTO.builder()
                .resumenGeneral(resumen)
                .predicciones(detalles)
                .estadisticas(estadisticas)
                .topProductos(topProductos)
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteInventarioDTO generarReporteInventario(Integer categoriaId) {
        log.info("Generando reporte de inventario para categoria: {}", categoriaId);

        List<Inventario> inventarios = categoriaId != null
                ? inventarioRepositorio.findByCategoria(categoriaId)
                : inventarioRepositorio.findAll();

        if (inventarios.isEmpty()) {
            log.warn("No se encontraron inventarios");
            return construirReporteInventarioVacio();
        }

        ReporteInventarioDTO.ResumenGeneralInventario resumen = construirResumenInventario(inventarios);
        List<ReporteInventarioDTO.InventarioDetalle> detalles = construirDetallesInventario(inventarios);
        ReporteInventarioDTO.EstadisticasInventario estadisticas = construirEstadisticasInventario(inventarios);
        List<ReporteInventarioDTO.ProductoCritico> productosCriticos = identificarProductosCriticos(inventarios);
        ReporteInventarioDTO.ValoracionInventario valoracion = calcularValoracionInventario(inventarios);

        return ReporteInventarioDTO.builder()
                .resumenGeneral(resumen)
                .inventarios(detalles)
                .estadisticas(estadisticas)
                .productosCriticos(productosCriticos)
                .valoracion(valoracion)
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportePrediccionDTO generarReportePrediccionPorProducto(Integer productoId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de predicciones para producto ID: {}", productoId);

        LocalDateTime fechaInicioDateTime = fechaInicio != null ? fechaInicio.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime fechaFinDateTime = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : LocalDateTime.now();

        List<Prediccion> predicciones = prediccionRepositorio.findByFechaEjecucionBetween(fechaInicioDateTime, fechaFinDateTime)
                .stream()
                .filter(p -> p.getProducto().getProductoId().equals(productoId))
                .collect(Collectors.toList());

        if (predicciones.isEmpty()) {
            log.warn("No se encontraron predicciones para el producto {}", productoId);
            return construirReporteVacio();
        }

        return generarReportePredicciones(fechaInicio, fechaFin);
    }

    private ReportePrediccionDTO.ResumenGeneralPrediccion construirResumenPredicciones(
            List<Prediccion> predicciones, LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        int excelentes = 0;
        int buenas = 0;
        int regulares = 0;
        int malas = 0;

        for (Prediccion pred : predicciones) {
            BigDecimal mape = pred.getMetricasError();
            if (mape == null) continue;

            double mapeValue = mape.doubleValue();
            if (mapeValue < 10) excelentes++;
            else if (mapeValue < 20) buenas++;
            else if (mapeValue < 30) regulares++;
            else malas++;
        }

        double porcentajeExito = predicciones.isEmpty() ? 0.0
                : ((double) (excelentes + buenas) / predicciones.size()) * 100;

        return ReportePrediccionDTO.ResumenGeneralPrediccion.builder()
                .totalPredicciones(predicciones.size())
                .prediccionesExcelentes(excelentes)
                .prediccionesBuenas(buenas)
                .prediccionesRegulares(regulares)
                .prediccionesMalas(malas)
                .porcentajeExito(Math.round(porcentajeExito * 100.0) / 100.0)
                .periodoAnalisis(fechaInicio.toLocalDate() + " al " + fechaFin.toLocalDate())
                .build();
    }

    private List<ReportePrediccionDTO.PrediccionDetalle> construirDetallesPredicciones(List<Prediccion> predicciones) {
        return predicciones.stream()
                .map(pred -> ReportePrediccionDTO.PrediccionDetalle.builder()
                        .prediccionId(pred.getPrediccionId())
                        .nombreProducto(pred.getProducto().getNombre())
                        .codigoProducto(String.valueOf(pred.getProducto().getProductoId()))
                        .categoria(pred.getProducto().getCategoria().getNombre())
                        .algoritmoUsado(pred.getAlgoritmoUsado())
                        .fechaEjecucion(pred.getFechaEjecucion())
                        .horizonteTiempo(pred.getHorizonteTiempo())
                        .demandaPredichaTotal(pred.getDemandaPredichaTotal())
                        .mape(pred.getMetricasError())
                        .rmse(BigDecimal.ZERO)
                        .mae(BigDecimal.ZERO)
                        .r2(BigDecimal.ZERO)
                        .nivelPrecision(calcularNivelPrecision(pred.getMetricasError()))
                        .nombreUsuario(pred.getUsuario() != null ? pred.getUsuario().getNombre() : "Sistema")
                        .build())
                .collect(Collectors.toList());
    }

    private ReportePrediccionDTO.EstadisticasPrediccion construirEstadisticasPredicciones(List<Prediccion> predicciones) {
        double mapePromedio = predicciones.stream()
                .filter(p -> p.getMetricasError() != null)
                .mapToDouble(p -> p.getMetricasError().doubleValue())
                .average()
                .orElse(0.0);

        Map<String, Long> algoritmos = predicciones.stream()
                .collect(Collectors.groupingBy(Prediccion::getAlgoritmoUsado, Collectors.counting()));

        String algoritmoMasUsado = algoritmos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        int demandaTotal = predicciones.stream()
                .mapToInt(Prediccion::getDemandaPredichaTotal)
                .sum();

        return ReportePrediccionDTO.EstadisticasPrediccion.builder()
                .mapePromedio(Math.round(mapePromedio * 100.0) / 100.0)
                .rmsePromedio(0.0)
                .maePromedio(0.0)
                .r2Promedio(0.0)
                .algoritmoMasUsado(algoritmoMasUsado)
                .cantidadPorAlgoritmo(algoritmos.getOrDefault(algoritmoMasUsado, 0L).intValue())
                .demandaTotalPredicha(demandaTotal)
                .build();
    }

    private List<ReportePrediccionDTO.ProductoConPrediccion> construirTopProductos(List<Prediccion> predicciones) {
        Map<Integer, List<Prediccion>> prediccionesPorProducto = predicciones.stream()
                .collect(Collectors.groupingBy(p -> p.getProducto().getProductoId()));

        return prediccionesPorProducto.entrySet().stream()
                .map(entry -> {
                    List<Prediccion> preds = entry.getValue();
                    Prediccion primera = preds.get(0);

                    double mapePromedio = preds.stream()
                            .filter(p -> p.getMetricasError() != null)
                            .mapToDouble(p -> p.getMetricasError().doubleValue())
                            .average()
                            .orElse(0.0);

                    int demandaTotal = preds.stream()
                            .mapToInt(Prediccion::getDemandaPredichaTotal)
                            .sum();

                    return ReportePrediccionDTO.ProductoConPrediccion.builder()
                            .nombreProducto(primera.getProducto().getNombre())
                            .codigoProducto(String.valueOf(primera.getProducto().getProductoId()))
                            .cantidadPredicciones(preds.size())
                            .mapePromedio(BigDecimal.valueOf(mapePromedio).setScale(2, RoundingMode.HALF_UP))
                            .demandaTotalPredicha(demandaTotal)
                            .categoria(primera.getProducto().getCategoria().getNombre())
                            .build();
                })
                .sorted(Comparator.comparingInt(ReportePrediccionDTO.ProductoConPrediccion::getCantidadPredicciones).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private ReporteInventarioDTO.ResumenGeneralInventario construirResumenInventario(List<Inventario> inventarios) {
        int totalProductos = inventarios.size();
        int productosConStock = (int) inventarios.stream().filter(i -> i.getStockDisponible() > 0).count();
        int productosSinStock = (int) inventarios.stream().filter(i -> i.getStockDisponible() == 0).count();
        int productosBajoMinimo = (int) inventarios.stream().filter(Inventario::bajoPuntoMinimo).count();
        int productosEnReorden = (int) inventarios.stream().filter(Inventario::necesitaReorden).count();
        int productosObsoletos = (int) inventarios.stream().filter(i -> i.getDiasSinVenta() != null && i.getDiasSinVenta() > 90).count();

        return ReporteInventarioDTO.ResumenGeneralInventario.builder()
                .totalProductos(totalProductos)
                .productosConStock(productosConStock)
                .productosSinStock(productosSinStock)
                .productosBajoMinimo(productosBajoMinimo)
                .productosEnReorden(productosEnReorden)
                .productosObsoletos(productosObsoletos)
                .periodoAnalisis(LocalDate.now().toString())
                .build();
    }

    private List<ReporteInventarioDTO.InventarioDetalle> construirDetallesInventario(List<Inventario> inventarios) {
        return inventarios.stream()
                .map(inv -> {
                    BigDecimal precioUnitario = inv.getProducto().getCostoAdquisicion();
                    BigDecimal valorStock = precioUnitario.multiply(BigDecimal.valueOf(inv.getStockTotal()));

                    return ReporteInventarioDTO.InventarioDetalle.builder()
                            .inventarioId(inv.getInventarioId())
                            .nombreProducto(inv.getProducto().getNombre())
                            .codigoProducto(String.valueOf(inv.getProducto().getProductoId()))
                            .categoria(inv.getProducto().getCategoria().getNombre())
                            .stockDisponible(inv.getStockDisponible())
                            .stockReservado(inv.getStockReservado())
                            .stockEnTransito(inv.getStockEnTransito())
                            .stockTotal(inv.getStockTotal())
                            .stockMinimo(inv.getStockMinimo())
                            .stockMaximo(inv.getStockMaximo())
                            .puntoReorden(inv.getPuntoReorden())
                            .ubicacionAlmacen(inv.getUbicacionAlmacen())
                            .fechaUltimoMovimiento(inv.getFechaUltimoMovimiento())
                            .diasSinVenta(inv.getDiasSinVenta())
                            .estado(inv.getEstado().toString())
                            .precioUnitario(precioUnitario)
                            .valorStock(valorStock)
                            .rotacion(0.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ReporteInventarioDTO.EstadisticasInventario construirEstadisticasInventario(List<Inventario> inventarios) {
        int stockTotalGeneral = inventarios.stream().mapToInt(Inventario::getStockTotal).sum();
        int stockDisponibleTotal = inventarios.stream().mapToInt(Inventario::getStockDisponible).sum();
        int stockReservadoTotal = inventarios.stream().mapToInt(Inventario::getStockReservado).sum();
        int stockEnTransitoTotal = inventarios.stream().mapToInt(Inventario::getStockEnTransito).sum();

        double diasPromedioSinVenta = inventarios.stream()
                .filter(i -> i.getDiasSinVenta() != null)
                .mapToInt(Inventario::getDiasSinVenta)
                .average()
                .orElse(0.0);

        Map<String, Integer> stockPorCategoria = inventarios.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getProducto().getCategoria().getNombre(),
                        Collectors.summingInt(Inventario::getStockTotal)
                ));

        String categoriaConMasStock = stockPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String categoriaConMenosStock = stockPorCategoria.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return ReporteInventarioDTO.EstadisticasInventario.builder()
                .stockTotalGeneral(stockTotalGeneral)
                .stockDisponibleTotal(stockDisponibleTotal)
                .stockReservadoTotal(stockReservadoTotal)
                .stockEnTransitoTotal(stockEnTransitoTotal)
                .rotacionPromedio(0.0)
                .diasPromedioSinVenta((int) Math.round(diasPromedioSinVenta))
                .categoriaConMasStock(categoriaConMasStock)
                .categoriaConMenosStock(categoriaConMenosStock)
                .build();
    }

    private List<ReporteInventarioDTO.ProductoCritico> identificarProductosCriticos(List<Inventario> inventarios) {
        return inventarios.stream()
                .filter(inv -> inv.bajoPuntoMinimo() || inv.necesitaReorden() ||
                        (inv.getDiasSinVenta() != null && inv.getDiasSinVenta() > 60))
                .map(inv -> {
                    String nivelCriticidad;
                    String razon;

                    if (inv.getStockDisponible() == 0) {
                        nivelCriticidad = "CRITICA";
                        razon = "Stock agotado";
                    } else if (inv.bajoPuntoMinimo()) {
                        nivelCriticidad = "ALTA";
                        razon = "Bajo stock minimo";
                    } else if (inv.necesitaReorden()) {
                        nivelCriticidad = "MEDIA";
                        razon = "Requiere reorden";
                    } else {
                        nivelCriticidad = "BAJA";
                        razon = "Sin movimiento";
                    }

                    return ReporteInventarioDTO.ProductoCritico.builder()
                            .nombreProducto(inv.getProducto().getNombre())
                            .codigoProducto(String.valueOf(inv.getProducto().getProductoId()))
                            .stockDisponible(inv.getStockDisponible())
                            .stockMinimo(inv.getStockMinimo())
                            .diasSinVenta(inv.getDiasSinVenta())
                            .nivelCriticidad(nivelCriticidad)
                            .razon(razon)
                            .categoria(inv.getProducto().getCategoria().getNombre())
                            .build();
                })
                .sorted(Comparator.comparing(ReporteInventarioDTO.ProductoCritico::getNivelCriticidad))
                .limit(20)
                .collect(Collectors.toList());
    }

    private ReporteInventarioDTO.ValoracionInventario calcularValoracionInventario(List<Inventario> inventarios) {
        BigDecimal valorTotalInventario = BigDecimal.ZERO;
        BigDecimal valorStockDisponible = BigDecimal.ZERO;
        BigDecimal valorStockReservado = BigDecimal.ZERO;
        BigDecimal valorStockEnTransito = BigDecimal.ZERO;

        for (Inventario inv : inventarios) {
            BigDecimal precio = inv.getProducto().getCostoAdquisicion();
            valorTotalInventario = valorTotalInventario.add(precio.multiply(BigDecimal.valueOf(inv.getStockTotal())));
            valorStockDisponible = valorStockDisponible.add(precio.multiply(BigDecimal.valueOf(inv.getStockDisponible())));
            valorStockReservado = valorStockReservado.add(precio.multiply(BigDecimal.valueOf(inv.getStockReservado())));
            valorStockEnTransito = valorStockEnTransito.add(precio.multiply(BigDecimal.valueOf(inv.getStockEnTransito())));
        }

        BigDecimal valorPromedioPorProducto = inventarios.isEmpty() ? BigDecimal.ZERO
                : valorTotalInventario.divide(BigDecimal.valueOf(inventarios.size()), 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> valorPorCategoria = inventarios.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getProducto().getCategoria().getNombre(),
                        Collectors.reducing(BigDecimal.ZERO,
                                i -> i.getProducto().getCostoAdquisicion().multiply(BigDecimal.valueOf(i.getStockTotal())),
                                BigDecimal::add)
                ));

        Map.Entry<String, BigDecimal> categoriaMaxima = valorPorCategoria.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        return ReporteInventarioDTO.ValoracionInventario.builder()
                .valorTotalInventario(valorTotalInventario.setScale(2, RoundingMode.HALF_UP))
                .valorStockDisponible(valorStockDisponible.setScale(2, RoundingMode.HALF_UP))
                .valorStockReservado(valorStockReservado.setScale(2, RoundingMode.HALF_UP))
                .valorStockEnTransito(valorStockEnTransito.setScale(2, RoundingMode.HALF_UP))
                .valorPromedioPorProducto(valorPromedioPorProducto)
                .categoriaConMayorValor(categoriaMaxima != null ? categoriaMaxima.getKey() : "N/A")
                .valorCategoriaMaxima(categoriaMaxima != null ? categoriaMaxima.getValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build();
    }

    private String calcularNivelPrecision(BigDecimal mape) {
        if (mape == null) return "N/A";
        double mapeValue = mape.doubleValue();
        if (mapeValue < 10) return "EXCELENTE";
        if (mapeValue < 20) return "BUENA";
        if (mapeValue < 30) return "REGULAR";
        return "MALA";
    }

    private ReportePrediccionDTO construirReporteVacio() {
        return ReportePrediccionDTO.builder()
                .resumenGeneral(ReportePrediccionDTO.ResumenGeneralPrediccion.builder()
                        .totalPredicciones(0)
                        .prediccionesExcelentes(0)
                        .prediccionesBuenas(0)
                        .prediccionesRegulares(0)
                        .prediccionesMalas(0)
                        .porcentajeExito(0.0)
                        .periodoAnalisis("Sin datos")
                        .build())
                .predicciones(Collections.emptyList())
                .estadisticas(ReportePrediccionDTO.EstadisticasPrediccion.builder()
                        .mapePromedio(0.0)
                        .rmsePromedio(0.0)
                        .maePromedio(0.0)
                        .r2Promedio(0.0)
                        .algoritmoMasUsado("N/A")
                        .cantidadPorAlgoritmo(0)
                        .demandaTotalPredicha(0)
                        .build())
                .topProductos(Collections.emptyList())
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }

    private ReporteInventarioDTO construirReporteInventarioVacio() {
        return ReporteInventarioDTO.builder()
                .resumenGeneral(ReporteInventarioDTO.ResumenGeneralInventario.builder()
                        .totalProductos(0)
                        .productosConStock(0)
                        .productosSinStock(0)
                        .productosBajoMinimo(0)
                        .productosEnReorden(0)
                        .productosObsoletos(0)
                        .periodoAnalisis(LocalDate.now().toString())
                        .build())
                .inventarios(Collections.emptyList())
                .estadisticas(ReporteInventarioDTO.EstadisticasInventario.builder()
                        .stockTotalGeneral(0)
                        .stockDisponibleTotal(0)
                        .stockReservadoTotal(0)
                        .stockEnTransitoTotal(0)
                        .rotacionPromedio(0.0)
                        .diasPromedioSinVenta(0)
                        .categoriaConMasStock("N/A")
                        .categoriaConMenosStock("N/A")
                        .build())
                .productosCriticos(Collections.emptyList())
                .valoracion(ReporteInventarioDTO.ValoracionInventario.builder()
                        .valorTotalInventario(BigDecimal.ZERO)
                        .valorStockDisponible(BigDecimal.ZERO)
                        .valorStockReservado(BigDecimal.ZERO)
                        .valorStockEnTransito(BigDecimal.ZERO)
                        .valorPromedioPorProducto(BigDecimal.ZERO)
                        .categoriaConMayorValor("N/A")
                        .valorCategoriaMaxima(BigDecimal.ZERO)
                        .build())
                .fechaGeneracion(LocalDateTime.now())
                .build();
    }
}
