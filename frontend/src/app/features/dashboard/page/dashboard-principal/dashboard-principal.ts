import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { TagModule } from 'primeng/tag';
import { DividerModule } from 'primeng/divider';
import { SkeletonModule } from 'primeng/skeleton';
import { Router } from '@angular/router';
import { AlertaInventarioService } from '../../../alertas-inventario/services/alerta-inventario.service';
import { AlertaInventario } from '../../../alertas-inventario/models/AlertaInventario';

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
    SkeletonModule
  ],
  templateUrl: './dashboard-principal.html',
  styleUrls: ['./dashboard-principal.css']
})
export default class DashboardPrincipalComponent implements OnInit {
  
  // Signals para datos reactivos
  cargando = signal<boolean>(true);
  alertasActivas = signal<AlertaInventario[]>([]);
  
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

  constructor(
    private alertaService: AlertaInventarioService,
    private router: Router
  ) {
    this.configurarOpcionesGraficos();
  }

  ngOnInit(): void {
    this.cargarDatosDashboard();
  }

  private configurarOpcionesGraficos(): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--p-text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--p-text-muted-color');
    const surfaceBorder = documentStyle.getPropertyValue('--p-content-border-color');

    this.chartOptions = {
      maintainAspectRatio: false,
      aspectRatio: 0.8,
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        }
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
        color: 'orange',
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
            documentStyle.getPropertyValue('--p-red-500'),
            documentStyle.getPropertyValue('--p-orange-500'),
            documentStyle.getPropertyValue('--p-blue-500'),
            documentStyle.getPropertyValue('--p-purple-500')
          ],
          borderColor: 'transparent',
          borderRadius: 8
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
            documentStyle.getPropertyValue('--p-red-500'),
            documentStyle.getPropertyValue('--p-orange-500'),
            documentStyle.getPropertyValue('--p-yellow-500'),
            documentStyle.getPropertyValue('--p-green-500')
          ]
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
          fill: false,
          borderColor: documentStyle.getPropertyValue('--p-blue-500'),
          tension: 0.4
        },
        {
          label: 'Stock Óptimo',
          data: [75, 75, 75, 75, 75, 75],
          fill: false,
          borderColor: documentStyle.getPropertyValue('--p-green-500'),
          borderDash: [5, 5],
          tension: 0
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
}
