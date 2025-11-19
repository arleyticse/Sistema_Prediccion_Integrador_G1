import { Component, ChangeDetectionStrategy, inject, signal, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { PaginatorState } from 'primeng/paginator';
import { InventarioService } from '../../service/inventario-service';
import { ProductoService } from '../../../productos/service/producto-service';
import { ProductoResponse } from '../../../productos/models/ProductoResponse';
import { InventarioResponse, PageInventarioResponse } from '../../model/InventarioResponse';
import { InventarioCreateRequest } from '../../model/InventarioRequest';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PaginatorModule } from 'primeng/paginator';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { ImportacionCsvComponent } from '../../../../shared/components/importacion-csv/importacion-csv';
import { TextareaModule } from 'primeng/textarea';
import { Toast } from 'primeng/toast';
import { Message } from 'primeng/message';
import { SkeletonModule } from 'primeng/skeleton';

interface Column {
  field: keyof InventarioResponse | 'acciones' | 'producto';
  header: string;
}

@Component({
  selector: 'app-inventario-component',
  imports: [
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    ReactiveFormsModule,
    TableModule,
    ConfirmDialogModule,
    PaginatorModule,
    SelectModule,
    DialogModule,
    FormsModule,
    ImportacionCsvComponent,
    TextareaModule,
    Toast,
    Message,
    SkeletonModule
  ],
  templateUrl: './inventario-component.html',
  styleUrl: './inventario-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ConfirmationService, MessageService]
})
export class InventarioComponent {
  @ViewChild('importacionCsv') importacionCsv!: ImportacionCsvComponent;
  
  inventarios = signal<InventarioResponse[]>([]);
  productos = signal<ProductoResponse[]>([]);
  visible = signal<boolean>(false);
  isEditing = signal<boolean>(false);
  inventarioIdSeleccionado = signal<number | null>(null);
  loadingProductos = signal<boolean>(false);

    loading = signal<boolean>(false);

  first = signal<number>(0);
  rows = signal<number>(10);
  totalRecords = signal<number>(0);
  readonly rowsPerPageOptions = [10, 20, 30];

  inventarioForm = new FormGroup({
    producto: new FormControl<ProductoResponse | null>(null, Validators.required),
    stockDisponible: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    stockReservado: new FormControl<number>(0, [Validators.min(0), Validators.required]),
    stockEnTransito: new FormControl<number>(0, [Validators.min(0), Validators.required]),
    stockMinimo: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    stockMaximo: new FormControl<number>(0, [Validators.min(0), Validators.required]),
    puntoReorden: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    ubicacionAlmacen: new FormControl<string>(''),
    observaciones: new FormControl<string>('')
  });

  cols: Column[] = [
    { field: 'producto', header: 'Producto' },
    { field: 'stockDisponible', header: 'Stock Disponible' },
    { field: 'stockMinimo', header: 'Stock Mínimo' },
    { field: 'puntoReorden', header: 'Punto Reorden' },
    { field: 'ubicacionAlmacen', header: 'Ubicación' },
    { field: 'estado', header: 'Estado' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private readonly confirmationService = inject(ConfirmationService);
  private readonly inventarioService = inject(InventarioService);
  private readonly productoService = inject(ProductoService);
  private readonly messageService = inject(MessageService);

  constructor() {
    this.cargarDatos();
  }

  private cargarDatos(): void {
    this.cargarInventarios();
    this.cargarProductos();
  }

  cargarInventarios(): void {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());
    this.inventarioService.obtenerInventarios(page, this.rows()).subscribe(response => {
      this.inventarios.set(response.content);
      this.totalRecords.set(response.page.totalElements);
      this.loading.set(false);
    });
  }

  abrirImportacion(): void {
    this.importacionCsv.showDialog();
  }

  private cargarProductos(): void {
    this.loadingProductos.set(true);
    this.productoService.obtenerTodosProductos().subscribe({
      next: (productos) => {
        this.productos.set(productos);
        this.loadingProductos.set(false);
      },
      error: (error) => {
        console.error('Error cargando productos:', error);
        this.loadingProductos.set(false);
      }
    });
  }

  showDialog(): void {
    this.isEditing.set(false);
    this.inventarioIdSeleccionado.set(null);
    this.inventarioForm.reset();
    this.visible.set(true);
  }

  onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
    this.cargarInventarios();
  }

  editar(inventario: InventarioResponse): void {
    this.isEditing.set(true);
    this.inventarioIdSeleccionado.set(inventario.inventarioId);
    
    const producto = this.productos().find(p => p.productoId === inventario.productoId);
    
    this.inventarioForm.patchValue({
      producto: producto || null,
      stockDisponible: inventario.stockDisponible,
      stockReservado: inventario.stockReservado || 0,
      stockEnTransito: inventario.stockEnTransito || 0,
      stockMinimo: inventario.stockMinimo,
      stockMaximo: inventario.stockMaximo || 0,
      puntoReorden: inventario.puntoReorden,
      ubicacionAlmacen: inventario.ubicacionAlmacen,
      observaciones: inventario.observaciones
    });
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.inventarioIdSeleccionado.set(null);
    this.inventarioForm.reset();
    this.isEditing.set(false);
  }

  onSubmit(): void {
    if (this.inventarioForm.valid) {
      const formValue = this.inventarioForm.value;
      const inventarioData: InventarioCreateRequest = {
        productoId: formValue.producto!.productoId,
        stockDisponible: formValue.stockDisponible!,
        stockReservado: formValue.stockReservado ?? 0,
        stockEnTransito: formValue.stockEnTransito ?? 0,
        stockMinimo: formValue.stockMinimo!,
        stockMaximo: formValue.stockMaximo ?? 0,
        puntoReorden: formValue.puntoReorden!,
        ubicacionAlmacen: formValue.ubicacionAlmacen ?? '',
        observaciones: formValue.observaciones ?? ''
      };

      if (this.isEditing()) {
        this.inventarioService.actualizarInventario(this.inventarioIdSeleccionado()!, inventarioData).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Inventario actualizado correctamente' 
          });
          this.cargarInventarios();
          this.closeDialog();
        });
      } else {
        this.inventarioService.crearInventario(inventarioData).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Inventario creado correctamente' 
          });
          this.first.set(0);
          this.cargarInventarios();
          this.closeDialog();
        });
      }
    }
  }

  eliminar(inventario: InventarioResponse): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar este inventario?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.inventarioService.eliminarInventario(inventario.inventarioId).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Inventario eliminado correctamente' 
          });
          this.cargarInventarios();
        });
      }
    });
  }
}
