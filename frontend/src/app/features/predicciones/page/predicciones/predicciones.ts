import { Component, inject, signal, ViewChild, ChangeDetectionStrategy, computed, effect } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { ToastModule } from 'primeng/toast';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { Tooltip } from 'primeng/tooltip';
import { Chip } from 'primeng/chip';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { Popover } from 'primeng/popover';
import { TabsModule } from 'primeng/tabs';
import { TimelineModule } from 'primeng/timeline';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { SkeletonModule } from 'primeng/skeleton';
import { PrediccionesService } from '../../service/predicciones.service';
import { AuthService } from '../../../../core/services/auth';
import { AyudaContextualService } from '../../service/ayuda-contextual.service';
import { PrediccionResponse, EstadoPrediccion } from '../../models/PrediccionResponse';
import { OptimizacionResponse, CalcularOptimizacionRequest } from '../../models/OptimizacionResponse';
import { ProductoSimpleResponse } from '../../../productos/models/ProductoResponse';
import { ProductoService } from '../../../productos/service/producto-service';
import { 
  AlgoritmoPipe, 
  AlgoritmoSeverityPipe, 
  EstadoPrediccionPipe, 
  EstadoPrediccionSeverityPipe,
  CalidadPrediccionPipe,
  CalidadPrediccionSeverityPipe,
  AlgoritmoNormalizadoPipe 
} from '../../../../shared/pipes/prediccion.pipe';

interface AlgoritmoCard {
  codigo: string;
  nombre: string;
  descripcion: string;
  icono: string;
  color: string;
  usoCaso: string;
  minimosDatos: number;
  recomendado?: boolean;
}

@Component({
  selector: 'app-predicciones',
  imports: [
    CommonModule,
    Dialog,
    ButtonModule,
    InputTextModule,
    InputNumber,
    ReactiveFormsModule,
    Select,
    TableModule,
    ConfirmDialogModule,
    PaginatorModule,
    FormsModule,
    ToastModule,
    TagModule,
    CardModule,
    ChartModule,
    Tooltip,
    Chip,
    ToggleSwitch,
    Popover,
    TabsModule,
    TimelineModule,
    IconField,
    InputIcon,
    SkeletonModule,
    AlgoritmoPipe,
    AlgoritmoSeverityPipe,
    EstadoPrediccionPipe,
    EstadoPrediccionSeverityPipe,
    CalidadPrediccionPipe,
    CalidadPrediccionSeverityPipe,
    AlgoritmoNormalizadoPipe
  ],
  templateUrl: './predicciones.html',
  styleUrl: './predicciones.css',
  providers: [ConfirmationService, MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PrediccionesComponent {
  @ViewChild('dt') dataTable: any;

  // Permitir acceso a Object en el template
  protected readonly Object = Object;

  // Servicios inyectados
  private prediccionesService = inject(PrediccionesService);
  private productoService = inject(ProductoService);
  private authService = inject(AuthService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  ayudaService = inject(AyudaContextualService); // Público para usar en template

  // Signals para estado del componente
  predicciones = signal<PrediccionResponse[]>([]);
  productos = signal<ProductoSimpleResponse[]>([]);
  algoritmos = signal<AlgoritmoCard[]>([]);
  prediccionSeleccionada = signal<PrediccionResponse | null>(null);

  // Optimización EOQ/ROP
  optimizacionActual = signal<OptimizacionResponse | null>(null);
  loadingOptimizacion = signal<boolean>(false);
  optimizacionCalculada = signal<boolean>(false);

  // Map para guardar optimizaciones por predicción (evita que se comparta entre predicciones)
  private optimizacionesPorPrediccion = new Map<number, OptimizacionResponse>();

  // Modo automático (AUTO vs selección manual de algoritmo)
  modoAutomatico = signal<boolean>(true);
  productoTieneDatosSuficientes = signal<boolean>(true);
  mensajeValidacionProducto = signal<string>('');

  // Dialogs y loading
  dialogGenerarVisible = signal<boolean>(false);
  dialogDetalleVisible = signal<boolean>(false);
  loading = signal<boolean>(false);
  loadingPrediccion = signal<boolean>(false);

  // Paginación
  first = signal<number>(0);
  rows = signal<number>(10);
  totalRecords = signal<number>(0);
  rowsPerPageOptions = [10, 20, 30];

  // Búsqueda
  searchValue = signal<string>('');

  // Paso actual del wizard
  pasoActual = signal<number>(1);

  // Formulario de generación
  generarForm = new FormGroup({
    producto: new FormControl<ProductoSimpleResponse | null>(null, Validators.required),
    algoritmo: new FormControl<string>('AUTO', Validators.required),
    horizonteTiempo: new FormControl<number>(30, [Validators.required, Validators.min(1), Validators.max(365)]),
    detectarEstacionalidad: new FormControl<boolean>(true)
  });

  // Formulario de optimización EOQ/ROP (SIMPLIFICADO)
  // Los costos y tiempos se obtienen automáticamente del producto en la BD
  optimizacionForm = new FormGroup({
    nivelServicioDeseado: new FormControl<number>(0.95, [
      Validators.required,
      Validators.min(0.80),
      Validators.max(0.99)
    ])
  });

  // Computed para Timeline de recomendaciones inteligentes
  recomendacionesInteligentes = computed(() => {
    const pred = this.prediccionSeleccionada();
    if (!pred) return [];

    const recomendaciones: Array<{ icon: string, color: string, titulo: string, descripcion: string }> = [];

    // Recomendación de estado
    if (pred.estado === 'ACTIVA') {
      recomendaciones.push({
        icon: 'pi-check-circle',
        color: '#10b981',
        titulo: '✅ Predicción Vigente',
        descripcion: 'Esta predicción está activa y lista para usar en la planificación de compras e inventario.'
      });
    } else if (pred.estado === 'OBSOLETA') {
      recomendaciones.push({
        icon: 'pi-exclamation-triangle',
        color: '#f59e0b',
        titulo: '⚠️ Predicción Vencida',
        descripcion: 'El horizonte de tiempo ha sido superado. Genera una nueva predicción para datos actualizados.'
      });
    }

    // Recomendación de calidad
    if (pred.calidadPrediccion === 'EXCELENTE' || pred.calidadPrediccion === 'BUENA') {
      recomendaciones.push({
        icon: 'pi-star-fill',
        color: '#3b82f6',
        titulo: `Calidad ${pred.calidadPrediccion}`,
        descripcion: `Alta confiabilidad con MAPE de ${pred.mape?.toFixed(2)}%. Puedes confiar en esta predicción para decisiones estratégicas.`
      });
    } else if (pred.calidadPrediccion === 'ACEPTABLE') {
      recomendaciones.push({
        icon: 'pi-info-circle',
        color: '#f59e0b',
        titulo: '📉 Calidad Aceptable',
        descripcion: `MAPE de ${pred.mape?.toFixed(2)}%. Úsala con precaución y valida con datos adicionales.`
      });
    }

    // Recomendación de patrones
    if (pred.tieneEstacionalidad) {
      recomendaciones.push({
        icon: 'pi-calendar',
        color: '#10b981',
        titulo: '📅 Patrón Estacional Detectado',
        descripcion: 'El producto tiene variaciones cíclicas. Considera ajustar inventario según temporadas.'
      });
    }

    if (pred.tieneTendencia) {
      recomendaciones.push({
        icon: 'pi-chart-line',
        color: '#8b5cf6',
        titulo: '📈 Tendencia Identificada',
        descripcion: 'La demanda muestra crecimiento/decrecimiento consistente. Planifica con margen adicional.'
      });
    }

    // Recomendación de acción
    if (pred.estado === 'ACTIVA' && (pred.calidadPrediccion === 'EXCELENTE' || pred.calidadPrediccion === 'BUENA')) {
      recomendaciones.push({
        icon: 'pi-shopping-cart',
        color: '#06b6d4',
        titulo: '🛒 Acción Recomendada',
        descripcion: `Genera una orden de compra por ${pred.demandaPredichaTotal} unidades para cubrir los próximos ${pred.horizonteTiempo} días.`
      });
    }

    return recomendaciones;
  });

  // Computed para datos del gráfico
  chartData = computed(() => {
    const prediccion = this.prediccionSeleccionada();
    if (!prediccion || !prediccion.datosHistoricos || !prediccion.valoresPredichos) {
      return null;
    }

    const historicos = prediccion.datosHistoricos;
    const predichos = prediccion.valoresPredichos;

    // Generar labels (fechas)
    const labels: string[] = [];
    const hoy = new Date();

    // Labels para históricos
    for (let i = historicos.length - 1; i >= 0; i--) {
      const fecha = new Date(hoy);
      fecha.setDate(fecha.getDate() - i);
      labels.push(fecha.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' }));
    }

    // Labels para predichos
    for (let i = 1; i <= predichos.length; i++) {
      const fecha = new Date(hoy);
      fecha.setDate(fecha.getDate() + i);
      labels.push(fecha.toLocaleDateString('es-ES', { day: '2-digit', month: 'short' }));
    }

    return {
      labels: labels,
      datasets: [
        {
          label: 'Demanda Histórica',
          data: [...historicos, ...Array(predichos.length).fill(null)],
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          tension: 0.4,
          fill: true,
          pointRadius: 4,
          pointHoverRadius: 6
        },
        {
          label: 'Predicción',
          data: [...Array(historicos.length).fill(null), ...predichos],
          borderColor: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          borderDash: [5, 5],
          tension: 0.4,
          fill: true,
          pointRadius: 4,
          pointHoverRadius: 6
        }
      ]
    };
  });

  chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
        labels: {
          usePointStyle: true,
          padding: 20,
          font: { size: 12 }
        }
      },
      tooltip: {
        mode: 'index' as const,
        intersect: false,
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        titleFont: { size: 14 },
        bodyFont: { size: 13 }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Unidades',
          font: { size: 12, weight: 'bold' as const }
        },
        grid: {
          color: 'rgba(0, 0, 0, 0.05)'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Fecha',
          font: { size: 12, weight: 'bold' as const }
        },
        grid: {
          display: false
        }
      }
    },
    interaction: {
      mode: 'nearest' as const,
      axis: 'x' as const,
      intersect: false
    }
  };

  constructor() {
    this.cargarDatos();
    this.inicializarAlgoritmos();

    // Effect para actualizar valores por defecto cuando cambia el algoritmo
    effect(() => {
      const algoritmo = this.generarForm.get('algoritmo')?.value;
      if (algoritmo) {
        this.actualizarParametrosSegunAlgoritmo(algoritmo);
      }
    }, { allowSignalWrites: true });

    // DEBUGGING: Verificar carga de ayuda contextual
    effect(() => {
      const cargado = this.ayudaService.estaCargado();
      if (cargado) {
        console.log('🔍 Ayuda contextual está cargada');
        console.log('🔍 Tooltip producto:', this.ayudaService.obtenerTooltipCampo('producto'));
        console.log('🔍 Todos los datos:', this.ayudaService.obtenerTodosLosDatos());
      } else {
        console.log('⏳ Esperando carga de ayuda contextual...');
      }
    });
  }

  private inicializarAlgoritmos(): void {
    this.algoritmos.set([
      {
        codigo: 'AUTO',
        nombre: 'Selección Automática',
        descripcion: 'El sistema analiza tus datos y selecciona automáticamente el mejor algoritmo según patrones detectados.',
        icono: 'pi pi-sparkles',
        color: '#8b5cf6',
        usoCaso: 'Recomendado para todos los productos - detección inteligente',
        minimosDatos: 10,
        recomendado: true
      },
      {
        codigo: 'LINEAR_REGRESSION',
        nombre: 'Regresión Lineal',
        descripcion: 'Regresión con mínimos cuadrados ordinarios (OLS). Ideal para productos con demanda estable y tendencia clara.',
        icono: 'pi pi-minus',
        color: '#06b6d4',
        usoCaso: 'Productos estables: arroz, azúcar, sal, aceite',
        minimosDatos: 10,
        recomendado: false
      },
      {
        codigo: 'ARIMA',
        nombre: 'ARIMA',
        descripcion: 'Series temporales con autocorrelación. Excelente para tendencias y estacionalidad compleja.',
        icono: 'pi pi-chart-line',
        color: '#3b82f6',
        usoCaso: 'Productos con patrones estacionales y tendencias',
        minimosDatos: 30,
        recomendado: false
      },
      {
        codigo: 'RANDOM_FOREST',
        nombre: 'Random Forest',
        descripcion: 'Ensemble de árboles de decisión. Robusto ante datos ruidosos y relaciones no lineales.',
        icono: 'pi pi-sitemap',
        color: '#10b981',
        usoCaso: 'Bebidas, snacks, productos de limpieza',
        minimosDatos: 30,
        recomendado: false
      },
      {
        codigo: 'GRADIENT_BOOSTING',
        nombre: 'Gradient Boosting',
        descripcion: 'Máxima precisión para demanda errática. Ideal para productos perecederos.',
        icono: 'pi pi-chart-bar',
        color: '#f59e0b',
        usoCaso: 'Pan, lácteos, frutas, verduras',
        minimosDatos: 20,
        recomendado: false
      }
    ]);
  }

  cargarDatos(): void {
    this.cargarPredicciones();
    this.cargarProductos();
  }

  // Todas las predicciones (sin filtrar)
  private todasPredicciones = signal<PrediccionResponse[]>([]);

  private cargarPredicciones(): void {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());

    this.prediccionesService.obtenerPredicciones(page, this.rows()).subscribe({
      next: (response) => {
        this.todasPredicciones.set(response.content);
        this.aplicarFiltros();
        this.totalRecords.set(response.page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las predicciones'
        });
        this.loading.set(false);
      }
    });
  }

  private aplicarFiltros(): void {
    const busqueda = this.searchValue().toLowerCase().trim();
    
    if (!busqueda) {
      this.predicciones.set(this.todasPredicciones());
      return;
    }

    const filtradas = this.todasPredicciones().filter(p => 
      p.productoNombre?.toLowerCase().includes(busqueda) ||
      p.algoritmo?.toLowerCase().includes(busqueda) ||
      p.estado?.toLowerCase().includes(busqueda) ||
      p.calidadPrediccion?.toLowerCase().includes(busqueda)
    );
    
    this.predicciones.set(filtradas);
  }

  private cargarProductos(): void {
    this.productoService.obtenerProductosSimple().subscribe({
      next: (productos) => {
        this.productos.set(productos);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los productos'
        });
      }
    });
  }

  showDialogGenerar(): void {
    this.generarForm.reset({
      algoritmo: 'AUTO',
      horizonteTiempo: 30,
      detectarEstacionalidad: true
    });
    this.pasoActual.set(1);
    this.modoAutomatico.set(true);
    this.productoTieneDatosSuficientes.set(true);
    this.mensajeValidacionProducto.set('');

    // Resetear algoritmos recomendados - AUTO siempre es el recomendado por defecto
    const algoritmos = this.algoritmos();
    const reseteados = algoritmos.map(alg => ({
      ...alg,
      recomendado: alg.codigo === 'AUTO'
    }));
    this.algoritmos.set(reseteados);

    this.dialogGenerarVisible.set(true);
  }

  closeDialogGenerar(): void {
    this.dialogGenerarVisible.set(false);
    this.pasoActual.set(1);
  }

  siguientePaso(): void {
    if (this.pasoActual() < 3) {
      this.pasoActual.update(p => p + 1);
    }
  }

  pasoAnterior(): void {
    if (this.pasoActual() > 1) {
      this.pasoActual.update(p => p - 1);
    }
  }

  seleccionarAlgoritmo(codigo: string): void {
    this.generarForm.patchValue({ algoritmo: codigo });
    this.siguientePaso();
  }

  private actualizarParametrosSegunAlgoritmo(algoritmo: string): void {
    // SMILE ML no requiere parámetros manuales por algoritmo
    // Los algoritmos se auto-configuran internamente
    // Solo mantenemos detectarEstacionalidad como opción global
  }

  onSubmit(): void {
    if (this.generarForm.valid) {
      const formValue = this.generarForm.value;
      const algoritmo = formValue.algoritmo || 'AUTO';

      const request: any = {
        idProducto: formValue.producto!.productoId,
        algoritmoSeleccionado: algoritmo,
        horizonteTiempo: formValue.horizonteTiempo || 30,
        detectarEstacionalidad: formValue.detectarEstacionalidad ?? true,
        generarOrdenCompra: false
      };

      this.loadingPrediccion.set(true);
      this.prediccionesService.generarPrediccionInteligente(request).subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: `Predicción generada con ${response.algoritmoUtilizado} - Calidad: ${response.metricas.calificacionCalidad}`
          });
          this.cargarPredicciones();
          this.closeDialogGenerar();
          this.loadingPrediccion.set(false);

          // Mapear calidad de SMILE ML a formato PrediccionResponse
          // El backend usa: EXCELENTE, BUENA, REGULAR, POBRE
          let calidadMapeada: 'EXCELENTE' | 'BUENA' | 'REGULAR' | 'POBRE' = 'REGULAR';
          switch (response.metricas.calificacionCalidad) {
            case 'EXCELENTE': calidadMapeada = 'EXCELENTE'; break;
            case 'BUENA': calidadMapeada = 'BUENA'; break;
            case 'REGULAR': calidadMapeada = 'REGULAR'; break;
            case 'MALA': 
            case 'POBRE': calidadMapeada = 'POBRE'; break;
          }

          // Mapear respuesta de SMILE ML a PrediccionResponse para visualización
          const prediccionMapeada: PrediccionResponse = {
            prediccionId: response.prediccionId,
            productoId: response.idProducto,
            productoNombre: response.nombreProducto,
            algoritmo: response.algoritmoUtilizado,
            horizonteTiempo: response.horizonteTiempo,
            demandaPredichaTotal: response.valoresPredichos.reduce((a, b) => a + b, 0),
            datosHistoricos: response.valoresHistoricos,
            valoresPredichos: response.valoresPredichos,
            fechaGeneracion: response.fechaEjecucion,
            estado: 'ACTIVA' as EstadoPrediccion,
            calidadPrediccion: calidadMapeada,
            mape: response.metricas.mape,
            mae: response.metricas.mae,
            rmse: response.metricas.rmse,
            tieneTendencia: response.tieneTendencia,
            tieneEstacionalidad: response.tieneEstacionalidad,
            advertencias: response.advertencias,
            recomendaciones: response.recomendaciones
          };

          // Mostrar detalles automáticamente
          this.verDetalle(prediccionMapeada);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Error al generar la predicción'
          });
          this.loadingPrediccion.set(false);
        }
      });
    }
  }

  verDetalle(prediccion: PrediccionResponse): void {
    this.prediccionSeleccionada.set(prediccion);
    this.dialogDetalleVisible.set(true);

    // Cargar optimización guardada para esta predicción específica
    const optimizacionGuardada = this.optimizacionesPorPrediccion.get(prediccion.prediccionId);
    if (optimizacionGuardada) {
      this.optimizacionActual.set(optimizacionGuardada);
      this.optimizacionCalculada.set(true);
    } else {
      this.optimizacionActual.set(null);
      this.optimizacionCalculada.set(false);
    }

    // Resetear formulario con valor por defecto
    this.optimizacionForm.reset({
      nivelServicioDeseado: 0.95
    });
  }

  closeDialogDetalle(): void {
    this.dialogDetalleVisible.set(false);
    this.prediccionSeleccionada.set(null);
    this.optimizacionActual.set(null);
    this.optimizacionCalculada.set(false);
    this.optimizacionForm.reset({
      nivelServicioDeseado: 0.95
    });
  }

  /**
   * Calcula la optimización EOQ/ROP para la predicción seleccionada.
   * Los costos y tiempos se obtienen automáticamente de la BD del producto.
   */
  calcularOptimizacion(): void {
    if (this.optimizacionForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario Inválido',
        detail: 'Por favor complete correctamente el nivel de servicio deseado'
      });
      return;
    }

    const prediccion = this.prediccionSeleccionada();
    if (!prediccion) return;

    // Verificar que el producto tenga los costos configurados
    if (!prediccion.producto) {
      this.messageService.add({
        severity: 'error',
        summary: 'Producto no encontrado',
        detail: 'La predicción no tiene un producto asociado'
      });
      return;
    }

    const request: CalcularOptimizacionRequest = {
      prediccionId: prediccion.prediccionId,
      nivelServicioDeseado: this.optimizacionForm.value.nivelServicioDeseado!
      // Los costos se obtienen automáticamente de prediccion.producto en el backend
    };

    this.loadingOptimizacion.set(true);

    this.prediccionesService.calcularOptimizacion(request).subscribe({
      next: (response) => {
        // Guardar optimización en el Map por predicción
        this.optimizacionesPorPrediccion.set(prediccion.prediccionId, response);

        this.optimizacionActual.set(response);
        this.optimizacionCalculada.set(true);
        this.messageService.add({
          severity: 'success',
          summary: 'Optimización Calculada',
          detail: `EOQ: ${response.cantidadEconomicaPedido.toFixed(0)} unidades, ROP: ${response.puntoReorden.toFixed(0)} unidades`
        });
        this.loadingOptimizacion.set(false);
      },
      error: (error) => {
        console.error('Error al calcular optimización:', error);
        const errorMsg = error.error?.message || 'No se pudo calcular la optimización. Verifique que el producto tenga configurados los costos.';
        this.messageService.add({
          severity: 'error',
          summary: 'Error al Calcular',
          detail: errorMsg
        });
        this.loadingOptimizacion.set(false);
      }
    });
  }

  /**
   * Formatea el porcentaje de nivel de servicio para mostrar en el formulario.
   */
  formatNivelServicio(value: number): string {
    return `${(value * 100).toFixed(0)}%`;
  }

  /**
   * Obtiene el color según el nivel de confianza.
   */
  getColorNivelConfianza(nivel: string): string {
    switch (nivel) {
      case 'ALTO': return '#10b981';
      case 'MEDIO': return '#f59e0b';
      case 'BAJO': return '#ef4444';
      default: return '#6b7280';
    }
  }

  eliminarPrediccion(prediccion: PrediccionResponse): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de eliminar la predicción para ${prediccion.productoNombre}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.prediccionesService.eliminarPrediccion(prediccion.prediccionId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Predicción eliminada exitosamente'
            });
            this.cargarPredicciones();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo eliminar la predicción'
            });
          }
        });
      }
    });
  }

  onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
    this.cargarPredicciones();
  }

  onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchValue.set(target.value);
    this.aplicarFiltros();
  }

  limpiarFiltros(): void {
    this.searchValue.set('');
    this.aplicarFiltros();
  }

  formatDisplayDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSeverityEstado(estado?: string): string {
    switch (estado) {
      case 'ACTIVA': return '#10b981'; // green
      case 'OBSOLETA': return '#f59e0b'; // yellow
      default: return '#6b7280'; // gray
    }
  }

  getSeverityCalidad(calidad?: string): string {
    switch (calidad) {
      case 'EXCELENTE': return '#10b981'; // green
      case 'BUENA': return '#3b82f6'; // blue
      case 'REGULAR': 
      case 'ACEPTABLE': return '#f59e0b'; // yellow - soporta ambos nombres
      case 'POBRE': return '#ef4444'; // red
      default: return '#6b7280'; // gray
    }
  }

  getIconCalidad(calidad?: string): string {
    switch (calidad) {
      case 'EXCELENTE': return 'pi pi-check-circle';
      case 'BUENA': return 'pi pi-thumbs-up';
      case 'REGULAR':
      case 'ACEPTABLE': return 'pi pi-exclamation-circle'; // soporta ambos nombres
      case 'POBRE': return 'pi pi-times-circle';
      default: return 'pi pi-info-circle';
    }
  }

  getColorAlgoritmo(algoritmo: string): string {
    const alg = this.algoritmos().find(a => a.codigo === algoritmo);
    return alg?.color || '#6b7280';
  }

  /**
   * Callback cuando se selecciona un producto en el paso 1.
   * Ya no solicita recomendación - el usuario puede elegir AUTO o manual.
   */
  onProductoSeleccionado(productoId: number | undefined): void {
    if (!productoId) {
      this.productoTieneDatosSuficientes.set(true);
      this.mensajeValidacionProducto.set('');
      return;
    }

    // El producto está seleccionado, permitimos avanzar directamente
    // La validación de datos se hará al momento de generar la predicción
    this.productoTieneDatosSuficientes.set(true);
    this.mensajeValidacionProducto.set('');
  }

  /**
   * Actualiza el algoritmo recomendado visualmente en la lista.
   * Ahora AUTO siempre es el recomendado por defecto.
   */
  private actualizarAlgoritmoRecomendado(algoritmoRecomendado: string): void {
    const algoritmos = this.algoritmos();
    const actualizados = algoritmos.map(alg => ({
      ...alg,
      recomendado: alg.codigo === algoritmoRecomendado
    }));
    this.algoritmos.set(actualizados);
  }

  /**
   * Alterna entre modo automático y manual.
   * En modo automático se usa AUTO, en manual el usuario elige.
   */
  toggleModo(): void {
    const nuevoModo = this.modoAutomatico();

    if (nuevoModo) {
      // Modo automático: establecer algoritmo AUTO
      this.generarForm.patchValue({ algoritmo: 'AUTO' }, { emitEvent: false });
      this.actualizarAlgoritmoRecomendado('AUTO');
    }
  }

  /**
   * Valida si se puede avanzar al siguiente paso
   */
  puedeAvanzar(): boolean {
    const paso = this.pasoActual();

    switch (paso) {
      case 1:
        // Paso 1: Debe tener producto y horizonte de tiempo
        const tieneProducto = !!this.generarForm.get('producto')?.value;
        const tieneHorizonte = !!this.generarForm.get('horizonteTiempo')?.value;
        return tieneProducto && tieneHorizonte;

      case 2:
        // Paso 2: Debe tener algoritmo seleccionado (AUTO por defecto en modo automático)
        return !!this.generarForm.get('algoritmo')?.value;

      case 3:
        // Paso 3: Formulario debe ser válido
        return this.generarForm.valid;

      default:
        return false;
    }
  }

  /**
   * Obtiene el nombre de visualización de un algoritmo
   */
  obtenerNombreAlgoritmo(codigo: string): string {
    const algoritmo = this.algoritmos().find(a => a.codigo === codigo);
    return algoritmo?.nombre || 'Algoritmo Desconocido';
  }

  /**
   * Obtiene el mensaje de tooltip para el botón Siguiente
   */
  obtenerTooltipSiguiente(): string {
    const paso = this.pasoActual();

    if (paso === 1) {
      if (!this.generarForm.get('producto')?.value) {
        return 'Selecciona un producto para continuar';
      }
      if (!this.generarForm.get('horizonteTiempo')?.value) {
        return 'Ingresa el horizonte de tiempo';
      }
      return 'Continuar al siguiente paso';
    }

    if (paso === 2) {
      if (!this.generarForm.get('algoritmo')?.value) {
        return 'Selecciona un algoritmo para continuar';
      }
      return 'Continuar al siguiente paso';
    }

    return 'Continuar';
  }
}