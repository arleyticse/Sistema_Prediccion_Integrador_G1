import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { SkeletonModule } from 'primeng/skeleton';
import { TimelineModule } from 'primeng/timeline';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { OrdenesCompraService } from '../../../ordenes-compra/service/ordenes-compra.service';
import { OrdenCompraResponse } from '../../../ordenes-compra/models/OrdenCompraResponse';
import { DialogModule } from 'primeng/dialog';
import { CheckboxModule } from 'primeng/checkbox';
import { AlertaInventarioService } from '../../../alertas-inventario/services/alerta-inventario.service';
import { AlertaInventario } from '../../../alertas-inventario/models/AlertaInventario';
import { MovimientoService } from '../../../movimientos/service/movimiento-service';
import { KardexResponse } from '../../../movimientos/model/KardexResponse';

interface EstadisticaCard {
  titulo: string;
  valor: number;
  icono: string;
  color: string;
  tendencia?: {
    valor: number;
    tipo: 'positiva' | 'negativa';
  };
}

@Component({
  selector: 'app-dashboard-principal',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ChartModule,
    ButtonModule,
    BadgeModule,
    TagModule,
    DividerModule,
    SkeletonModule,
    TimelineModule
    , DialogModule
    , CheckboxModule
  ],
  templateUrl: './dashboard-principal.html',
  styleUrls: ['./dashboard-principal.css']
})
export default class DashboardPrincipalComponent implements OnInit {
  
  // Signals para datos reactivos
  cargando = signal<boolean>(true);
  alertasActivas = signal<AlertaInventario[]>([]);
  cargandoMovimientos = signal<boolean>(false);
  ultimosMovimientos = signal<KardexResponse[]>([]);
  
  // Estadísticas generales
  estadisticas = signal<EstadisticaCard[]>([]);
  
  // Datos para gráficos
  chartAlertasPorTipo = signal<any>(null);
  chartAlertasPorCriticidad = signal<any>(null);
  chartTendenciaInventario = signal<any>(null);
  
  // Opciones de los gráficos
  chartOptions: any;
  
  // Productos que necesitan atención inmediata
  productosAtencion = computed(() => {
    return this.alertasActivas()
      .filter(alerta => alerta.nivelCriticidad === 'CRITICA' || alerta.nivelCriticidad === 'ALTA')
      .slice(0, 5); // Solo los primeros 5
  });

  // Órdenes BORRADOR
  borradores = signal<OrdenCompraResponse[]>([]);
  borradoresModalVisible = signal<boolean>(false);
  borradorSeleccionados = signal<number[]>([]);

  constructor(
    private alertaService: AlertaInventarioService,
    private movimientoService: MovimientoService,
    private router: Router,
    private route: ActivatedRoute,
    public ordenesService: OrdenesCompraService,
    public authService: AuthService
  ) {
    this.configurarOpcionesGraficos();
  }

  esGerente = computed(() => (this.authService.getUsuario()?.rol ?? '') === 'GERENTE');

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('es-ES');
  }

  aprobarOrden(ordenId: number): void {
    this.ordenesService.aprobarOrdenesBorrador([ordenId]).subscribe({
      next: () => {
        this.loadBorradores(false);
        // notify navbar and other components
        document.dispatchEvent(new CustomEvent('borradores-updated'));
      },
      error: () => {}
    });
  }

  ngOnInit(): void {
    this.cargarDatosDashboard();
    this.cargarUltimosMovimientos();
    // Cargar borradores si el usuario es GERENTE
    this.route.queryParams.subscribe(params => {
      const showBorradores = params['showBorradores'] === 'true';
      this.loadBorradores(showBorradores);
    });
  }

  private loadBorradores(showModal: boolean = false): void {
    const user = this.authService.getUsuario();
    if (!user || user.rol !== 'GERENTE') return;
    this.ordenesService.obtenerOrdenesBorrador().subscribe({
      next: (res) => {
        this.borradores.set(res || []);
        if (showModal && (res || []).length > 0) {
          this.borradoresModalVisible.set(true);
        }
      },
      error: () => {}
    });
  }

  aprobarSeleccionados(): void {
    const ids = this.borradorSeleccionados();
    if (ids.length === 0) return;
    this.ordenesService.aprobarOrdenesBorrador(ids).subscribe({
      next: () => {
        // recargar borradores y cerrar modal
        this.loadBorradores(false);
        this.borradoresModalVisible.set(false);
      },
      error: () => {
        // manejar error
      }
    });
  }

  private configurarOpcionesGraficos(): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--p-text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--p-text-muted-color');
    const surfaceBorder = documentStyle.getPropertyValue('--p-content-border-color');

    this.chartOptions = {
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      responsive: true,
      plugins: {
        legend: {
          labels: {
            color: textColor,
            font: {
              size: 12,
              weight: '500'
            },
            padding: 15,
            usePointStyle: true
          },
          position: 'bottom'
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          cornerRadius: 8,
          titleFont: {
            size: 14,
            weight: 'bold'
          },
          bodyFont: {
            size: 13
          },
          displayColors: true
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary,
            font: {
              size: 11
            }
          },
          grid: {
            color: surfaceBorder,
            display: false
          }
        },
        y: {
          ticks: {
            color: textColorSecondary,
            font: {
              size: 11
            }
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false
          },
          beginAtZero: true
        }
      },
      animation: {
        duration: 750,
        easing: 'easeInOutQuart'
      }
    };
  }

  private async cargarDatosDashboard(): Promise<void> {
    try {
      this.cargando.set(true);
      
      // Cargar alertas del dashboard
      const alertas = await this.alertaService.obtenerAlertasDashboard().toPromise();
      this.alertasActivas.set(alertas || []);
      
      // Generar estadísticas
      this.generarEstadisticas(alertas || []);
      
      // Generar datos de gráficos
      this.generarGraficoAlertasPorTipo(alertas || []);
      this.generarGraficoAlertasPorCriticidad(alertas || []);
      this.generarGraficoTendenciaInventario();
      
    } catch (error) {
      console.error('Error al cargar datos del dashboard:', error);
    } finally {
      this.cargando.set(false);
    }
  }

  private generarEstadisticas(alertas: AlertaInventario[]): void {
    const alertasCriticas = alertas.filter(a => a.nivelCriticidad === 'CRITICA').length;
    const alertasAltas = alertas.filter(a => a.nivelCriticidad === 'ALTA').length;
    const productosConAlerta = new Set(alertas.map(a => a.producto.productoId)).size;
    
    this.estadisticas.set([
      {
        titulo: 'Alertas Activas',
        valor: alertas.length,
        icono: 'pi pi-bell',
        color: 'orange', // palette key
        tendencia: { valor: 12, tipo: 'negativa' }
      },
      {
        titulo: 'Alertas Críticas',
        valor: alertasCriticas,
        icono: 'pi pi-exclamation-triangle',
        color: 'red',
        tendencia: { valor: 8, tipo: 'negativa' }
      },
      {
        titulo: 'Productos en Riesgo',
        valor: productosConAlerta,
        icono: 'pi pi-box',
        color: 'purple',
        tendencia: { valor: 5, tipo: 'positiva' }
      },
      {
        titulo: 'Requieren Atención',
        valor: alertasCriticas + alertasAltas,
        icono: 'pi pi-flag',
        color: 'blue'
      }
    ]);
  }

  private generarGraficoAlertasPorTipo(alertas: AlertaInventario[]): void {
    const contadores: Record<string, number> = {};
    
    alertas.forEach(alerta => {
      contadores[alerta.tipoAlerta] = (contadores[alerta.tipoAlerta] || 0) + 1;
    });

    const documentStyle = getComputedStyle(document.documentElement);
    
    this.chartAlertasPorTipo.set({
      labels: Object.keys(contadores),
      datasets: [
        {
          label: 'Alertas',
          data: Object.values(contadores),
          backgroundColor: [
            'rgba(239, 68, 68, 0.8)',
            'rgba(251, 146, 60, 0.8)',
            'rgba(59, 130, 246, 0.8)',
            'rgba(168, 85, 247, 0.8)'
          ],
          borderColor: [
            'rgb(239, 68, 68)',
            'rgb(251, 146, 60)',
            'rgb(59, 130, 246)',
            'rgb(168, 85, 247)'
          ],
          borderWidth: 2,
          borderRadius: 8,
          hoverBackgroundColor: [
            'rgba(239, 68, 68, 1)',
            'rgba(251, 146, 60, 1)',
            'rgba(59, 130, 246, 1)',
            'rgba(168, 85, 247, 1)'
          ]
        }
      ]
    });
  }

  private generarGraficoAlertasPorCriticidad(alertas: AlertaInventario[]): void {
    const critica = alertas.filter(a => a.nivelCriticidad === 'CRITICA').length;
    const alta = alertas.filter(a => a.nivelCriticidad === 'ALTA').length;
    const media = alertas.filter(a => a.nivelCriticidad === 'MEDIA').length;
    const baja = alertas.filter(a => a.nivelCriticidad === 'BAJA').length;

    const documentStyle = getComputedStyle(document.documentElement);
    
    this.chartAlertasPorCriticidad.set({
      labels: ['Crítica', 'Alta', 'Media', 'Baja'],
      datasets: [
        {
          data: [critica, alta, media, baja],
          backgroundColor: [
            'rgba(239, 68, 68, 0.9)',
            'rgba(251, 146, 60, 0.9)',
            'rgba(234, 179, 8, 0.9)',
            'rgba(34, 197, 94, 0.9)'
          ],
          borderColor: [
            'rgb(239, 68, 68)',
            'rgb(251, 146, 60)',
            'rgb(234, 179, 8)',
            'rgb(34, 197, 94)'
          ],
          borderWidth: 3,
          hoverOffset: 15
        }
      ]
    });
  }

  private generarGraficoTendenciaInventario(): void {
    const documentStyle = getComputedStyle(document.documentElement);
    
    // Datos de ejemplo - en producción vendrían del backend
    this.chartTendenciaInventario.set({
      labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'],
      datasets: [
        {
          label: 'Stock Actual',
          data: [65, 59, 80, 81, 56, 55],
          fill: true,
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          borderColor: 'rgb(59, 130, 246)',
          borderWidth: 3,
          tension: 0.4,
          pointBackgroundColor: 'rgb(59, 130, 246)',
          pointBorderColor: '#fff',
          pointBorderWidth: 2,
          pointRadius: 5,
          pointHoverRadius: 7
        },
        {
          label: 'Stock Óptimo',
          data: [75, 75, 75, 75, 75, 75],
          fill: false,
          borderColor: 'rgb(34, 197, 94)',
          borderWidth: 2,
          borderDash: [8, 4],
          tension: 0,
          pointRadius: 0
        }
      ]
    });
  }

  // Métodos de navegación
  navegarAProducto(producto: any): void {
    this.router.navigate(['/administracion/productos'], { 
      queryParams: { id: producto.productoId } 
    });
  }

  navegarAAlertas(): void {
    this.router.navigate(['/administracion/alertas-inventario']);
  }

  navegarAPredicciones(): void {
    this.router.navigate(['/administracion/alertas-inventario/flujo-procesamiento']);
  }

  navegarAOrdenes(): void {
    this.router.navigate(['/administracion/ordenes-compra']);
  }

  // Helpers para obtener colores según criticidad
  claseDecorativaEstadistica(stat: EstadisticaCard): string {
    const base = 'absolute top-0 right-0 w-32 h-32 opacity-10 -mr-10 -mt-10 rounded-full';
    const map: Record<string,string> = {
      orange: 'gradient-attention',
      red: 'bg-gradient-to-br from-red-500 to-red-700',
      purple: 'bg-gradient-to-br from-purple-500 to-purple-700',
      blue: 'bg-gradient-to-br from-blue-500 to-blue-700'
    };
    return `${base} ${map[stat.color] || 'gradient-neutral'}`;
  }

  claseValorEstadistica(stat: EstadisticaCard): string {
    const base = 'text-4xl font-extrabold mb-3 bg-clip-text text-transparent';
    const map: Record<string,string> = {
      orange: 'bg-gradient-to-r from-orange-500 to-orange-700',
      red: 'bg-gradient-to-r from-red-500 to-red-700',
      purple: 'bg-gradient-to-r from-purple-500 to-purple-700',
      blue: 'bg-gradient-to-r from-blue-500 to-blue-700'
    };
    return `${base} ${map[stat.color] || 'bg-gradient-to-r from-gray-500 to-gray-700'}`;
  }

  claseIconoEstadistica(stat: EstadisticaCard): string {
    const base = 'w-20 h-20 flex items-center justify-center rounded-2xl shadow-lg transition-transform duration-300 hover:scale-110';
    const map: Record<string,string> = {
      orange: 'bg-gradient-to-br from-orange-500 to-orange-600',
      red: 'bg-gradient-to-br from-red-500 to-red-600',
      purple: 'bg-gradient-to-br from-purple-500 to-purple-600',
      blue: 'bg-gradient-to-br from-blue-500 to-blue-600'
    };
    return `${base} ${map[stat.color] || 'bg-gradient-to-br from-gray-500 to-gray-600'}`;
  }

  claseTendencia(stat: EstadisticaCard): string {
    if (!stat.tendencia) return '';
    return stat.tendencia.tipo === 'positiva'
      ? 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400'
      : 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400';
  }

  claseIconoCriticidad(nivel: string): string {
    const map: Record<string,string> = {
      CRITICA: 'text-red-600 dark:text-red-400',
      ALTA: 'text-orange-600 dark:text-orange-400'
    };
    return map[nivel] || 'text-surface-600 dark:text-surface-300';
  }

  obtenerColorCriticidad(nivel: string): 'success' | 'info' | 'warn' | 'danger' {
    const colores: Record<string, 'success' | 'info' | 'warn' | 'danger'> = {
      'CRITICA': 'danger',
      'ALTA': 'warn',
      'MEDIA': 'info',
      'BAJA': 'success'
    };
    return colores[nivel] || 'info';
  }

  obtenerIconoCriticidad(nivel: string): string {
    const iconos: Record<string, string> = {
      'CRITICA': 'pi pi-times-circle',
      'ALTA': 'pi pi-exclamation-circle',
      'MEDIA': 'pi pi-info-circle',
      'BAJA': 'pi pi-check-circle'
    };
    return iconos[nivel] || 'pi pi-info-circle';
  }

  obtenerTextoTipoAlerta(tipo: string): string {
    const textos: Record<string, string> = {
      'STOCK_BAJO': 'Stock Bajo',
      'PUNTO_REORDEN': 'Punto de Reorden',
      'PREDICCION_ALTA_DEMANDA': 'Alta Demanda Predicha',
      'INVENTARIO_EXCESIVO': 'Inventario Excesivo'
    };
    return textos[tipo] || tipo;
  }

  // Método para cargar últimos movimientos
  cargarUltimosMovimientos(): void {
    this.cargandoMovimientos.set(true);
    this.movimientoService.getUltimosMovimientos(8).subscribe({
      next: (movimientos) => {
        this.ultimosMovimientos.set(movimientos);
        this.cargandoMovimientos.set(false);
      },
      error: (error) => {
        console.error('Error al cargar movimientos:', error);
        this.ultimosMovimientos.set([]);
        this.cargandoMovimientos.set(false);
      }
    });
  }

  // Helpers para movimientos
  obtenerIconoMovimiento(tipo: string): string {
    const iconos: Record<string, string> = {
      'ENTRADA_COMPRA': 'pi pi-shopping-cart',
      'ENTRADA_DEVOLUCION': 'pi pi-reply',
      'ENTRADA_AJUSTE': 'pi pi-plus-circle',
      'ENTRADA_PRODUCCION': 'pi pi-cog',
      'SALIDA_VENTA': 'pi pi-shopping-bag',
      'SALIDA_DEVOLUCION': 'pi pi-arrow-circle-left',
      'SALIDA_AJUSTE': 'pi pi-minus-circle',
      'SALIDA_MERMA': 'pi pi-exclamation-triangle',
      'SALIDA_CONSUMO': 'pi pi-box'
    };
    return iconos[tipo] || 'pi pi-circle';
  }

  obtenerColorMovimiento(tipo: string): string {
    if (tipo?.startsWith('ENTRADA')) {
      return 'timeline-marker-in';
    } else if (tipo?.startsWith('SALIDA')) {
      return 'timeline-marker-out';
    }
    return 'timeline-marker-neutral';
  }

  obtenerTextoTipoMovimiento(tipo: string): string {
    const textos: Record<string, string> = {
      'ENTRADA_COMPRA': 'Compra',
      'ENTRADA_DEVOLUCION': 'Devolución Cliente',
      'ENTRADA_AJUSTE': 'Ajuste Entrada',
      'ENTRADA_PRODUCCION': 'Producción',
      'SALIDA_VENTA': 'Venta',
      'SALIDA_DEVOLUCION': 'Devolución Proveedor',
      'SALIDA_AJUSTE': 'Ajuste Salida',
      'SALIDA_MERMA': 'Merma',
      'SALIDA_CONSUMO': 'Consumo'
    };
    return textos[tipo] || tipo;
  }

  obtenerSeveridadMovimiento(tipo: string): 'success' | 'info' | 'warn' | 'danger' {
    if (tipo?.startsWith('ENTRADA')) {
      return 'success';
    } else if (tipo === 'SALIDA_MERMA') {
      return 'danger';
    } else if (tipo?.startsWith('SALIDA')) {
      return 'warn';
    }
    return 'info';
  }

  navegarAMovimientos(): void {
    this.router.navigate(['/administracion/movimientos']);
  }

  formatearFecha(fecha: string | Date): string {
    const date = new Date(fecha);
    const ahora = new Date();
    const diffMs = ahora.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHoras = Math.floor(diffMs / 3600000);
    const diffDias = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Hace un momento';
    if (diffMins < 60) return `Hace ${diffMins} min`;
    if (diffHoras < 24) return `Hace ${diffHoras}h`;
    if (diffDias === 1) return 'Ayer';
    if (diffDias < 7) return `Hace ${diffDias} días`;
    
    return date.toLocaleDateString('es-ES', { 
      day: '2-digit', 
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
