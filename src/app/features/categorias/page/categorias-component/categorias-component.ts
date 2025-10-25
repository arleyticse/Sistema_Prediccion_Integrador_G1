import { Component, inject, OnInit, signal } from '@angular/core';
import { Categoria } from '../../models/Categoria';
import { CategoriaServicio } from '../../services/categoria-servicio';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabel } from 'primeng/floatlabel';
import { FormsModule } from '@angular/forms';

interface Column {
  field: keyof Categoria | 'acciones';
  header: string;
}


@Component({
  selector: 'app-categorias-component',
  imports: [TableModule, CommonModule, ButtonModule, ConfirmDialogModule, InputTextModule, FloatLabel, FormsModule],
  templateUrl: './categorias-component.html',
  styleUrl: './categorias-component.css',
  providers: [ConfirmationService]
})
export class CategoriasComponent {

  categorias = signal<Categoria[]>([]);

  nombreCategoria = signal<string>('');
  categoriaId = signal<number | null>(null);
  loading = signal<boolean>(false);

  cols: Column[] = [
    { field: 'categoriaId', header: 'ID' },
    { field: 'nombre', header: 'Nombre' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private categoriaServicio = inject(CategoriaServicio);
  private confirmationService = inject(ConfirmationService);

  constructor() {
    this.cargarCategorias();
  }

  private cargarCategorias(): void {
    this.loading.set(true);
    this.categoriaServicio.obtenerCategorias().subscribe(categorias => {
      this.categorias.set(categorias);
      console.log("Categorias cargadas:", categorias);
      this.loading.set(false);
    });
  }

  editar(categoria: Categoria): void {
    this.nombreCategoria.set(categoria.nombre);
    this.categoriaId.set(categoria.categoriaId);
  }

  eliminar(categoria: Categoria): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar la categoría "${categoria.nombre}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.categoriaServicio.eliminarCategoria(categoria.categoriaId!).subscribe(() => {
          this.cargarCategorias();
        });
      }
    });
  }

  actualizarCategoria() {
    const categoriaActualizada: Categoria = {
      categoriaId: this.categoriaId()!,
      nombre: this.nombreCategoria()
    };
    this.categoriaServicio.actualizarCategoria(categoriaActualizada).subscribe(() => {
      this.cargarCategorias();
    });
    this.categoriaId.set(null);
    this.nombreCategoria.set('');
  }


  nuevaCategoria() {
    const nuevaCategoria: Categoria = {
      categoriaId: null,
      nombre: this.nombreCategoria()
    };
    this.categoriaServicio.crearCategoria(nuevaCategoria).subscribe(() => {
      this.cargarCategorias();
    });
    this.nombreCategoria.set('');
  }
  cancelar() {
    this.categoriaId.set(null);
    this.nombreCategoria.set('');
  }
}