import { Component, inject, signal } from '@angular/core';
import { ProductoResponse } from '../../models/ProductoResponse';
import { ConfirmationService } from 'primeng/api';
import { ProductoService } from '../../service/producto-service';
import { Dialog } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Categoria } from '../../../categorias/models/Categoria';
import { CategoriaServicio } from '../../../categorias/services/categoria-servicio';
import { UnidaMedidaService } from '../../../unidades-medida/service/unida-medida-service';
import { UnidadMedida } from '../../../unidades-medida/models/UnidadMedida';
import { Select } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ProductoRequest } from '../../models/ProductoRequest';
interface Column {
  field: keyof ProductoResponse | 'acciones';
  header: string;
}
@Component({
  selector: 'app-productos-component',
  imports: [Dialog, ButtonModule, InputTextModule, ReactiveFormsModule, Select, TableModule,ConfirmDialogModule],
  templateUrl: './productos-component.html',
  styleUrl: './productos-component.css',
  providers: [ConfirmationService]
})
export class ProductosComponent {
  productos = signal<ProductoResponse[]>([]);
  visible = signal<boolean>(false);
  categorias = signal<Categoria[]>([]);
  unidadMedidas = signal<UnidadMedida[]>([]);
  isEditing = signal<boolean>(false);
  productoIdSeleccionado = signal<number | null>(null);
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

  private confirmationService = inject(ConfirmationService);
  private productoService = inject(ProductoService);
  private categoriaService = inject(CategoriaServicio);
  private unidadMedidaService = inject(UnidaMedidaService);

  constructor() {
    this.cargarDatos();
  }

  private cargarDatos(): void {
    this.productoService.obtenerProductos(0, 10).subscribe(productos => {
      this.productos.set(productos.content);
    });
    this.categoriaService.obtenerCategorias().subscribe(categorias => {
      this.categorias.set(categorias);
    });
    this.unidadMedidaService.obtenerUnidadesMedida().subscribe(unidades => {
      this.unidadMedidas.set(unidades);
    });
  }

  showDialog(): void {
    this.isEditing.set(false);
    this.productoIdSeleccionado.set(null);
    this.productoForm.reset();
    this.visible.set(true);
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
          this.cargarDatos();
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
          this.cargarDatos();
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
          this.productos.update(prods => prods.filter(p => p.productoId !== producto.productoId));
        });
      }
    });
  }
} 
