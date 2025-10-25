import { Component, inject, signal, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { ToastModule } from 'primeng/toast';
import { TagModule } from 'primeng/tag';
import { PrediccionesService } from '../../service/predicciones.service';
import { PrediccionResponse } from '../../../ordenes-compra/models/PrediccionResponse';
import { GenerarPrediccionRequest } from '../../models/GenerarPrediccionRequest';
import { ProductoResponse } from '../../../productos/models/ProductoResponse';

interface Column {
  field: keyof PrediccionResponse | 'acciones';
  header: string;
}

@Component({
  selector: 'app-predicciones',
  imports: [
    Dialog, 
    ButtonModule, 
    InputTextModule, 
    ReactiveFormsModule, 
    Select, 
    TableModule, 
    ConfirmDialogModule, 
    PaginatorModule,
    IconField, 
    InputIcon,
    FormsModule,
    ToastModule,
    TagModule
  ],
  templateUrl: './predicciones.html',
  styleUrl: './predicciones.css',
  providers: [ConfirmationService, MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PrediccionesComponent {

  @ViewChild('dt') dataTable: any;

  predicciones = signal<PrediccionResponse[]>([]);
  productos = signal<ProductoResponse[]>([]);
  visible = signal<boolean>(false);
  searchValue = signal<string>('');
  loading = signal<boolean>(false);

  first = signal<number>(0);
  rows = signal<number>(10);
  totalRecords = signal<number>(0);
  rowsPerPageOptions = [10, 20, 30];

  generarForm = new FormGroup({
    producto: new FormControl<ProductoResponse | null>(null, Validators.required),
    diasPronostico: new FormControl<number>(30, [Validators.required, Validators.min(1), Validators.max(365)]),
    tipoAnalisis: new FormControl<string>('ARIMA')
  });

  cols: Column[] = [
    { field: 'prediccionId', header: 'ID' },
    { field: 'producto', header: 'Producto' },
    { field: 'demandaPredichaTotal', header: 'Demanda Predicha' },
    { field: 'fechaPrediccion', header: 'Fecha Predicción' },
    { field: 'fechaProyeccion', header: 'Fecha Proyección' },
    { field: 'precision', header: 'Precisión (%)' },
    { field: 'metodoUtilizado', header: 'Método' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);
  private readonly prediccionesService = inject(PrediccionesService);

  constructor() {
    this.cargarDatos();
  }

  private cargarDatos(): void {
    this.cargarPredicciones();
    this.cargarProductos();
  }

  private cargarPredicciones(): void {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());
    
    this.prediccionesService.obtenerPredicciones(page, this.rows()).subscribe({
      next: (response) => {
        this.predicciones.set(response.content);
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

  private cargarProductos(): void {
    this.prediccionesService.obtenerProductos(0, 50).subscribe({
      next: (response) => {
        this.productos.set(response.content);
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

  showDialog(): void {
    this.generarForm.reset();
    this.generarForm.patchValue({
      tipoAnalisis: 'ARIMA',
      diasPronostico: 30
    });
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.generarForm.reset();
  }

  onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
    this.cargarPredicciones();
  }

  clearSearch(): void {
    this.searchValue.set('');
    if (this.dataTable) {
      this.dataTable.filterGlobal('', 'contains');
    }
  }

  onSearchChange(value: string): void {
    this.searchValue.set(value);
    if (this.dataTable) {
      this.dataTable.filterGlobal(value, 'contains');
    }
  }

  onSubmit(): void {
    if (this.generarForm.valid) {
      const formValue = this.generarForm.value;
      const request: GenerarPrediccionRequest = {
        productoId: formValue.producto!.productoId,
        diasPronostico: formValue.diasPronostico || 30,
        tipoAnalisis: formValue.tipoAnalisis || 'ARIMA'
      };

      this.loading.set(true);
      this.prediccionesService.generarPrediccion(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Predicción generada exitosamente'
          });
          this.cargarPredicciones();
          this.closeDialog();
          this.loading.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Error al generar la predicción'
          });
          this.loading.set(false);
        }
      });
    }
  }

  eliminarPrediccion(prediccion: PrediccionResponse): void {
    this.confirmationService.confirm({
      message: `¿Eliminar la predicción para ${prediccion.producto.nombre}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
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

  formatDisplayDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('es-ES');
  }

  getSeverity(precision: number): 'success' | 'info' | 'warn' | 'danger' {
    if (precision >= 90) return 'success';
    if (precision >= 75) return 'info';
    if (precision >= 60) return 'warn';
    return 'danger';
  }
}