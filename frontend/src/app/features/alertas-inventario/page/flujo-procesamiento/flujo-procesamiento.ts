import { CommonModule } from '@angular/common';
import { Component, ChangeDetectionStrategy, computed, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AlertaInventarioService } from '../../services/alerta-inventario.service';
import { AlertaDashboard, AlertasProveedor } from '../../models/AlertaInventario';
import { ProcesarAlertasRequest, ProcesamientoBatchResponse } from '../../models/ProcesamientoAlerta';
import { ResumenPrediccionPorProveedor, PrediccionProductoDTO } from '../../models/PrediccionDetallada';
import { ActivatedRoute } from '@angular/router';

// PrimeNG Modules
import { AccordionModule } from 'primeng/accordion';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { StepsModule } from 'primeng/steps';
import { CardModule } from 'primeng/card';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { TagModule } from 'primeng/tag';
import { ChartModule } from 'primeng/chart';
import { MessageService } from 'primeng/api';

@Component({
    selector: 'app-flujo-procesamiento',
    imports: [
        CommonModule,
        FormsModule,
        AccordionModule,
        TableModule,
        ButtonModule,
        StepsModule,
        CardModule,
        CheckboxModule,
        DialogModule,
        InputNumberModule,
        ToastModule,
        TagModule,
        ChartModule
    ],
        templateUrl: './flujo-procesamiento.html',
    styleUrls: ['./flujo-procesamiento.css'],
        providers: [MessageService],
        changeDetection: ChangeDetectionStrategy.OnPush
})
export class FlujoProcesamientoComponent implements OnInit {
    // Services
        private alertaService = inject(AlertaInventarioService);
        private route = inject(ActivatedRoute);
    private messageService = inject(MessageService);
    private router = inject(Router);

    // Paso actual (0: Selección, 1: Predicciones, 2: Órdenes)
    pasoActual = signal<number>(0);

    // Datos
    alertas = signal<AlertaDashboard[]>([]);
    grupos = signal<AlertasProveedor[]>([]);
    seleccion = signal<Set<number>>(new Set<number>());

    // Configuración
    horizontePrediccion = signal<number>(90);

    // Resultados de predicciones (Paso 2)
    prediccionesPorProveedor = signal<Record<number, ResumenPrediccionPorProveedor>>({});
    prediccionSeleccionada = signal<PrediccionProductoDTO | null>(null);
    mostrarDetallePrediccion = signal<boolean>(false);

    // Configuración de gráfico
    chartData = signal<any>(null);
    chartOptions = signal<any>({
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'top' },
            title: { display: true, text: 'Demanda Histórica vs Predicción' }
        },
        scales: {
            y: { beginAtZero: true }
        }
    });

    // Resultados finales (Paso 3)
    procesamientoResultado = signal<ProcesamientoBatchResponse | null>(null);
    cargando = signal<boolean>(false);

    // Steps model (visual)
    pasos = [
        { label: 'Seleccionar Alertas', command: () => this.pasoActual.set(0) },
        { label: 'Ver Predicciones', command: () => {} }, // No permite retroceder
        { label: 'Generar Órdenes', command: () => {} }
    ];

    // Métricas calculadas
    totalSeleccionadas = computed(() => this.seleccion().size);
    totalProductosSeleccionados = computed(() => this.totalSeleccionadas());
    totalEstimado = computed(() => {
        const ids = this.seleccion();
        return this.alertas().reduce((acc, a: any) => {
            if (ids.has(a.alertaId)) {
                const cant = a.cantidadSugerida || 0;
                const costo = a.costoAdquisicion || 0;
                return acc + (cant * costo);
            }
            return acc;
        }, 0);
    });

        ngOnInit() {
            this.cargarAlertas();
        }

    private cargarAlertas() {
        this.cargando.set(true);
        // Usamos el mismo endpoint del dashboard que ya trae datos de proveedor y costos
        this.alertaService.obtenerAlertasDashboard().subscribe({
            next: (alertas) => {
                // Mapear proveedorId y nombre a nivel raíz si es necesario
                const conProveedor = alertas.map(a => ({
                    ...a,
                    proveedorId: a.producto.proveedor?.proveedorId,
                    proveedorNombreComercial: a.producto.proveedor?.nombreComercial || 'Sin proveedor',
                    costoAdquisicion: a.producto.costoAdquisicion ?? (a as any).costoAdquisicion
                } as any));
                this.alertas.set(conProveedor);
                this.agruparPorProveedor();
                        // Preselección por query param ?alertas=1,2,3
                        const param = this.route.snapshot.queryParamMap.get('alertas');
                        if (param) {
                            const ids = param.split(',').map(v => +v).filter(v => !isNaN(v));
                            const set = new Set(this.seleccion());
                            ids.forEach(id => set.add(id));
                            this.seleccion.set(set);
                            if (ids.length > 0) {
                                this.pasoActual.set(1); // ir directo a configuración si viene con selección
                            }
                        }
                        this.cargando.set(false);
            },
            error: () => {
                this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las alertas.' });
                this.cargando.set(false);
            }
        });
    }

    private agruparPorProveedor() {
        const mapa = new Map<number, AlertaDashboard[]>();
        this.alertas().forEach(a => {
            const pid = (a as any).proveedorId;
            if (!pid) return;
            const arr = mapa.get(pid) || [];
            arr.push(a);
            mapa.set(pid, arr);
        });
        const grupos: AlertasProveedor[] = Array.from(mapa.entries()).map(([proveedorId, alertas]) => ({
            proveedorId,
            proveedorNombre: (alertas[0] as any).proveedorNombreComercial || 'Sin proveedor',
            alertas,
            totalAlertas: alertas.length,
            cantidadTotalSugerida: alertas.reduce((s, a) => s + (a.cantidadSugerida || 0), 0),
            alertasPorCriticidad: {}
        }));
        this.grupos.set(grupos);
    }

    toggleSeleccion(alertaId: number, checked: boolean) {
        const set = new Set(this.seleccion());
        if (checked) set.add(alertaId); else set.delete(alertaId);
        this.seleccion.set(set);
    }

    toggleSeleccionProveedor(proveedorId: number, checked: boolean) {
        const set = new Set(this.seleccion());
        const grupo = this.grupos().find(g => g.proveedorId === proveedorId);
        if (!grupo) return;
        grupo.alertas.forEach(a => {
            if (checked) set.add(a.alertaId); else set.delete(a.alertaId);
        });
        this.seleccion.set(set);
    }

    /** 
     * Paso 1 → Paso 2: Ejecutar predicciones 
     */
    irAPasoPredicciones() {
        if (this.seleccion().size === 0) {
            this.messageService.add({ 
                severity: 'warn', 
                summary: 'Atención', 
                detail: 'Seleccione al menos una alerta.' 
            });
            return;
        }

        this.cargando.set(true);
        const request: ProcesarAlertasRequest = {
            alertaIds: Array.from(this.seleccion()),
            horizonteTiempo: this.horizontePrediccion()
        };

        this.alertaService.procesarAlertasConDetalles(request).subscribe({
            next: (resultado) => {
                this.prediccionesPorProveedor.set(resultado);
                this.pasoActual.set(1);
                this.cargando.set(false);
                this.messageService.add({ 
                    severity: 'success', 
                    summary: 'Éxito', 
                    detail: 'Predicciones generadas correctamente.' 
                });
            },
            error: (err) => {
                this.messageService.add({ 
                    severity: 'error', 
                    summary: 'Error', 
                    detail: 'No se pudieron generar las predicciones.' 
                });
                this.cargando.set(false);
                console.error('Error en predicciones:', err);
            }
        });
    }

    /**
     * Mostrar detalle de predicción con gráfico optimizado
     */
    verDetallePrediccion(prediccion: PrediccionProductoDTO) {
        this.prediccionSeleccionada.set(prediccion);
        
        // Limitar datos históricos a últimos 60 días para mejor visualización
        const maxHistoricos = 60;
        const historicosLimitados = prediccion.valoresHistoricos.slice(-maxHistoricos);
        const fechasHistoricasLimitadas = prediccion.fechasHistoricas.slice(-maxHistoricos);
        
        // Combinar fechas para eje X
        const labels = [
            ...fechasHistoricasLimitadas,
            ...prediccion.fechasPredichas
        ];
        
        // Preparar datasets para el gráfico
        const historicos = [
            ...historicosLimitados,
            ...Array(prediccion.valoresPredichos.length).fill(null)
        ];
        
        const predichos = [
            ...Array(historicosLimitados.length).fill(null),
            ...prediccion.valoresPredichos
        ];
        
        // Agregar punto de conexión entre histórico y predicción
        const puntoConexion = [...Array(historicosLimitados.length - 1).fill(null), 
                                historicosLimitados[historicosLimitados.length - 1], 
                                prediccion.valoresPredichos[0], 
                                ...Array(prediccion.valoresPredichos.length - 1).fill(null)];
        
        this.chartData.set({
            labels: labels,
            datasets: [
                {
                    label: 'Demanda Histórica',
                    data: historicos,
                    borderColor: '#3B82F6',
                    backgroundColor: 'rgba(59, 130, 246, 0.2)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 2,
                    pointHoverRadius: 6,
                    borderWidth: 2
                },
                {
                    label: 'Conexión',
                    data: puntoConexion,
                    borderColor: '#94A3B8',
                    borderDash: [2, 2],
                    borderWidth: 1,
                    pointRadius: 0,
                    fill: false
                },
                {
                    label: 'Predicción SMILE ML',
                    data: predichos,
                    borderColor: '#10B981',
                    backgroundColor: 'rgba(16, 185, 129, 0.2)',
                    fill: true,
                    borderDash: [5, 5],
                    tension: 0.4,
                    pointRadius: 3,
                    pointHoverRadius: 7,
                    borderWidth: 2
                }
            ]
        });
        
        // Opciones mejoradas del gráfico
        this.chartOptions.set({
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: { 
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 15,
                        font: { size: 12 }
                    }
                },
                title: { 
                    display: true, 
                    text: `Análisis de Demanda - ${prediccion.nombreProducto}`,
                    font: { size: 16, weight: 'bold' },
                    padding: { top: 10, bottom: 20 }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleFont: { size: 13, weight: 'bold' },
                    bodyFont: { size: 12 },
                    callbacks: {
                        label: (context: any) => {
                            if (context.dataset.label === 'Conexión') return '';
                            const label = context.dataset.label || '';
                            const value = context.parsed.y;
                            return `${label}: ${value !== null ? Math.round(value * 100) / 100 : 'N/A'}`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    display: true,
                    title: {
                        display: true,
                        text: 'Fecha',
                        font: { size: 13, weight: 'bold' }
                    },
                    ticks: {
                        maxRotation: 45,
                        minRotation: 45,
                        autoSkip: true,
                        maxTicksLimit: 15
                    }
                },
                y: { 
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Cantidad de Demanda',
                        font: { size: 13, weight: 'bold' }
                    },
                    ticks: {
                        callback: (value: any) => Math.round(value)
                    }
                }
            }
        });
        
        this.mostrarDetallePrediccion.set(true);
    }

    /**
     * Obtener severity del badge según calidad
     */
    getSeverityCalidad(calidad: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
        switch(calidad) {
            case 'EXCELENTE': return 'success';
            case 'BUENA': return 'info';
            case 'REGULAR': return 'warn';
            case 'MALA': return 'danger';
            default: return 'secondary';
        }
    }

    /**
     * Paso 2 → Paso 3: Generar órdenes de compra
     */
    generarOrdenes() {
        this.cargando.set(true);
        const request: ProcesarAlertasRequest = {
            alertaIds: Array.from(this.seleccion()),
            horizonteTiempo: this.horizontePrediccion(),
            usuarioId: 1,
            observaciones: 'Órdenes generadas desde flujo guiado'
        };

        this.alertaService.procesarAlertasAutomatico(request).subscribe({
            next: (res) => {
                this.procesamientoResultado.set(res);
                this.messageService.add({ 
                    severity: 'success', 
                    summary: 'Procesado', 
                    detail: 'Órdenes generadas correctamente.' 
                });
                this.pasoActual.set(2);
                this.cargando.set(false);
            },
            error: () => {
                this.messageService.add({ 
                    severity: 'error', 
                    summary: 'Error', 
                    detail: 'No se pudo completar el procesamiento.' 
                });
                this.cargando.set(false);
            }
        });
    }

    iniciarNuevo() {
        this.seleccion.set(new Set());
        this.prediccionesPorProveedor.set({});
        this.procesamientoResultado.set(null);
        this.prediccionSeleccionada.set(null);
        this.pasoActual.set(0);
        this.cargarAlertas();
    }

    /**
     * Navegar a la vista de órdenes de compra
     */
    verOrdenes() {
        this.router.navigate(['/administracion/ordenes-compra']);
    }

    /**
     * Obtener tooltip con información adicional de la predicción
     */
    getTooltipPrediccion(pred: PrediccionProductoDTO): string {
        return `
            Algoritmo: ${pred.algoritmoUsado}
            MAE: ${pred.mae.toFixed(2)}
            RMSE: ${pred.rmse.toFixed(2)}
            Horizonte: ${pred.horizonteUsado} días
            Tendencia: ${pred.tieneTendencia ? 'Sí' : 'No'}
            Estacionalidad: ${pred.tieneEstacionalidad ? 'Sí' : 'No'}
        `.trim();
    }

    /**
     * Obtener clase CSS según criticidad
     */
    getClaseCriticidad(criticidad: string): string {
        switch(criticidad) {
            case 'ALTA': return 'bg-red-100 text-red-700 border-red-200';
            case 'MEDIA': return 'bg-amber-100 text-amber-700 border-amber-200';
            case 'BAJA': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            default: return 'bg-slate-100 text-slate-700 border-slate-200';
        }
    }
}
