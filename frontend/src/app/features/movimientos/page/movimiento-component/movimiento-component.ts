import { Component, inject, signal, ViewChild } from '@angular/core';
import { KardexResponse } from '../../model/KardexResponse';
import { KardexCreateRequest } from '../../model/KardexRequest';
import { ConfirmationService, MessageService } from 'primeng/api';
import { MovimientoService } from '../../service/movimiento-service';
import { ProductoService } from '../../../productos/service/producto-service';
import { ProveedorService } from '../../../proveedores/service/proveedor-service';
import { Dialog } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductoResponse } from '../../../productos/models/ProductoResponse';
import { Proveedor } from '../../../proveedores/model/Proveedor';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { ChangeDetectionStrategy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ImportacionCsvComponent } from '../../../../shared/components/importacion-csv/importacion-csv';
import { InputNumberModule } from 'primeng/inputnumber';
import { FloatLabel } from 'primeng/floatlabel';
import { Message } from 'primeng/message';
import { Toast } from 'primeng/toast';
import { TextareaModule } from 'primeng/textarea';
import { DatePipe, CurrencyPipe, DecimalPipe } from '@angular/common';
import { DatePicker } from 'primeng/datepicker';
import { SkeletonModule } from 'primeng/skeleton';

interface Column {
  field: keyof KardexResponse | 'acciones';
  header: string;
}

interface TipoMovimientoDTO {
  valor: string;
  descripcion: string;
}

@Component({
  selector: 'app-movimiento-component',
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
    ImportacionCsvComponent,
    InputNumberModule,
    FloatLabel,
    Message,
    Toast,
    TextareaModule,
    DatePipe,
    CurrencyPipe,
    DecimalPipe,
    DatePicker,
    SkeletonModule
  ],
  templateUrl: './movimiento-component.html',
  styleUrl: './movimiento-component.css',
  providers: [ConfirmationService, MessageService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MovimientoComponent {

  @ViewChild('dt') dataTable: any;
  @ViewChild('importacionCsv') importacionCsv!: ImportacionCsvComponent;

  movimientos = signal<KardexResponse[]>([]);
  visible = signal<boolean>(false);
  productos = signal<ProductoResponse[]>([]);
  proveedores = signal<Proveedor[]>([]);
  tiposMovimiento = signal<TipoMovimientoDTO[]>([]);
  searchValue = signal<string>('');

    loading = signal<boolean>(false);

  first = signal<number>(0);
  rows = signal<number>(10);
  totalRecords = signal<number>(0);
  rowsPerPageOptions = [10, 20, 30];
  loadingProductos = signal<boolean>(false);
  loadingProveedores = signal<boolean>(false);

  movimientoForm = new FormGroup({
    productoId: new FormControl<number | null>(null, Validators.required),
    proveedorId: new FormControl<Proveedor | null>(null),
    tipoMovimiento: new FormControl<TipoMovimientoDTO | null>(null, Validators.required),
    tipoDocumento: new FormControl<string>(''),
    numeroDocumento: new FormControl<string>(''),
    cantidad: new FormControl<number>(0, [Validators.required, Validators.min(1)]),
    costoUnitario: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    lote: new FormControl<string>(''),
    fechaVencimiento: new FormControl<Date | null>(null),
    motivo: new FormControl<string>('', Validators.required),
    referencia: new FormControl<string>(''),
    observaciones: new FormControl<string>(''),
    ubicacion: new FormControl<string>(''),
  });

  cols: Column[] = [
    { field: 'nombreProducto', header: 'Producto' },
    { field: 'tipoMovimiento', header: 'Tipo de Movimiento' },
    { field: 'cantidad', header: 'Cantidad' },
    { field: 'saldoCantidad', header: 'Saldo' },
    { field: 'valorTotal', header: 'Valor Total' },
    { field: 'fechaMovimiento', header: 'Fecha' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private readonly confirmationService = inject(ConfirmationService);
  private readonly movimientoService = inject(MovimientoService);
  private readonly productoService = inject(ProductoService);
  private readonly proveedorService = inject(ProveedorService);
  private readonly http = inject(HttpClient);
  private readonly messageService = inject(MessageService);

  constructor() {
    this.cargarDatos();
  }

  // TrackBy function para optimizar el rendimiento de la tabla
  trackByKardexId(index: number, item: KardexResponse): number {
    return item.kardexId ?? index;
  }

  private cargarDatos(): void {
    this.cargarMovimientos();
    this.cargarProductos();
    this.cargarProveedores();
    this.cargarTiposMovimiento();
  }

  cargarMovimientos(): void {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());
    this.movimientoService.getKardex(page, this.rows()).subscribe(response => {
      this.movimientos.set(response.content);
      this.totalRecords.set(response.page.totalElements);
      this.loading.set(false);
    });
  }

  abrirImportacion(): void {
    this.importacionCsv.showDialog();
  }

  private cargarProductos(): void {
    this.loadingProductos.set(true);
    this.productoService.obtenerProductos(0, 100).subscribe(response => {
      this.productos.set(response.content);
      this.loadingProductos.set(false);
    });
  }

  private cargarProveedores(): void {
    this.loadingProveedores.set(true);
    this.proveedorService.getProveedores().subscribe(
      (proveedores: Proveedor[]) => {
        this.proveedores.set(proveedores);
        this.loadingProveedores.set(false);
      },
      (error: any) => {
        console.error('Error cargando proveedores:', error);
        this.loadingProveedores.set(false);
      }
    );
  }

  private cargarTiposMovimiento(): void {
    this.http.get<TipoMovimientoDTO[]>('http://localhost:8080/api/catalogos/tipos-movimiento').subscribe(
      (tipos) => {
        this.tiposMovimiento.set(tipos);
      },
      (error) => {
        console.error('Error cargando tipos de movimiento:', error);
      }
    );
  }

  showDialog(): void {
    this.movimientoForm.reset({
      productoId: null,
      proveedorId: null,
      tipoMovimiento: null,
      tipoDocumento: '',
      numeroDocumento: '',
      cantidad: 0,
      costoUnitario: 0,
      lote: '',
      fechaVencimiento: null,
      motivo: '',
      referencia: '',
      observaciones: '',
      ubicacion: ''
    });
    this.visible.set(true);
  }

  onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
    this.cargarMovimientos();
  }

  closeDialog(): void {
    this.visible.set(false);
    this.movimientoForm.reset({
      productoId: null,
      proveedorId: null,
      tipoMovimiento: null,
      tipoDocumento: '',
      numeroDocumento: '',
      cantidad: 0,
      costoUnitario: 0,
      lote: '',
      fechaVencimiento: null,
      motivo: '',
      referencia: '',
      observaciones: '',
      ubicacion: ''
    });
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

  private convertToLocalDateTime(date: Date | null | undefined): string | undefined {
    if (!date) return undefined;
    // Convierte Date object a formato ISO LocalDateTime "2025-10-20T00:00:00"
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}T00:00:00`;
  }

  onSubmit(): void {
    if (this.movimientoForm.valid) {
      const formValue = this.movimientoForm.value;
      const nuevoMovimiento: KardexCreateRequest = {
        productoId: formValue.productoId!,
        tipoMovimiento: formValue.tipoMovimiento!.valor,
        tipoDocumento: formValue.tipoDocumento || undefined,
        numeroDocumento: formValue.numeroDocumento || undefined,
        cantidad: formValue.cantidad!,
        costoUnitario: formValue.costoUnitario!,
        lote: formValue.lote || undefined,
        fechaVencimiento: this.convertToLocalDateTime(formValue.fechaVencimiento),
        proveedorId: formValue.proveedorId?.proveedorId || undefined,
        motivo: formValue.motivo!,
        referencia: formValue.referencia || undefined,
        observaciones: formValue.observaciones || undefined,
        ubicacion: formValue.ubicacion || undefined,
      };

      this.movimientoService.createKardex(nuevoMovimiento).subscribe(
        () => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Movimiento registrado correctamente' 
          });
          this.first.set(0);
          this.cargarMovimientos();
          this.closeDialog();
        },
        (error) => {
          console.error('Error creando movimiento:', error);
          this.messageService.add({ 
            severity: 'error', 
            summary: 'Error', 
            detail: 'No se pudo registrar el movimiento' 
          });
        }
      );
    }
  }

  eliminar(movimiento: KardexResponse): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea anular el movimiento de "${movimiento.nombreProducto}" (ID: ${movimiento.kardexId})?`,
      header: 'Confirmar anulación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.movimientoService.deleteKardex(movimiento.kardexId!).subscribe(
          () => {
            this.messageService.add({ 
              severity: 'success', 
              summary: 'Éxito', 
              detail: 'Movimiento anulado correctamente' 
            });
            this.cargarMovimientos();
          },
          (error) => {
            console.error('Error anulando movimiento:', error);
            this.messageService.add({ 
              severity: 'error', 
              summary: 'Error', 
              detail: 'No se pudo anular el movimiento' 
            });
          }
        );
      }
    });
  }
}
