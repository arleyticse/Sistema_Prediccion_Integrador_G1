import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { SkeletonModule } from 'primeng/skeleton';
import { TimelineModule } from 'primeng/timeline';
import { TooltipModule } from 'primeng/tooltip';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { OrdenesCompraService } from '../../../ordenes-compra/service/ordenes-compra.service';
import { OrdenCompraResponse } from '../../../ordenes-compra/models/OrdenCompraResponse';
import { DialogModule } from 'primeng/dialog';
import { CheckboxModule } from 'primeng/checkbox';
import { AlertaInventarioService } from '../../../alertas-inventario/services/alerta-inventario.service';
import { AlertaInventario } from '../../../alertas-inventario/models/AlertaInventario';
import { DashboardService } from '../../service/dashboard.service';
import { DashboardCompleto, DashboardEstadisticas, ProductoStockBajo } from '../../models/dashboard.models';

interface EstadisticaCard {
  titulo: string;
  valor: number | string;
  icono: string;
  color: string;
  descripcion?: string;
  formato?: 'numero' | 'moneda' | 'texto';
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
    TimelineModule,
    TooltipModule,
    DialogModule,
    CheckboxModule,
    DecimalPipe
  ],
  templateUrl: './dashboard-principal.html',
  styleUrls: ['./dashboard-principal.css']
})
export default class DashboardPrincipalComponent implements OnInit {
  
  private readonly dashboardService = inject(DashboardService);
  private readonly alertaService = inject(AlertaInventarioService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly ordenesService = inject(OrdenesCompraService);
  readonly authService = inject(AuthService);

  cargando = signal<boolean>(true);
  cargandoGraficos = signal<boolean>(true);
  
  dashboardData = signal<DashboardCompleto | null>(null);
  alertasActivas = signal<AlertaInventario[]>([]);
  
  chartDistribucionInventario = signal<any>(null);
  chartTopVendidos = signal<any>(null);
  chartTendenciaMovimientos = signal<any>(null);
  chartDistribucionCategorias = signal<any>(null);
  chartAlertasPorTipo = signal<any>(null);
  
  chartOptionsBar: any;
  chartOptionsDoughnut: any;
  chartOptionsLine: any;
  chartOptionsHorizontalBar: any;
  
  productosStockBajo = signal<ProductoStockBajo[]>([]);

  // Contadores basados en alertas activas del servicio de alertas (filtradas correctamente)
  totalAlertasActivas = computed(() => this.alertasActivas().length);
  alertasCriticasCount = computed(() => 
    this.alertasActivas().filter(a => a.nivelCriticidad === 'CRITICA').length
  );

  // Estadísticas principales usando computed para reactividad con alertas
  estadisticasPrincipales = computed<EstadisticaCard[]>(() => {
    const data = this.dashboardData();
    if (!data) return [];
    
    const stats = data.estadisticas;
    const alertasActivas = this.totalAlertasActivas();
    const alertasCriticas = this.alertasCriticasCount();

    return [
      {
        titulo: 'Total Productos',
        valor: stats.totalProductos,
        icono: 'pi pi-box',
        color: 'blue',
        descripcion: 'Productos registrados',
        formato: 'numero'
      },
      {
        titulo: 'Valor Inventario',
        valor: stats.valorInventarioTotal,
        icono: 'pi pi-dollar',
        color: 'green',
        descripcion: 'Valor total en stock',
        formato: 'moneda'
      },
      {
        titulo: 'Alertas Activas',
        valor: alertasActivas,
        icono: 'pi pi-bell',
        color: 'orange',
        descripcion: `${alertasCriticas} críticas`,
        formato: 'numero'
      },
      {
        titulo: 'Stock Bajo',
        valor: stats.productosStockBajo + stats.productosStockCritico,
        icono: 'pi pi-exclamation-triangle',
        color: 'red',
        descripcion: `${stats.productosStockCritico} críticos`,
        formato: 'numero'
      },
      {
        titulo: 'Proveedores',
        valor: stats.proveedoresActivos,
        icono: 'pi pi-truck',
        color: 'purple',
        descripcion: 'Proveedores activos',
        formato: 'numero'
      },
      {
        titulo: 'Stock Total',
        valor: stats.stockTotalUnidades,
        icono: 'pi pi-database',
        color: 'cyan',
        descripcion: 'Unidades en inventario',
        formato: 'numero'
      }
    ];
  });

  productosAtencion = computed(() => {
    return this.alertasActivas()
      .filter(alerta => alerta.nivelCriticidad === 'CRITICA' || alerta.nivelCriticidad === 'ALTA')
      .slice(0, 5);
  });

  borradores = signal<OrdenCompraResponse[]>([]);
  borradoresModalVisible = signal<boolean>(false);
  borradorSeleccionados = signal<number[]>([]);

  esGerente = computed(() => (this.authService.getUsuario()?.rol ?? '') === 'GERENTE');

  constructor() {
    this.configurarOpcionesGraficos();
  }

  ngOnInit(): void {
    this.cargarDashboard();
    this.cargarAlertasActivas();
    
    this.route.queryParams.subscribe(params => {
      const showBorradores = params['showBorradores'] === 'true';
      this.loadBorradores(showBorradores);
    });
  }

  private async cargarDashboard(): Promise<void> {
    this.cargando.set(true);
    this.cargandoGraficos.set(true);
    
    this.dashboardService.obtenerDashboardCompleto().subscribe({
      next: (data) => {
        this.dashboardData.set(data);
        this.productosStockBajo.set(data.productosStockBajo);
        this.cargando.set(false);
        
        this.generarGraficos(data);
        this.cargandoGraficos.set(false);
      },
      error: (error) => {
        console.error('Error al cargar dashboard:', error);
        this.cargando.set(false);
        this.cargandoGraficos.set(false);
      }
    });
  }

  private async cargarAlertasActivas(): Promise<void> {
    this.alertaService.obtenerAlertasDashboard().subscribe({
      next: (alertas) => this.alertasActivas.set(alertas || []),
      error: (error) => console.error('Error al cargar alertas:', error)
    });
  }

  private generarGraficos(data: DashboardCompleto): void {
    this.generarGraficoDistribucionInventario(data.distribucionInventario);
    this.generarGraficoTopVendidos(data.productosMasVendidos);
    this.generarGraficoTendenciaMovimientos(data.tendenciaMovimientos);
    this.generarGraficoDistribucionCategorias(data.distribucionCategorias);
    this.generarGraficoAlertasPorTipo(data.distribucionAlertas);
  }

  private generarGraficoDistribucionInventario(distribucion: any[]): void {
    const colores: Record<string, string> = {
      'NORMAL': 'rgba(34, 197, 94, 0.8)',
      'BAJO': 'rgba(251, 146, 60, 0.8)',
      'CRITICO': 'rgba(239, 68, 68, 0.8)',
      'EXCESO': 'rgba(59, 130, 246, 0.8)',
      'OBSOLETO': 'rgba(156, 163, 175, 0.8)',
      'BLOQUEADO': 'rgba(107, 114, 128, 0.8)'
    };

    const labels = distribucion.map(d => this.formatearEstadoInventario(d.estado));
    const datos = distribucion.map(d => d.cantidad);
    const backgroundColors = distribucion.map(d => colores[d.estado] || 'rgba(156, 163, 175, 0.8)');

    this.chartDistribucionInventario.set({
      labels,
      datasets: [{
        data: datos,
        backgroundColor: backgroundColors,
        borderWidth: 2,
        borderColor: '#fff',
        hoverOffset: 10
      }]
    });
  }

  private generarGraficoTopVendidos(productos: any[]): void {
    this.chartTopVendidos.set({
      labels: productos.map(p => this.truncarNombre(p.nombre, 20)),
      datasets: [{
        label: 'Unidades Vendidas',
        data: productos.map(p => p.cantidadVendida),
        backgroundColor: 'rgba(59, 130, 246, 0.7)',
        borderColor: 'rgb(59, 130, 246)',
        borderWidth: 2,
        borderRadius: 6,
        hoverBackgroundColor: 'rgba(59, 130, 246, 0.9)'
      }]
    });
  }

  private generarGraficoTendenciaMovimientos(tendencia: any[]): void {
    const labels = tendencia.map(t => this.formatearFechaCorta(t.fecha));
    
    this.chartTendenciaMovimientos.set({
      labels,
      datasets: [
        {
          label: 'Entradas',
          data: tendencia.map(t => t.entradas),
          fill: true,
          backgroundColor: 'rgba(34, 197, 94, 0.1)',
          borderColor: 'rgb(34, 197, 94)',
          borderWidth: 2,
          tension: 0.4,
          pointBackgroundColor: 'rgb(34, 197, 94)',
          pointBorderColor: '#fff',
          pointRadius: 3,
          pointHoverRadius: 6
        },
        {
          label: 'Salidas',
          data: tendencia.map(t => t.salidas),
          fill: true,
          backgroundColor: 'rgba(239, 68, 68, 0.1)',
          borderColor: 'rgb(239, 68, 68)',
          borderWidth: 2,
          tension: 0.4,
          pointBackgroundColor: 'rgb(239, 68, 68)',
          pointBorderColor: '#fff',
          pointRadius: 3,
          pointHoverRadius: 6
        }
      ]
    });
  }

  private generarGraficoDistribucionCategorias(categorias: any[]): void {
    this.chartDistribucionCategorias.set({
      labels: categorias.map(c => this.truncarNombre(c.nombre, 15)),
      datasets: [{
        label: 'Productos',
        data: categorias.map(c => c.cantidadProductos),
        backgroundColor: [
          'rgba(59, 130, 246, 0.7)',
          'rgba(34, 197, 94, 0.7)',
          'rgba(168, 85, 247, 0.7)',
          'rgba(251, 146, 60, 0.7)',
          'rgba(236, 72, 153, 0.7)',
          'rgba(20, 184, 166, 0.7)',
          'rgba(245, 158, 11, 0.7)',
          'rgba(239, 68, 68, 0.7)',
          'rgba(99, 102, 241, 0.7)',
          'rgba(16, 185, 129, 0.7)'
        ],
        borderWidth: 0,
        borderRadius: 4
      }]
    });
  }

  private generarGraficoAlertasPorTipo(alertas: any[]): void {
    const colores: Record<string, string> = {
      'STOCK_BAJO': 'rgba(251, 146, 60, 0.8)',
      'PUNTO_REORDEN': 'rgba(59, 130, 246, 0.8)',
      'PREDICCION_VENCIDA': 'rgba(168, 85, 247, 0.8)',
      'PREDICCION_ALTA_DEMANDA': 'rgba(236, 72, 153, 0.8)',
      'INVENTARIO_EXCESIVO': 'rgba(20, 184, 166, 0.8)'
    };

    this.chartAlertasPorTipo.set({
      labels: alertas.map(a => this.formatearTipoAlerta(a.tipo)),
      datasets: [{
        data: alertas.map(a => a.cantidad),
        backgroundColor: alertas.map(a => colores[a.tipo] || 'rgba(156, 163, 175, 0.8)'),
        borderWidth: 2,
        borderColor: '#fff',
        hoverOffset: 8
      }]
    });
  }

  private configurarOpcionesGraficos(): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--p-text-color') || '#374151';
    const textColorSecondary = documentStyle.getPropertyValue('--p-text-muted-color') || '#6B7280';
    const surfaceBorder = documentStyle.getPropertyValue('--p-content-border-color') || '#E5E7EB';

    this.chartOptionsBar = {
      maintainAspectRatio: false,
      responsive: true,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          cornerRadius: 8
        }
      },
      scales: {
        x: {
          ticks: { color: textColorSecondary, font: { size: 10 } },
          grid: { display: false }
        },
        y: {
          ticks: { color: textColorSecondary },
          grid: { color: surfaceBorder, drawBorder: false },
          beginAtZero: true
        }
      }
    };

    this.chartOptionsHorizontalBar = {
      indexAxis: 'y',
      maintainAspectRatio: false,
      responsive: true,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          cornerRadius: 8
        }
      },
      scales: {
        x: {
          ticks: { color: textColorSecondary },
          grid: { color: surfaceBorder, drawBorder: false },
          beginAtZero: true
        },
        y: {
          ticks: { color: textColorSecondary, font: { size: 11 } },
          grid: { display: false }
        }
      }
    };

    this.chartOptionsDoughnut = {
      maintainAspectRatio: false,
      responsive: true,
      cutout: '60%',
      plugins: {
        legend: {
          position: 'right',
          labels: {
            color: textColor,
            usePointStyle: true,
            padding: 15,
            font: { size: 11 }
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          cornerRadius: 8
        }
      }
    };

    this.chartOptionsLine = {
      maintainAspectRatio: false,
      responsive: true,
      plugins: {
        legend: {
          position: 'top',
          labels: {
            color: textColor,
            usePointStyle: true,
            padding: 20,
            font: { size: 12 }
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          cornerRadius: 8,
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        x: {
          ticks: { color: textColorSecondary, font: { size: 10 }, maxRotation: 45 },
          grid: { display: false }
        },
        y: {
          ticks: { color: textColorSecondary },
          grid: { color: surfaceBorder, drawBorder: false },
          beginAtZero: true
        }
      },
      interaction: {
        mode: 'nearest',
        axis: 'x',
        intersect: false
      }
    };
  }

  private formatearEstadoInventario(estado: string): string {
    const estados: Record<string, string> = {
      'NORMAL': 'Normal',
      'BAJO': 'Bajo',
      'CRITICO': 'Crítico',
      'EXCESO': 'Exceso',
      'OBSOLETO': 'Obsoleto',
      'BLOQUEADO': 'Bloqueado'
    };
    return estados[estado] || estado;
  }

  private formatearTipoAlerta(tipo: string): string {
    const tipos: Record<string, string> = {
      'STOCK_BAJO': 'Stock Bajo',
      'PUNTO_REORDEN': 'Punto Reorden',
      'PREDICCION_VENCIDA': 'Pred. Vencida',
      'PREDICCION_ALTA_DEMANDA': 'Alta Demanda',
      'INVENTARIO_EXCESIVO': 'Inv. Excesivo'
    };
    return tipos[tipo] || tipo;
  }

  private truncarNombre(nombre: string, maxLength: number): string {
    return nombre.length > maxLength ? nombre.substring(0, maxLength) + '...' : nombre;
  }

  private formatearFechaCorta(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' });
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('es-ES');
  }

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

  navegarAInventario(): void {
    this.router.navigate(['/administracion/inventario']);
  }

  navegarAProductos(): void {
    this.router.navigate(['/administracion/productos']);
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
      'PREDICCION_ALTA_DEMANDA': 'Alta Demanda',
      'INVENTARIO_EXCESIVO': 'Inventario Excesivo',
      'PREDICCION_VENCIDA': 'Predicción Vencida'
    };
    return textos[tipo] || tipo;
  }

  obtenerColorEstadoStock(estado: string): 'success' | 'info' | 'warn' | 'danger' {
    const colores: Record<string, 'success' | 'info' | 'warn' | 'danger'> = {
      'NORMAL': 'success',
      'BAJO': 'warn',
      'CRITICO': 'danger',
      'EXCESO': 'info'
    };
    return colores[estado] || 'info';
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

  aprobarOrden(ordenId: number): void {
    this.ordenesService.aprobarOrdenesBorrador([ordenId]).subscribe({
      next: () => {
        this.loadBorradores(false);
        document.dispatchEvent(new CustomEvent('borradores-updated'));
      },
      error: () => {}
    });
  }

  aprobarSeleccionados(): void {
    const ids = this.borradorSeleccionados();
    if (ids.length === 0) return;
    this.ordenesService.aprobarOrdenesBorrador(ids).subscribe({
      next: () => {
        this.loadBorradores(false);
        this.borradoresModalVisible.set(false);
      },
      error: () => {}
    });
  }
}
