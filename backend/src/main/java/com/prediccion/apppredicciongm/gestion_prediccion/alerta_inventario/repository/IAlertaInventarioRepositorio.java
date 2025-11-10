package com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.repository;

import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.EstadoAlerta;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.NivelCriticidad;
import com.prediccion.apppredicciongm.gestion_prediccion.alerta_inventario.enums.TipoAlerta;
import com.prediccion.apppredicciongm.models.AlertaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar las operaciones de persistencia de AlertaInventario.
 * 
 * Proporciona metodos de consulta personalizados para obtener alertas
 * segun diferentes criterios como estado, criticidad, producto y proveedor.
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 * @since 2025-11-06
 */
@Repository
public interface IAlertaInventarioRepositorio extends JpaRepository<AlertaInventario, Long> {

    /**
     * Obtiene todas las alertas con estado PENDIENTE.
     * 
     * @return Lista de alertas pendientes ordenadas por criticidad y fecha
     */
    @Query("SELECT a FROM AlertaInventario a " +
           "WHERE a.estado = 'PENDIENTE' " +
           "ORDER BY " +
           "CASE a.nivelCriticidad " +
           "  WHEN 'CRITICA' THEN 1 " +
           "  WHEN 'ALTA' THEN 2 " +
           "  WHEN 'MEDIA' THEN 3 " +
           "  WHEN 'BAJA' THEN 4 " +
           "END, " +
           "a.fechaGeneracion DESC")
    List<AlertaInventario> findAlertasPendientes();

    /**
     * Obtiene alertas por estado especifico.
     * 
     * @param estado Estado de la alerta a buscar
     * @return Lista de alertas con el estado indicado
     */
    List<AlertaInventario> findByEstado(EstadoAlerta estado);

    /**
     * Obtiene alertas por nivel de criticidad.
     * 
     * @param criticidad Nivel de criticidad a buscar
     * @return Lista de alertas con la criticidad indicada
     */
    List<AlertaInventario> findByNivelCriticidad(NivelCriticidad criticidad);

    /**
     * Obtiene alertas por tipo de alerta.
     * 
     * @param tipo Tipo de alerta a buscar
     * @return Lista de alertas del tipo indicado
     */
    List<AlertaInventario> findByTipoAlerta(TipoAlerta tipo);

    /**
     * Obtiene alertas de un producto especifico que estan pendientes.
     * 
     * @param productoId ID del producto
     * @return Lista de alertas pendientes para el producto
     */
    @Query("SELECT a FROM AlertaInventario a " +
           "WHERE a.producto.productoId = :productoId " +
           "AND a.estado = 'PENDIENTE'")
    List<AlertaInventario> findAlertasPendientesByProducto(@Param("productoId") Integer productoId);

    /**
     * Obtiene alertas pendientes agrupadas por proveedor.
     * Consulta las alertas y las ordena por el proveedor principal del producto.
     * Incluye EAGER FETCH de producto, categoria, unidadMedida, proveedorPrincipal e inventario.
     * 
     * @return Lista de alertas pendientes con informacion completa cargada
     */
    @Query("SELECT DISTINCT a FROM AlertaInventario a " +
           "LEFT JOIN FETCH a.producto p " +
           "LEFT JOIN FETCH p.categoria " +
           "LEFT JOIN FETCH p.unidadMedida " +
           "LEFT JOIN FETCH p.proveedorPrincipal " +
           "WHERE a.estado = 'PENDIENTE' " +
           "ORDER BY " +
           "CASE a.nivelCriticidad " +
           "  WHEN 'CRITICA' THEN 1 " +
           "  WHEN 'ALTA' THEN 2 " +
           "  WHEN 'MEDIA' THEN 3 " +
           "  WHEN 'BAJA' THEN 4 " +
           "END, " +
           "a.fechaGeneracion DESC")
    List<AlertaInventario> findAlertasPendientesConProducto();

    /**
     * Obtiene alertas por estado y nivel de criticidad.
     * 
     * @param estado Estado de la alerta
     * @param criticidad Nivel de criticidad
     * @return Lista de alertas que cumplen ambos criterios
     */
    List<AlertaInventario> findByEstadoAndNivelCriticidad(
        EstadoAlerta estado, 
        NivelCriticidad criticidad
    );

    /**
     * Cuenta el numero de alertas pendientes.
     * 
     * @return Numero de alertas con estado PENDIENTE
     */
    @Query("SELECT COUNT(a) FROM AlertaInventario a WHERE a.estado = 'PENDIENTE'")
    Long contarAlertasPendientes();

    /**
     * Cuenta alertas pendientes por nivel de criticidad.
     * 
     * @param criticidad Nivel de criticidad
     * @return Numero de alertas pendientes con esa criticidad
     */
    @Query("SELECT COUNT(a) FROM AlertaInventario a " +
           "WHERE a.estado = 'PENDIENTE' " +
           "AND a.nivelCriticidad = :criticidad")
    Long contarAlertasPendientesPorCriticidad(@Param("criticidad") NivelCriticidad criticidad);

    /**
     * Obtiene alertas generadas en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de alertas generadas en el rango
     */
    List<AlertaInventario> findByFechaGeneracionBetween(
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin
    );

    /**
     * Verifica si existe una alerta pendiente para un producto y tipo especifico.
     * Evita duplicacion de alertas para la misma condicion.
     * 
     * @param productoId ID del producto
     * @param tipoAlerta Tipo de alerta
     * @return true si existe una alerta pendiente, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM AlertaInventario a " +
           "WHERE a.producto.productoId = :productoId " +
           "AND a.tipoAlerta = :tipoAlerta " +
           "AND a.estado = 'PENDIENTE'")
    boolean existeAlertaPendiente(
        @Param("productoId") Integer productoId,
        @Param("tipoAlerta") TipoAlerta tipoAlerta
    );

    /**
     * Obtiene alertas asignadas a un usuario especifico.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de alertas asignadas al usuario
     */
    @Query("SELECT a FROM AlertaInventario a " +
           "WHERE a.usuarioAsignado.usuarioId = :usuarioId " +
           "AND a.estado IN ('PENDIENTE', 'EN_PROCESO') " +
           "ORDER BY a.nivelCriticidad, a.fechaGeneracion DESC")
    List<AlertaInventario> findAlertasAsignadasAUsuario(@Param("usuarioId") Integer usuarioId);
}
