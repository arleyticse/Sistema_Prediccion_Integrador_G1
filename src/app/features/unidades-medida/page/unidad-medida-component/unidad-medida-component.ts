import { Component, inject, signal } from '@angular/core';
import { UnidadMedida } from '../../models/UnidadMedida';
import { TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { FormsModule } from '@angular/forms';
import { FloatLabel } from 'primeng/floatlabel';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmationService } from 'primeng/api';
import { UnidaMedidaService } from '../../service/unida-medida-service';

interface Column {
  field: keyof UnidadMedida | 'acciones';
  header: string;
}
@Component({
  selector: 'app-unidad-medida-component',
  imports: [TableModule, CommonModule, ButtonModule, ConfirmDialogModule, InputTextModule, FloatLabel, FormsModule],
  templateUrl: './unidad-medida-component.html',
  styleUrl: './unidad-medida-component.css',
  providers: [ConfirmationService]
})
export class UnidadMedidaComponent {

  unidadMedidas = signal<UnidadMedida[]>([]);
  unidadMedidaId = signal<number | null>(null);
  nombre = signal<string>('');
  abreviatura = signal<string>('');
    loading = signal<boolean>(false);

  cols: Column[] = [
    { field: 'unidadMedidaId', header: 'ID' },
    { field: 'nombre', header: 'Nombre' },
    { field: 'abreviatura', header: 'Abreviatura' },
    { field: 'acciones', header: 'Acciones' }
  ];
  private confirmationService = inject(ConfirmationService);
  private unidadMedidaService = inject(UnidaMedidaService);

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

  actualizarUnidadMedida() {
    const unidadActualizada: UnidadMedida = {
      unidadMedidaId: this.unidadMedidaId(),
      nombre: this.nombre(),
      abreviatura: this.abreviatura()
    };
    this.unidadMedidaService.actualizarUnidadMedida(unidadActualizada).subscribe(() => {
      this.cargarDatos();
      this.cancelar();
    });
  }
  cancelar() {
    this.unidadMedidaId.set(null);
    this.nombre.set('');
    this.abreviatura.set('');
  }
  nuevaUnidadMedida() {
    const nuevaUnidad: UnidadMedida = {
      unidadMedidaId: this.unidadMedidaId(),
      nombre: this.nombre(),
      abreviatura: this.abreviatura()
    };
    this.unidadMedidaService.crearUnidadMedida(nuevaUnidad).subscribe(() => {
      this.cargarDatos();
      this.cancelar();
    });
  }
  eliminar(unidad: UnidadMedida) {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar la unidad de medida "${unidad.nombre}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.unidadMedidaService.eliminarUnidadMedida(unidad.unidadMedidaId!).subscribe(() => {
          this.cargarDatos();
        });
      }
    });
  }
  editar(unidad: UnidadMedida) {
    this.unidadMedidaId.set(unidad.unidadMedidaId);
    this.nombre.set(unidad.nombre);
    this.abreviatura.set(unidad.abreviatura);
  }
}
