import { Component, inject, signal } from '@angular/core';
import { UnidadMedida } from '../../models/UnidadMedida';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { FormsModule, ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { FloatLabel } from 'primeng/floatlabel';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmationService, MessageService } from 'primeng/api';
import { UnidaMedidaService } from '../../service/unida-medida-service';
import { KeyFilterModule } from 'primeng/keyfilter';
import { MessageModule } from 'primeng/message';
import { Dialog } from 'primeng/dialog';
import { Toast } from 'primeng/toast';

interface Column {
  field: keyof UnidadMedida | 'acciones';
  header: string;
}
@Component({
  selector: 'app-unidad-medida-component',
  imports: [TableModule, CommonModule, ButtonModule, ConfirmDialogModule, InputTextModule, FloatLabel, FormsModule, KeyFilterModule, MessageModule, ReactiveFormsModule, Dialog, Toast],
  templateUrl: './unidad-medida-component.html',
  styleUrl: './unidad-medida-component.css',
  providers: [ConfirmationService, MessageService]
})
export class UnidadMedidaComponent {

  unidadMedidas = signal<UnidadMedida[]>([]);
  visible = signal<boolean>(false);
  isEditing = signal<boolean>(false);
  unidadMedidaId = signal<number | null>(null);
  loading = signal<boolean>(false);

  unidadForm = new FormGroup({
    nombre: new FormControl<string>('', Validators.required),
    abreviatura: new FormControl<string>('', [Validators.required, Validators.minLength(1), Validators.maxLength(5)])
  });

  cols: Column[] = [
    { field: 'unidadMedidaId', header: 'ID' },
    { field: 'nombre', header: 'Nombre' },
    { field: 'abreviatura', header: 'Abreviatura' },
    { field: 'acciones', header: 'Acciones' }
  ];
  private confirmationService = inject(ConfirmationService);
  private unidadMedidaService = inject(UnidaMedidaService);
  private messageService = inject(MessageService);

  constructor() {
    this.cargarDatos();
  }

  private cargarDatos() {
    this.loading.set(true);
    this.unidadMedidaService.obtenerUnidadesMedida().subscribe(unidades => {
      this.unidadMedidas.set(unidades);
      this.loading.set(false);
    });
  }

  showDialog(): void {
    this.isEditing.set(false);
    this.unidadMedidaId.set(null);
    this.unidadForm.reset();
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.unidadMedidaId.set(null);
    this.unidadForm.reset();
    this.isEditing.set(false);
  }

  onSubmit(): void {
    if (this.unidadForm.valid) {
      const formValue = this.unidadForm.value;
      
      if (this.isEditing()) {
        const unidadActualizada: UnidadMedida = {
          unidadMedidaId: this.unidadMedidaId(),
          nombre: formValue.nombre!,
          abreviatura: formValue.abreviatura!
        };
        this.unidadMedidaService.actualizarUnidadMedida(unidadActualizada).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Unidad de medida actualizada correctamente' 
          });
          this.cargarDatos();
          this.closeDialog();
        });
      } else {
        const nuevaUnidad: UnidadMedida = {
          unidadMedidaId: this.unidadMedidaId(),
          nombre: formValue.nombre!,
          abreviatura: formValue.abreviatura!
        };
        this.unidadMedidaService.crearUnidadMedida(nuevaUnidad).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Unidad de medida creada correctamente' 
          });
          this.cargarDatos();
          this.closeDialog();
        });
      }
    }
  }

  editar(unidad: UnidadMedida): void {
    this.isEditing.set(true);
    this.unidadMedidaId.set(unidad.unidadMedidaId);
    this.unidadForm.patchValue({
      nombre: unidad.nombre,
      abreviatura: unidad.abreviatura
    });
    this.visible.set(true);
  }

  eliminar(unidad: UnidadMedida): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar la unidad de medida "${unidad.nombre}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.unidadMedidaService.eliminarUnidadMedida(unidad.unidadMedidaId!).subscribe(() => {
          this.messageService.add({ 
            severity: 'success', 
            summary: 'Éxito', 
            detail: 'Unidad de medida eliminada correctamente' 
          });
          this.cargarDatos();
        });
      }
    });
  }
}
