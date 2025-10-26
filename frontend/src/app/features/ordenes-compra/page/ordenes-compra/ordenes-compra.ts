import { Component, inject, signal, ViewChild, computed, ChangeDetectionStrategy } from '@angular/core';
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
import { TooltipModule } from 'primeng/tooltip';
import { OrdenesCompraService } from '../../service/ordenes-compra.service';
import { OrdenCompraResponse } from '../../models/OrdenCompraResponse';
import { GenerarOrdenRequest } from '../../models/GenerarOrdenRequest';
import { PrediccionResponse } from '../../models/PrediccionResponse';

interface Column {
  field: keyof OrdenCompraResponse | 'acciones';
  header: string;
}

@Component({
  selector: 'app-ordenes-compra',
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
    TagModule,
    TooltipModule
  ],
  templateUrl: './ordenes-compra.html',
  styleUrl: './ordenes-compra.css',
  providers: [ConfirmationService, MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrdenesCompraComponent {

  @ViewChild('dt') dataTable: any;

  ordenes = signal<OrdenCompraResponse[]>([]);
  predicciones = signal<PrediccionResponse[]>([]);
  visible = signal<boolean>(false);
  searchValue = signal<string>('');
  loading = signal<boolean>(false);

  first = signal<number>(0);
  rows = signal<number>(10);
  totalRecords = signal<number>(0);
  rowsPerPageOptions = [10, 20, 30];

  generarForm = new FormGroup({
    prediccion: new FormControl<PrediccionResponse | null>(null, Validators.required),
    cantidadAdicional: new FormControl<number>(0),
    notasEspeciales: new FormControl<string>(''),
    fechaEntregaDeseada: new FormControl<string>('')
  });

  cols: Column[] = [
    { field: 'numeroOrden', header: 'Número de Orden' },
    { field: 'proveedorNombre', header: 'Proveedor' },
    { field: 'fechaOrden', header: 'Fecha Orden' },
    { field: 'fechaEntregaEsperada', header: 'Entrega Esperada' },
    { field: 'estadoOrden', header: 'Estado' },
    { field: 'totalOrden', header: 'Total' },
    { field: 'generadaAutomaticamente', header: 'Automática' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private readonly confirmationService = inject(ConfirmationService);
  private readonly messageService = inject(MessageService);
  private readonly ordenesService = inject(OrdenesCompraService);

  prediccionesDisponibles = computed(() => 
    this.predicciones().filter(p => p.demandaPredichaTotal > 0)
  );

  constructor() {
    this.cargarDatos();
  }

  private cargarDatos(): void {
    this.cargarOrdenes();
    this.cargarPredicciones();
  }

  private cargarOrdenes(): void {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());
    
    this.ordenesService.obtenerTodasLasOrdenes(page, this.rows()).subscribe({
      next: (response) => {
        this.ordenes.set(response.content);
        this.totalRecords.set(response.page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las órdenes'
        });
        this.loading.set(false);
      }
    });
  }

  private cargarPredicciones(): void {
    this.ordenesService.obtenerPredicciones(0, 50).subscribe({
      next: (response) => {
        this.predicciones.set(response.content);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las predicciones'
        });
      }
    });
  }

  showDialog(): void {
    this.generarForm.reset();
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.generarForm.reset();
  }

  onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
    this.cargarOrdenes();
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
      const request: GenerarOrdenRequest = {
        prediccionId: formValue.prediccion!.prediccionId,
        cantidadAdicional: formValue.cantidadAdicional || 0,
        notasEspeciales: formValue.notasEspeciales || '',
        fechaEntregaDeseada: formValue.fechaEntregaDeseada || ''
      };

      this.loading.set(true);
      this.ordenesService.generarOrden(request).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Orden generada exitosamente'
          });
          this.cargarOrdenes();
          this.closeDialog();
          this.loading.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Error al generar la orden'
          });
          this.loading.set(false);
        }
      });
    }
  }

  confirmarOrden(orden: OrdenCompraResponse): void {
    this.confirmationService.confirm({
      message: `¿Confirmar la orden ${orden.numeroOrden}?`,
      header: 'Confirmar Orden',
      icon: 'pi pi-check-circle',
      accept: () => {
        this.ordenesService.confirmarOrden(orden.ordenCompraId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Orden confirmada exitosamente'
            });
            this.cargarOrdenes();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo confirmar la orden'
            });
          }
        });
      }
    });
  }

  cancelarOrden(orden: OrdenCompraResponse): void {
    this.confirmationService.confirm({
      message: `¿Cancelar la orden ${orden.numeroOrden}?`,
      header: 'Cancelar Orden',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.ordenesService.cancelarOrden(orden.ordenCompraId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Orden cancelada exitosamente'
            });
            this.cargarOrdenes();
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo cancelar la orden'
            });
          }
        });
      }
    });
  }

  getSeverity(estado: string): 'success' | 'info' | 'warn' | 'danger' {
    switch (estado) {
      case 'PENDIENTE': return 'warn';
      case 'APROBADA': return 'success';
      case 'ENVIADA': return 'info';
      case 'RECIBIDA_COMPLETA': return 'success';
      case 'CANCELADA': return 'danger';
      default: return 'info';
    }
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('es-ES');
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }
}