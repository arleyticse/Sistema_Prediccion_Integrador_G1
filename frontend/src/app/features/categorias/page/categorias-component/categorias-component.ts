import { Component, inject, OnInit, signal } from '@angular/core';
import { Categoria } from '../../models/Categoria';
import { CategoriaServicio } from '../../services/categoria-servicio';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabel } from 'primeng/floatlabel';
import { FormsModule, ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { KeyFilterModule } from 'primeng/keyfilter';
import { MessageModule } from 'primeng/message';
import { Dialog } from 'primeng/dialog';
import { Toast } from 'primeng/toast';

interface Column {
  field: keyof Categoria | 'acciones';
  header: string;
}


@Component({
  selector: 'app-categorias-component',
  imports: [TableModule, CommonModule, ButtonModule, ConfirmDialogModule, InputTextModule, FloatLabel, FormsModule, KeyFilterModule, MessageModule, ReactiveFormsModule, Dialog, Toast],
  templateUrl: './categorias-component.html',
  styleUrl: './categorias-component.css',
  providers: [ConfirmationService, MessageService]
})
export class CategoriasComponent {

  categorias = signal<Categoria[]>([]);
  visible = signal<boolean>(false);
  isEditing = signal<boolean>(false);
  categoriaId = signal<number | null>(null);
  loading = signal<boolean>(false);

  categoriaForm = new FormGroup({
    nombre: new FormControl<string>('', Validators.required)
  });

  cols: Column[] = [
    { field: 'categoriaId', header: 'ID' },
    { field: 'nombre', header: 'Nombre' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private categoriaServicio = inject(CategoriaServicio);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);

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

  showDialog(): void {
    this.isEditing.set(false);
    this.categoriaId.set(null);
    this.categoriaForm.reset();
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.categoriaId.set(null);
    this.categoriaForm.reset();
    this.isEditing.set(false);
  }

  editar(categoria: Categoria): void {
    this.isEditing.set(true);
    this.categoriaId.set(categoria.categoriaId);
    this.categoriaForm.patchValue({
      nombre: categoria.nombre
    });
    this.visible.set(true);
  }

  eliminar(categoria: Categoria): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar la categoría "${categoria.nombre}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.categoriaServicio.eliminarCategoria(categoria.categoriaId!).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Categoría eliminada correctamente' 
          });
          this.cargarCategorias();
        });
      }
    });
  }

  onSubmit(): void {
    if (this.categoriaForm.valid) {
      const formValue = this.categoriaForm.value;
      
      if (this.isEditing()) {
        const categoriaActualizada: Categoria = {
          categoriaId: this.categoriaId()!,
          nombre: formValue.nombre!
        };
        this.categoriaServicio.actualizarCategoria(categoriaActualizada).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Categoría actualizada correctamente' 
          });
          this.cargarCategorias();
          this.closeDialog();
        });
      } else {
        const nuevaCategoria: Categoria = {
          categoriaId: null,
          nombre: formValue.nombre!
        };
        this.categoriaServicio.crearCategoria(nuevaCategoria).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Categoría creada correctamente' 
          });
          this.cargarCategorias();
          this.closeDialog();
        });
      }
    }
  }
}