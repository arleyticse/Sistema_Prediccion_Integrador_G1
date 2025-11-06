import { Component, inject, signal, ViewChild } from '@angular/core';
import { ProductoResponse } from '../../models/ProductoResponse';
import { ConfirmationService } from 'primeng/api';
import { ProductoService } from '../../service/producto-service';
import { Dialog } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Categoria } from '../../../categorias/models/Categoria';
import { CategoriaServicio } from '../../../categorias/services/categoria-servicio';
import { UnidaMedidaService } from '../../../unidades-medida/service/unida-medida-service';
import { UnidadMedida } from '../../../unidades-medida/models/UnidadMedida';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ProductoRequest } from '../../models/ProductoRequest';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { ChangeDetectionStrategy } from '@angular/core';
import { ImportacionCsvComponent } from '../../../../shared/components/importacion-csv/importacion-csv';

interface Column {
  field: keyof ProductoResponse | 'acciones';
  header: string;
}
@Component({
  selector: 'app-productos-component',
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
    ImportacionCsvComponent
  ],
  templateUrl: './productos-component.html',
  styleUrl: './productos-component.css',
  providers: [ConfirmationService],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductosComponent {

  @ViewChild('dt') dataTable: any;
  @ViewChild('importacionCsv') importacionCsv!: ImportacionCsvComponent;

  productos = signal<ProductoResponse[]>([]);
  visible = signal<boolean>(false);
  categorias = signal<Categoria[]>([]);
  unidadMedidas = signal<UnidadMedida[]>([]);
  isEditing = signal<boolean>(false);
  productoIdSeleccionado = signal<number | null>(null);
  searchValue = signal<string>('');
  loading = signal<boolean>(false);

  first = signal<number>(0);
  rows = signal<number>(10);
  totalRecords = signal<number>(0);
  rowsPerPageOptions = [10, 20, 30];

  productoForm = new FormGroup({
    nombre: new FormControl<string>('', Validators.required),
    categoria: new FormControl<Categoria | null>(null, Validators.required),
    unidadMedida: new FormControl<UnidadMedida | null>(null, Validators.required),
    diasLeadTime: new FormControl<number>(0, [Validators.required, Validators.min(1)]),
    costoAdquisicion: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    costoMantenimiento: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    costoPedido: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
  });

  cols: Column[] = [
    { field: 'nombre', header: 'Nombre' },
    { field: 'categoria', header: 'Categoría' },
    { field: 'unidadMedida', header: 'Unidad de Medida' },
    { field: 'stockDisponible', header: 'Stock' },
    { field: 'costoAdquisicion', header: 'Costo de Adquisición' },
    { field: 'estadoInventario', header: 'Estado' },
    { field: 'acciones', header: 'Acciones' }
  ];

private readonly confirmationService = inject(ConfirmationService);
private readonly productoService = inject(ProductoService);
private readonly categoriaService = inject(CategoriaServicio);
private readonly unidadMedidaService = inject(UnidaMedidaService);

  constructor() {
    this.cargarDatos();
  }

  private cargarDatos(): void {
    this.cargarProductos();
    this.categoriaService.obtenerCategorias().subscribe(categorias => {
      this.categorias.set(categorias);
    });
    this.unidadMedidaService.obtenerUnidadesMedida().subscribe(unidades => {
      this.unidadMedidas.set(unidades);
    });
  }

  cargarProductos(): void {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());
    this.productoService.obtenerProductos(page, this.rows()).subscribe(response => {
      this.productos.set(response.content);
      this.totalRecords.set(response.page.totalElements);
      this.loading.set(false);
    });
  }

  abrirImportacion(): void {
    this.importacionCsv.showDialog();
  }
  showDialog(): void {
    this.isEditing.set(false);
    this.productoIdSeleccionado.set(null);
    this.productoForm.reset();
    this.visible.set(true);
  }

  onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
    this.cargarProductos();
  }
  editar(producto: ProductoResponse): void {
    this.isEditing.set(true);
    this.productoIdSeleccionado.set(producto.productoId);
    this.productoForm.patchValue({
      nombre: producto.nombre,
      categoria: producto.categoria,
      unidadMedida: producto.unidadMedida,
      diasLeadTime: producto.diasLeadTime,
      costoAdquisicion: producto.costoAdquisicion,
      costoMantenimiento: producto.costoMantenimiento,
      costoPedido: producto.costoPedido,
    });
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.productoIdSeleccionado.set(null);
    this.productoForm.reset();
    this.isEditing.set(false);
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
    if (this.productoForm.valid) {
      const formValue = this.productoForm.value;
      if (this.isEditing()) {
        const productoActualizado: ProductoRequest = {
          nombre: formValue.nombre!,
          categoriaId: formValue.categoria!.categoriaId!,
          unidadMedidaId: formValue.unidadMedida!.unidadMedidaId!,
          diasLeadTime: formValue.diasLeadTime!,
          costoAdquisicion: formValue.costoAdquisicion!,
          costoMantenimiento: formValue.costoMantenimiento!,
          costoPedido: formValue.costoPedido!
        };
        this.productoService.actualizarProducto(productoActualizado, this.productoIdSeleccionado()!).subscribe(() => {
           this.cargarProductos();
          this.closeDialog();
        });
      } else {
        const nuevoProducto: ProductoRequest = {
          nombre: formValue.nombre!,
          categoriaId: formValue.categoria!.categoriaId!,
          unidadMedidaId: formValue.unidadMedida!.unidadMedidaId!,
          diasLeadTime: formValue.diasLeadTime!,
          costoAdquisicion: formValue.costoAdquisicion!,
          costoMantenimiento: formValue.costoMantenimiento!,
          costoPedido: formValue.costoPedido!
        };
        this.productoService.crearProducto(nuevoProducto).subscribe(() => {
          this.first.set(0);
          this.cargarProductos();
          this.closeDialog();
        });
      }
    }
  }

  eliminar(producto: ProductoResponse): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar el producto "${producto.nombre}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.productoService.eliminarProducto(producto.productoId).subscribe(() => {
  this.cargarProductos();
        });
      }
    });
  }
} 
