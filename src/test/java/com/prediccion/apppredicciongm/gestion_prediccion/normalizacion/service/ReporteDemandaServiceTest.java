package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_inventario.movimiento.repository.IKardexRepositorio;
import com.prediccion.apppredicciongm.gestion_inventario.producto.repository.IProductoRepositorio;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.repository.IRegistroDemandaRepositorio;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;
import com.prediccion.apppredicciongm.models.Inventario.Producto;
import com.prediccion.apppredicciongm.models.RegistroDemanda;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Tests unitarios para ReporteDemandaService.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
@DisplayName("ReporteDemandaService Tests")
public class ReporteDemandaServiceTest {

    @Mock
    private IKardexRepositorio kardexRepositorio;

    @Mock
    private IProductoRepositorio productoRepositorio;

    @Mock
    private IRegistroDemandaRepositorio registroDemandaRepositorio;

    @InjectMocks
    private ReporteDemandaService reporteDemandaService;

    private Producto producto;
    private Kardex kardexVenta;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear datos de prueba
        producto = new Producto();
        producto.setProductoId(1);
        producto.setNombre("Producto Test");

        kardexVenta = new Kardex();
        kardexVenta.setKardexId(1L);
        kardexVenta.setProducto(producto);
        kardexVenta.setTipoMovimiento(TipoMovimiento.SALIDA_VENTA);
        kardexVenta.setCantidad(100);
        kardexVenta.setFechaMovimiento(LocalDateTime.now());
        kardexVenta.setAnulado(false);
    }

    @Test
    @DisplayName("Debe procesar SALIDA_VENTA y crear registro de demanda")
    public void testNormalizarDemandaProducto_Success() {
        // Arrange
        List<Kardex> movimientos = new ArrayList<>();
        movimientos.add(kardexVenta);

        Page<Kardex> page = new PageImpl<>(movimientos);
        when(kardexRepositorio.findAllByProductoAndFechaBetween(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        when(registroDemandaRepositorio.findByProductoAndFecha(any(Producto.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act
        int resultado = reporteDemandaService.normalizarDemandaProducto(producto, 30);

        // Assert
        assertEquals(1, resultado);
        verify(registroDemandaRepositorio, times(1)).save(any(RegistroDemanda.class));
    }

    @Test
    @DisplayName("Debe ignorar movimientos anulados")
    public void testNormalizarDemandaProducto_IgnoraAnulados() {
        // Arrange
        kardexVenta.setAnulado(true);
        List<Kardex> movimientos = new ArrayList<>();
        movimientos.add(kardexVenta);

        Page<Kardex> page = new PageImpl<>(movimientos);
        when(kardexRepositorio.findAllByProductoAndFechaBetween(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        int resultado = reporteDemandaService.normalizarDemandaProducto(producto, 30);

        // Assert
        assertEquals(0, resultado);
        verify(registroDemandaRepositorio, never()).save(any(RegistroDemanda.class));
    }

    @Test
    @DisplayName("Debe ignorar otros tipos de movimiento (no SALIDA_VENTA)")
    public void testNormalizarDemandaProducto_IgnoraOtrosTipos() {
        // Arrange
        kardexVenta.setTipoMovimiento(TipoMovimiento.ENTRADA_COMPRA);
        List<Kardex> movimientos = new ArrayList<>();
        movimientos.add(kardexVenta);

        Page<Kardex> page = new PageImpl<>(movimientos);
        when(kardexRepositorio.findAllByProductoAndFechaBetween(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        int resultado = reporteDemandaService.normalizarDemandaProducto(producto, 30);

        // Assert
        assertEquals(0, resultado);
        verify(registroDemandaRepositorio, never()).save(any(RegistroDemanda.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si producto es nulo")
    public void testNormalizarDemandaProducto_ProductoNulo() {
        // Assert & Act
        assertThrows(Exception.class, () -> {
            reporteDemandaService.normalizarDemandaProducto(null, 30);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción si diasProcesar es menor a 1")
    public void testNormalizarDemandaProducto_DiasProcesarInvalido() {
        // Assert & Act
        assertThrows(Exception.class, () -> {
            reporteDemandaService.normalizarDemandaProducto(producto, 0);
        });
    }

    @Test
    @DisplayName("Debe validar hay datos suficientes (>= 12 registros)")
    public void testHayDatosSuficientes() {
        // Arrange
        when(registroDemandaRepositorio.countByProducto(producto)).thenReturn(12L);

        // Act
        boolean resultado = reporteDemandaService.hayDatosSuficientes(producto);

        // Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Debe indicar datos insuficientes (< 12 registros)")
    public void testHayDatosInsuficientes() {
        // Arrange
        when(registroDemandaRepositorio.countByProducto(producto)).thenReturn(5L);

        // Act
        boolean resultado = reporteDemandaService.hayDatosSuficientes(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Debe contar registros correctamente")
    public void testObtenerCantidadDatosHistoricos() {
        // Arrange
        when(registroDemandaRepositorio.countByProducto(producto)).thenReturn(25L);

        // Act
        long resultado = reporteDemandaService.obtenerCantidadDatosHistoricos(producto);

        // Assert
        assertEquals(25L, resultado);
    }

    @Test
    @DisplayName("Debe eliminar registros correctamente")
    public void testLimpiarDemandaProducto() {
        // Arrange
        when(registroDemandaRepositorio.countByProducto(producto)).thenReturn(10L);
        doNothing().when(registroDemandaRepositorio).deleteByProducto(producto);

        // Act
        int resultado = reporteDemandaService.limpiarDemandaProducto(producto);

        // Assert
        assertEquals(10, resultado);
        verify(registroDemandaRepositorio, times(1)).deleteByProducto(producto);
    }
}
