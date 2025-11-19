package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.service;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.ActualizarEstadoRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.request.CrearAlertaRequest;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaDashboardDTO;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.dto.response.AlertaInventarioResponse;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz del servicio para gestion de alertas de inventario.
 * 
 * Define operaciones CRUD y consultas especializadas para el
 * sistema de alertas de inventario.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
public interface IAlertaInventarioService {

    /**
     * Obtiene todas las alertas pendientes ordenadas por criticidad.
     * 
     * @return Lista de alertas pendientes
     */
    List<AlertaInventarioResponse> obtenerAlertasPendientes();

    /**
     * Obtiene alertas pendientes agrupadas por proveedor.
     * Optimizado para el dashboard con informacion plana.
     * 
     * @return Lista de alertas para dashboard
     */
    List<AlertaDashboardDTO> obtenerAlertasParaDashboard();

    /**
     * Agrupa alertas pendientes por proveedor.
     * 
     * @return Mapa con proveedor como clave y lista de alertas como valor
     */
    Map<String, List<AlertaDashboardDTO>> agruparAlertasPorProveedor();

    /**
     * Obtiene una alerta por su ID.
     * 
     * @param alertaId ID de la alerta
     * @return Alerta encontrada
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.AlertaInventarioNoEncontradaException si no existe
     */
    AlertaInventarioResponse obtenerAlertaPorId(Long alertaId);

    /**
     * Crea una nueva alerta de inventario manualmente.
     * 
     * @param request Datos de la nueva alerta
     * @return Alerta creada
     */
    AlertaInventarioResponse crearAlerta(CrearAlertaRequest request);

    /**
     * Actualiza el estado de una alerta.
     * 
     * @param alertaId ID de la alerta
     * @param request Datos del nuevo estado
     * @return Alerta actualizada
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.AlertaInventarioNoEncontradaException si no existe
     * @throws com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.errors.AlertaYaResueltaException si ya esta resuelta
     */
    AlertaInventarioResponse actualizarEstado(Long alertaId, ActualizarEstadoRequest request);

    /**
     * Marca una alerta individual como resuelta.
     * 
     * @param alertaId ID de la alerta
     * @param accionTomada Descripcion de la accion tomada
     */
    void marcarComoResuelta(Long alertaId, String accionTomada);

    /**
     * Marca multiples alertas como resueltas despues de procesar.
     * 
     * @param alertaIds Lista de IDs de alertas
     * @param accionTomada Descripcion de la accion tomada
     */
    void marcarAlertasComoResueltas(List<Long> alertaIds, String accionTomada);

    /**
     * Cuenta alertas pendientes por nivel de criticidad.
     * 
     * @param criticidad Nivel de criticidad
     * @return Numero de alertas pendientes
     */
    Long contarAlertasPendientesPorCriticidad(NivelCriticidad criticidad);

    /**
     * Obtiene estadisticas de alertas.
     * 
     * @return Mapa con estadisticas (total, por estado, por criticidad)
     */
    Map<String, Object> obtenerEstadisticas();

    /**
     * Lista todas las alertas del sistema.
     * 
     * @return Lista de todas las alertas
     */
    List<AlertaInventarioResponse> listarAlertas();

    /**
     * Lista alertas con filtros y paginacion.
     * 
     * @param estado Estado de la alerta
     * @param criticidad Nivel de criticidad
     * @param tipoAlerta Tipo de alerta
     * @param productoId ID del producto
     * @param proveedorId ID del proveedor
     * @param fechaDesde Fecha inicio
     * @param fechaHasta Fecha fin
     * @param pageable Configuracion de paginacion
     * @return Pagina de alertas
     */
    Page<AlertaInventarioResponse> listarAlertasFiltradas(
            EstadoAlerta estado,
            NivelCriticidad criticidad,
            TipoAlerta tipoAlerta,
            Integer productoId,
            Integer proveedorId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable
    );

    /**
     * Agrupa alertas pendientes por proveedor.
     * 
     * @return Mapa con proveedorId como clave y lista de alertas como valor
     */
    Map<Integer, List<AlertaInventarioResponse>> obtenerAlertasAgrupadasPorProveedor();

    /**
     * Actualiza el estado de una alerta con parametros separados.
     * 
     * @param alertaId ID de la alerta
     * @param nuevoEstado Nuevo estado
     * @param observaciones Observaciones
     * @param usuarioId ID del usuario
     * @return Alerta actualizada
     */
    AlertaInventarioResponse actualizarEstadoAlerta(
            Long alertaId,
            EstadoAlerta nuevoEstado,
            String observaciones,
            Integer usuarioId
    );

    /**
     * Marca multiples alertas como EN_PROCESO en batch.
     * 
     * @param alertaIds Lista de IDs
     * @param usuarioId Usuario responsable
     * @param observaciones Observaciones
     * @return Lista de alertas actualizadas
     */
    List<AlertaInventarioResponse> marcarAlertasEnProcesoBatch(
            List<Long> alertaIds,
            Integer usuarioId,
            String observaciones
    );

    /**
     * Resuelve multiples alertas en batch.
     * 
     * @param alertaIds Lista de IDs
     * @param accionTomada Accion tomada
     * @param usuarioId ID del usuario
     * @return Lista de alertas resueltas
     */
    List<AlertaInventarioResponse> resolverAlertasBatch(
            List<Long> alertaIds,
            String accionTomada,
            Integer usuarioId
    );

    /**
     * Ignora multiples alertas en batch.
     * 
     * @param alertaIds Lista de IDs
     * @param motivo Motivo
     * @param usuarioId ID del usuario
     * @return Lista de alertas ignoradas
     */
    List<AlertaInventarioResponse> ignorarAlertasBatch(
            List<Long> alertaIds,
            String motivo,
            Integer usuarioId
    );
}
