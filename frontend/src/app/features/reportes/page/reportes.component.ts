import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Select } from 'primeng/select';
import { TabsModule } from 'primeng/tabs';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ReporteService } from '../services/reporte.service';
import { ReportePdfService } from '../services/reporte-pdf.service';
import { ReportePrediccionDTO } from '../models/ReportePrediccion';
import { ReporteInventarioDTO } from '../models/ReporteInventario';
import { CategoriaServicio } from '../../categorias/services/categoria-servicio';
import { Categoria } from '../../categorias/models/Categoria';
import { CurrencyService } from '../../../shared/services/currency.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    DatePicker,
    Select,
    TabsModule,
    ProgressSpinnerModule,
    MessageModule,
    Toast
  ],
  providers: [MessageService],
  templateUrl: './reportes.component.html',
  styleUrls: ['./reportes.component.css']
})
export default class ReportesComponent implements OnInit {
  
  private readonly reporteService = inject(ReporteService);
  private readonly reportePdfService = inject(ReportePdfService);
  private readonly categoriaService = inject(CategoriaServicio);
  private readonly messageService = inject(MessageService);
  readonly currencyService = inject(CurrencyService);

  cargando = signal(false);
  datosPredicciones = signal<ReportePrediccionDTO | null>(null);
  datosInventario = signal<ReporteInventarioDTO | null>(null);

  fechaInicio: Date | null = null;
  fechaFin: Date | null = null;
  categoriaSeleccionada: Categoria | null = null;
  categorias = signal<Categoria[]>([]);

  ngOnInit(): void {
    this.cargarCategorias();
    const hoy = new Date();
    const hace30Dias = new Date();
    hace30Dias.setDate(hoy.getDate() - 30);
    
    this.fechaInicio = hace30Dias;
    this.fechaFin = hoy;
  }

  cargarCategorias() {
    this.categoriaService.obtenerCategorias().subscribe({
      next: (categorias: Categoria[]) => {
        const todasOpcion: Categoria = { categoriaId: null as any, nombre: 'Todas las categorías' };
        this.categorias.set([todasOpcion, ...categorias]);
      },
      error: (error: any) => {
        console.error('Error al cargar categorías:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las categorías'
        });
      }
    });
  }

  cargarReportePredicciones() {
    if (!this.fechaInicio || !this.fechaFin) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe seleccionar el rango de fechas'
      });
      return;
    }

    this.cargando.set(true);
    this.datosPredicciones.set(null);

    const fechaInicioStr = this.formatearFecha(this.fechaInicio);
    const fechaFinStr = this.formatearFecha(this.fechaFin);

    this.reporteService.obtenerReportePredicciones(fechaInicioStr, fechaFinStr).subscribe({
      next: (datos: ReportePrediccionDTO) => {
        this.datosPredicciones.set(datos);
        this.cargando.set(false);
        
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `Reporte generado: ${datos.resumenGeneral.totalPredicciones} predicciones encontradas`
        });
      },
      error: (error: any) => {
        console.error('Error al generar reporte de predicciones:', error);
        this.cargando.set(false);
        
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo generar el reporte de predicciones'
        });
      }
    });
  }

  cargarReporteInventario() {
    this.cargando.set(true);
    this.datosInventario.set(null);

    const categoriaId = this.categoriaSeleccionada?.categoriaId !== null 
      ? this.categoriaSeleccionada?.categoriaId 
      : undefined;

    this.reporteService.obtenerReporteInventario(categoriaId).subscribe({
      next: (datos: ReporteInventarioDTO) => {
        this.datosInventario.set(datos);
        this.cargando.set(false);
        
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `Reporte generado: ${datos.resumenGeneral.totalProductos} productos en inventario`
        });
      },
      error: (error: any) => {
        console.error('Error al generar reporte de inventario:', error);
        this.cargando.set(false);
        
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo generar el reporte de inventario'
        });
      }
    });
  }

  descargarPDFPredicciones() {
    const datos = this.datosPredicciones();
    if (!datos) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe generar el reporte primero'
      });
      return;
    }
    
    this.reportePdfService.generarPDFPredicciones(datos as ReportePrediccionDTO);
    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'PDF descargado correctamente'
    });
  }

  previsualizarPDFPredicciones() {
    const datos = this.datosPredicciones();
    if (!datos) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe generar el reporte primero'
      });
      return;
    }
    
    this.reportePdfService.abrirPDFPredicciones(datos as ReportePrediccionDTO);
  }

  descargarPDFInventario() {
    const datos = this.datosInventario();
    if (!datos) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe generar el reporte primero'
      });
      return;
    }
    
    this.reportePdfService.generarPDFInventario(datos as ReporteInventarioDTO);
    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'PDF descargado correctamente'
    });
  }

  previsualizarPDFInventario() {
    const datos = this.datosInventario();
    if (!datos) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe generar el reporte primero'
      });
      return;
    }
    
    this.reportePdfService.abrirPDFInventario(datos as ReporteInventarioDTO);
  }

  private formatearFecha(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
    const dia = fecha.getDate().toString().padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
