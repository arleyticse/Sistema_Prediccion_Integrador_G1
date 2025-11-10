import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG Components
import { TableModule } from 'primeng/table';
import { TreeTableModule } from 'primeng/treetable';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Select } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { CardModule } from 'primeng/card';
import { ToolbarModule } from 'primeng/toolbar';
import { TooltipModule } from 'primeng/tooltip';
import { DataViewModule } from 'primeng/dataview';
import { DividerModule } from 'primeng/divider';
import { BadgeModule } from 'primeng/badge';

// PrimeNG Services
import { ConfirmationService, MessageService } from 'primeng/api';
import { TreeNode } from 'primeng/api';

// Models
import { 
  AlertaInventario, 
  AlertaDashboard,
  AlertasProveedor 
} from '../../models/AlertaInventario';
import { NivelCriticidad, CriticidadSeverity, CriticidadIcon } from '../../models/NivelCriticidad';
import { TipoAlerta, TipoAlertaLabels } from '../../models/TipoAlerta';
import { EstadoAlerta, EstadoAlertaSeverity, EstadoAlertaLabels } from '../../models/EstadoAlerta';
import { 
  ProcesarAlertasRequest, 
  ProcesamientoBatchResponse 
} from '../../models/ProcesamientoAlerta';
import { ResumenOrden } from '../../models/ResumenOrden';

// Services
import { AlertaInventarioService } from '../../services/alerta-inventario.service';
import { Router } from '@angular/router';

/**
 * Dashboard de Alertas de Inventario.
 * 
 * Características principales:
 * - Tabla agrupada por proveedor (Row Grouping)
 * - Selección múltiple de alertas
 * - Filtros por criticidad, tipo y estado
 * - Búsqueda global
 * - Procesamiento automático batch
 * - Visualización de resultados
 * 
 * @author Sistema de Prediccion
 * @version 1.0
 */
@Component({
  selector: 'app-dashboard-alertas',
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    TreeTableModule,
    ButtonModule,
    TagModule,
    Select,
    InputTextModule,
    IconField,
    InputIcon,
    ConfirmDialogModule,
    ToastModule,
    DialogModule,
    ProgressSpinnerModule,
    CardModule,
    ToolbarModule,
    TooltipModule,
    DataViewModule,
    DividerModule,
    BadgeModule
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './dashboard-alertas.html',
  styleUrl: './dashboard-alertas.css'
})
export default class DashboardAlertasComponent implements OnInit {
  
  // Signals para estado reactivo
  alertas = signal<AlertaDashboard[]>([]);
  alertasAgrupadas = signal<AlertasProveedor[]>([]);
  alertasSeleccionadas = signal<AlertaDashboard[]>([]);
  cargando = signal<boolean>(false);
  procesando = signal<boolean>(false);
  
  // TreeTable signals
  alertasTreeNodes = signal<TreeNode[]>([]);
  expandedKeys = signal<{ [key: string]: boolean }>({});
  
  // Filtros
  filtroGlobal = signal<string>('');
  filtroCriticidad = signal<NivelCriticidad | null>(null);
  filtroTipo = signal<TipoAlerta | null>(null);
  filtroEstado = signal<EstadoAlerta | null>(null);
  
  // Dialog de resultados
  dialogResultadosVisible = signal<boolean>(false);
  resultadoProcesamiento = signal<ProcesamientoBatchResponse | null>(null);
  resumenOrdenes = signal<ResumenOrden[]>([]);
  cargandoOrdenes = signal<boolean>(false);
  
  // Control de expansión de productos en órdenes
  productosOrdenExpandidos = signal<Record<number, boolean>>({});
  
  // Opciones para filtros
  opcionesCriticidad = Object.values(NivelCriticidad).map(c => ({
    label: c,
    value: c
  }));
  
  opcionesTipo = Object.entries(TipoAlertaLabels).map(([value, label]) => ({
    label,
    value: value as TipoAlerta
  }));
  
  opcionesEstado = Object.entries(EstadoAlertaLabels).map(([value, label]) => ({
    label,
    value: value as EstadoAlerta
  }));
  
  // Computed para estadísticas
  totalAlertas = computed(() => this.alertas().length);
  totalSeleccionadas = computed(() => this.alertasSeleccionadas().length);
  alertasCriticas = computed(() => 
    this.alertas().filter(a => a.nivelCriticidad === NivelCriticidad.CRITICA).length
  );
  alertasAltas = computed(() => 
    this.alertas().filter(a => a.nivelCriticidad === NivelCriticidad.ALTA).length
  );
  
  constructor(
    private alertaService: AlertaInventarioService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.cargarAlertas();
  }
  
  /**
   * Carga alertas pendientes desde el backend.
   */
  cargarAlertas(): void {
    this.cargando.set(true);
    
    // Usar el endpoint específico del dashboard que ya incluye datos de proveedor
    this.alertaService.obtenerAlertasDashboard().subscribe({
      next: (alertasDashboard: AlertaDashboard[]) => {
        // Mapear datos del proveedor desde producto.proveedor a nivel raiz
        const alertasConProveedor = alertasDashboard.map(alerta => ({
          ...alerta,
          proveedorId: alerta.producto.proveedor?.proveedorId,
          proveedorNombreComercial: alerta.producto.proveedor?.nombreComercial || 'Sin proveedor',
          proveedorTiempoEntrega: alerta.producto.proveedor?.tiempoEntregaDias
        }));
        
        this.alertas.set(alertasConProveedor);
        this.agruparPorProveedor();
        this.cargando.set(false);
        
        this.messageService.add({
          severity: 'success',
          summary: 'Alertas cargadas',
          detail: `Se encontraron ${alertasConProveedor.length} alertas pendientes`,
          life: 3000
        });
      },
      error: (error: any) => {
        console.error('Error al cargar alertas:', error);
        this.cargando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las alertas',
          life: 5000
        });
      }
    });
  }
  
  /**
   * Agrupa alertas por proveedor para el row grouping.
   */
  private agruparPorProveedor(): void {
    const grupos = new Map<number, AlertaDashboard[]>();
    
    this.alertas().forEach(alerta => {
      if (alerta.proveedorId) {
        const grupo = grupos.get(alerta.proveedorId) || [];
        grupo.push(alerta);
        grupos.set(alerta.proveedorId, grupo);
      }
    });
    
    const agrupadas: AlertasProveedor[] = Array.from(grupos.entries()).map(
      ([proveedorId, alertas]) => {
        const cantidadTotal = alertas.reduce(
          (sum, a) => sum + (a.cantidadSugerida || 0), 
          0
        );
        
        const porCriticidad: Record<string, number> = {};
        alertas.forEach(a => {
          porCriticidad[a.nivelCriticidad] = 
            (porCriticidad[a.nivelCriticidad] || 0) + 1;
        });
        
        return {
          proveedorId,
          proveedorNombre: alertas[0].proveedorNombreComercial || 'Sin nombre',
          alertas,
          totalAlertas: alertas.length,
          cantidadTotalSugerida: cantidadTotal,
          alertasPorCriticidad: porCriticidad
        };
      }
    );
    
    this.alertasAgrupadas.set(agrupadas);
    this.transformarATreeTable();
  }
  
  /**
   * Transforma alertas agrupadas a estructura TreeNode para TreeTable.
   * Nodo padre = Proveedor con datos agregados
   * Nodos hijos = Alertas individuales del proveedor
   */
  private transformarATreeTable(): void {
    const treeNodes: TreeNode[] = this.alertasAgrupadas().map(grupo => {
      // Calcular total estimado de compra por proveedor
      const totalCompra = grupo.alertas.reduce(
        (sum, alerta) => {
          const cantidad = alerta.cantidadSugerida || 0;
          // Usar costoAdquisicion del nivel raíz de AlertaDashboard
          const costo = (alerta as any).costoAdquisicion || 0;
          return sum + (cantidad * costo);
        },
        0
      );

      // Nodo padre: Proveedor
      const nodoPadre: TreeNode = {
        key: `proveedor-${grupo.proveedorId}`,
        data: {
          tipo: 'proveedor',
          proveedorId: grupo.proveedorId,
          proveedorNombre: grupo.proveedorNombre,
          totalProductos: grupo.totalAlertas,
          cantidadTotal: grupo.cantidadTotalSugerida,
          totalCompra: totalCompra,
          alertasCriticas: grupo.alertasPorCriticidad[NivelCriticidad.CRITICA] || 0,
          alertasAltas: grupo.alertasPorCriticidad[NivelCriticidad.ALTA] || 0,
          alertasMedias: grupo.alertasPorCriticidad[NivelCriticidad.MEDIA] || 0
        },
        children: []
      };

      // Nodos hijos: Alertas individuales
      nodoPadre.children = grupo.alertas.map((alerta, idx) => ({
        key: `alerta-${alerta.alertaId}`,
        data: {
          tipo: 'alerta',
          ...alerta
        }
      }));

      return nodoPadre;
    });

    this.alertasTreeNodes.set(treeNodes);
    
    // Dejar todos colapsados inicialmente para mejor rendimiento
    // El usuario expande según necesite
    this.expandedKeys.set({});
  }
  
  /**
   * Calcula totales por proveedor para el group footer.
   */
  calcularTotalProveedor(proveedorNombre: string): number {
    const grupo = this.alertasAgrupadas().find(
      g => g.proveedorNombre === proveedorNombre
    );
    return grupo?.totalAlertas || 0;
  }
  
  /**
   * Calcula cantidad total sugerida por proveedor.
   */
  calcularCantidadTotalProveedor(proveedorNombre: string): number {
    const grupo = this.alertasAgrupadas().find(
      g => g.proveedorNombre === proveedorNombre
    );
    return grupo?.cantidadTotalSugerida || 0;
  }
  
  /**
   * Aplica filtro global en la tabla.
   */
  aplicarFiltroGlobal(event: Event, dt: any): void {
    const valor = (event.target as HTMLInputElement).value;
    this.filtroGlobal.set(valor);
    dt.filterGlobal(valor, 'contains');
  }
  
  /**
   * Limpia todos los filtros aplicados.
   */
  limpiarFiltros(dt: any): void {
    dt.clear();
    this.filtroGlobal.set('');
    this.filtroCriticidad.set(null);
    this.filtroTipo.set(null);
    this.filtroEstado.set(null);
  }
  
  /**
   * Obtiene severidad de PrimeNG para criticidad.
   */
  getSeverityCriticidad(criticidad: NivelCriticidad): 'success' | 'info' | 'warn' | 'danger' {
    return CriticidadSeverity[criticidad] as 'success' | 'info' | 'warn' | 'danger';
  }
  
  /**
   * Obtiene icono para criticidad.
   */
  getIconCriticidad(criticidad: NivelCriticidad): string {
    return CriticidadIcon[criticidad];
  }
  
  /**
   * Obtiene severidad para estado de alerta.
   */
  getSeverityEstado(estado: EstadoAlerta): 'success' | 'secondary' | 'info' | 'warn' | 'danger' {
    return EstadoAlertaSeverity[estado] as 'success' | 'secondary' | 'info' | 'warn' | 'danger';
  }
  
  /**
   * Obtiene label para tipo de alerta.
   */
  getLabelTipo(tipo: TipoAlerta): string {
    return TipoAlertaLabels[tipo];
  }
  
  /**
   * Marca alertas seleccionadas como EN_PROCESO.
   */
  marcarEnProceso(): void {
    if (this.totalSeleccionadas() === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe seleccionar al menos una alerta',
        life: 3000
      });
      return;
    }
    
    this.confirmationService.confirm({
      message: `¿Marcar ${this.totalSeleccionadas()} alertas como EN PROCESO?`,
      header: 'Confirmar acción',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, marcar',
      rejectLabel: 'Cancelar',
      accept: () => {
        const ids = this.alertasSeleccionadas().map(a => a.alertaId);
        
        this.alertaService.marcarEnProcesoBatch(ids, 1, 'Marcadas desde dashboard')
          .subscribe({
            next: (response) => {
              this.messageService.add({
                severity: 'success',
                summary: 'Éxito',
                detail: response.mensaje,
                life: 3000
              });
              this.cargarAlertas();
              this.alertasSeleccionadas.set([]);
            },
            error: (error) => {
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'No se pudieron marcar las alertas',
                life: 5000
              });
            }
          });
      }
    });
  }
  
  /**
   * Procesa alertas seleccionadas de forma automática.
   * Ejecuta: Predicción → Optimización → Orden de Compra
   */
  procesarAutomatico(): void {
    if (this.totalSeleccionadas() === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe seleccionar al menos una alerta',
        life: 3000
      });
      return;
    }
    
    this.confirmationService.confirm({
      message: `¿Procesar automáticamente ${this.totalSeleccionadas()} alertas?`,
      header: 'Procesamiento Automático',
      icon: 'pi pi-cog',
      acceptLabel: 'Sí, procesar',
      rejectLabel: 'Cancelar',
      acceptIcon: 'pi pi-check',
      rejectIcon: 'pi pi-times',
      accept: () => {
        this.ejecutarProcesamientoAutomatico();
      }
    });
  }
  
  /**
   * Ejecuta el procesamiento automático batch.
   */
  private ejecutarProcesamientoAutomatico(): void {
    this.procesando.set(true);
    
    const request: ProcesarAlertasRequest = {
      alertaIds: this.alertasSeleccionadas().map(a => a.alertaId),
      horizonteTiempo: 30,
      usuarioId: 1,
      observaciones: 'Procesamiento automático desde dashboard'
    };
    
    this.messageService.add({
      severity: 'info',
      summary: 'Procesando',
      detail: 'Iniciando procesamiento automático...',
      life: 3000
    });
    
    this.alertaService.procesarAlertasAutomatico(request).subscribe({
      next: (response) => {
        this.procesando.set(false);
        this.resultadoProcesamiento.set(response);
        
        // Cargar detalles de órdenes generadas
        if (response.ordenesGeneradas && response.ordenesGeneradas.length > 0) {
          this.cargarResumenOrdenes(response.ordenesGeneradas);
        }
        
        this.dialogResultadosVisible.set(true);
        
        if (response.exitoTotal) {
          this.messageService.add({
            severity: 'success',
            summary: '¡Éxito!',
            detail: `Procesamiento completado: ${response.exitosos} exitosos, ${response.ordenesGeneradas.length} órdenes generadas`,
            life: 5000
          });
        } else {
          this.messageService.add({
            severity: 'warn',
            summary: 'Completado con errores',
            detail: `${response.exitosos} exitosos, ${response.fallidos} fallidos`,
            life: 5000
          });
        }
        
        this.cargarAlertas();
        this.alertasSeleccionadas.set([]);
      },
      error: (error) => {
        console.error('Error en procesamiento:', error);
        this.procesando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Fallo en el procesamiento automático',
          life: 5000
        });
      }
    });
  }
  
  /**
   * Procesa alertas de un proveedor específico.
   * Filtra alertas del proveedor y ejecuta procesamiento automático.
   */
  procesarPorProveedor(proveedorId: number, proveedorNombre: string): void {
    const alertasProveedor = this.alertas().filter(
      a => a.proveedorId === proveedorId
    );
    
    if (alertasProveedor.length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Sin alertas',
        detail: 'No hay alertas pendientes para este proveedor',
        life: 3000
      });
      return;
    }
    
    this.confirmationService.confirm({
      message: `¿Procesar ${alertasProveedor.length} alertas del proveedor "${proveedorNombre}"?`,
      header: 'Procesamiento por Proveedor',
      icon: 'pi pi-shopping-cart',
      acceptLabel: 'Sí, procesar',
      rejectLabel: 'Cancelar',
      acceptIcon: 'pi pi-check',
      rejectIcon: 'pi pi-times',
      accept: () => {
        this.ejecutarProcesamientoPorProveedor(alertasProveedor, proveedorNombre);
      }
    });
  }
  
  /**
   * Ejecuta el procesamiento automático para un proveedor específico.
   */
  private ejecutarProcesamientoPorProveedor(
    alertas: AlertaDashboard[], 
    proveedorNombre: string
  ): void {
    this.procesando.set(true);
    
    const request: ProcesarAlertasRequest = {
      alertaIds: alertas.map(a => a.alertaId),
      horizonteTiempo: 30,
      usuarioId: 1,
      observaciones: `Procesamiento automático - Proveedor: ${proveedorNombre}`
    };
    
    this.messageService.add({
      severity: 'info',
      summary: 'Procesando',
      detail: `Procesando alertas de ${proveedorNombre}...`,
      life: 3000
    });
    
    this.alertaService.procesarAlertasAutomatico(request).subscribe({
      next: (response) => {
        this.procesando.set(false);
        this.resultadoProcesamiento.set(response);
        
        // Cargar detalles de órdenes generadas
        if (response.ordenesGeneradas && response.ordenesGeneradas.length > 0) {
          this.cargarResumenOrdenes(response.ordenesGeneradas);
        }
        
        this.dialogResultadosVisible.set(true);
        
        if (response.exitoTotal) {
          this.messageService.add({
            severity: 'success',
            summary: '¡Éxito!',
            detail: `Proveedor ${proveedorNombre}: ${response.exitosos} alertas procesadas, ${response.ordenesGeneradas.length} órdenes generadas`,
            life: 5000
          });
        } else {
          this.messageService.add({
            severity: 'warn',
            summary: 'Completado con errores',
            detail: `${proveedorNombre}: ${response.exitosos} exitosos, ${response.fallidos} fallidos`,
            life: 5000
          });
        }
        
        this.cargarAlertas();
        this.alertasSeleccionadas.set([]);
      },
      error: (error) => {
        console.error('Error en procesamiento por proveedor:', error);
        this.procesando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: `Error al procesar alertas de ${proveedorNombre}`,
          life: 5000
        });
      }
    });
  }
  
  /**
   * Cierra el diálogo de resultados.
   */
  cerrarDialogResultados(): void {
    this.dialogResultadosVisible.set(false);
    this.resultadoProcesamiento.set(null);
    this.resumenOrdenes.set([]);
  }

  /**
   * Carga el resumen de órdenes generadas.
   */
  private cargarResumenOrdenes(ordenIds: number[]): void {
    if (!ordenIds || ordenIds.length === 0) {
      this.resumenOrdenes.set([]);
      return;
    }

    this.cargandoOrdenes.set(true);
    
    this.alertaService.obtenerResumenOrdenes(ordenIds).subscribe({
      next: (ordenes) => {
        this.resumenOrdenes.set(ordenes);
        this.cargandoOrdenes.set(false);
      },
      error: (error) => {
        console.error('Error al cargar resumen de órdenes:', error);
        this.cargandoOrdenes.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el detalle de las órdenes generadas',
          life: 5000
        });
      }
    });
  }

  /**
   * Navega a la vista de detalle de una orden de compra.
   */
  navegarAOrden(ordenId: number): void {
    this.router.navigate(['/ordenes-compra'], {
      queryParams: { ordenId: ordenId }
    });
    this.cerrarDialogResultados();
  }

  /**
   * Navega a la vista general de órdenes de compra.
   */
  verTodasLasOrdenes(): void {
    this.router.navigate(['/ordenes-compra']);
    this.cerrarDialogResultados();
  }
  
  /**
   * Formatea duración en milisegundos a formato legible.
   */
  formatearDuracion(ms: number): string {
    const segundos = Math.floor(ms / 1000);
    const minutos = Math.floor(segundos / 60);
    const segs = segundos % 60;
    
    if (minutos > 0) {
      return `${minutos}m ${segs}s`;
    }
    return `${segs}s`;
  }

  /**
   * Alterna la expansión de los productos de una orden.
   */
  toggleProductosOrden(ordenId: number): void {
    const expandidos = this.productosOrdenExpandidos();
    expandidos[ordenId] = !expandidos[ordenId];
    this.productosOrdenExpandidos.set({ ...expandidos });
  }
}
