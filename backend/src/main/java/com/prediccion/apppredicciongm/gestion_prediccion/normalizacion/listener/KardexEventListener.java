package com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.prediccion.apppredicciongm.enums.TipoMovimiento;
import com.prediccion.apppredicciongm.gestion_prediccion.normalizacion.service.IReporteDemandaService;
import com.prediccion.apppredicciongm.models.Inventario.Kardex;

/**
 * Listener de eventos de Kardex.
 * Se ejecuta cuando se crean nuevos registros de Kardex tipo SALIDA_VENTA
 * para actualizar automáticamente el registro de demanda correspondiente.
 *
 * @author Sistema de Predicción
 * @version 1.0
 * @since 2025-10-20
 */
@Component
@Slf4j
public class KardexEventListener {

    @Autowired
    private IReporteDemandaService reporteDemandaService;

    /**
     * Escucha cuando se crea un nuevo Kardex.
     * Si es un SALIDA_VENTA (demanda de cliente), recalcula inmediatamente
     * el registro de demanda del día actual para el producto.
     *
     * @param kardex el movimiento de kardex creado
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKardexCreated(Kardex kardex) {
        if (kardex == null || kardex.getProducto() == null) {
            return;
        }

        // Solo procesar SALIDA_VENTA (demanda de cliente)
        if (kardex.getTipoMovimiento() == TipoMovimiento.SALIDA_VENTA && !kardex.isAnulado()) {
            log.debug("📦 [LISTENER] SALIDA_VENTA detectada. Producto: {} Cantidad: {}",
                    kardex.getProducto().getNombre(), kardex.getCantidad());

            try {
                // Recalcular demanda de los últimos 1 día para este producto
                int registrosActualizados = reporteDemandaService
                        .normalizarDemandaProducto(kardex.getProducto(), 1);

                log.info("✅ [LISTENER] Registro de demanda actualizado. Registros: {}",
                        registrosActualizados);

            } catch (Exception e) {
                log.warn("⚠️  [LISTENER] Error actualizando demanda para {}: {}",
                        kardex.getProducto().getNombre(), e.getMessage());
            }
        }
    }

    /**
     * Escucha cuando se anula un Kardex.
     * Si es un SALIDA_VENTA que se anula, recalcula la demanda del día.
     *
     * @param kardex el movimiento de kardex anulado
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKardexCancelled(Kardex kardex) {
        if (kardex == null || kardex.getProducto() == null) {
            return;
        }

        if (kardex.getTipoMovimiento() == TipoMovimiento.SALIDA_VENTA && kardex.isAnulado()) {
            log.debug("❌ [LISTENER] SALIDA_VENTA anulada. Producto: {} Cantidad: {}",
                    kardex.getProducto().getNombre(), kardex.getCantidad());

            try {
                // Recalcular demanda después de anular
                int registrosActualizados = reporteDemandaService
                        .normalizarDemandaProducto(kardex.getProducto(), 1);

                log.info("✅ [LISTENER] Registro de demanda recalculado tras anulación. Registros: {}",
                        registrosActualizados);

            } catch (Exception e) {
                log.warn("⚠️  [LISTENER] Error actualizando demanda tras anulación para {}: {}",
                        kardex.getProducto().getNombre(), e.getMessage());
            }
        }
    }
}
