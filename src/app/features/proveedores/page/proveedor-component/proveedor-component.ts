import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { Proveedor } from '../../model/Proveedor';
import { ProveedorService } from '../../service/proveedor-service';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { FormsModule } from '@angular/forms';
import { TextareaModule } from 'primeng/textarea';

interface Column {
  field: keyof Proveedor | 'acciones';
  header: string;
}

@Component({
  selector: 'app-proveedor-component',
  imports: [
    ButtonModule,
    InputTextModule,
    ReactiveFormsModule,
    TableModule,
    ConfirmDialogModule,
    DialogModule,
    InputNumberModule,
    CheckboxModule,
    FormsModule,
    TextareaModule
  ],
  templateUrl: './proveedor-component.html',
  styleUrl: './proveedor-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [ConfirmationService]
})
export class ProveedorComponent {
  proveedores = signal<Proveedor[]>([]);
  visible = signal<boolean>(false);
  isEditing = signal<boolean>(false);
  proveedorIdSeleccionado = signal<number | null>(null);

  proveedorForm = new FormGroup({
    razonSocial: new FormControl<string>('', Validators.required),
    nombreComercial: new FormControl<string>('', Validators.required),
    rucNit: new FormControl<string>('', Validators.required),
    telefono: new FormControl<string>('', Validators.required),
    email: new FormControl<string>('', [Validators.required, Validators.email]),
    direccion: new FormControl<string>('', Validators.required),
    ciudad: new FormControl<string>('', Validators.required),
    pais: new FormControl<string>('', Validators.required),
    personaContacto: new FormControl<string>('', Validators.required),
    tiempoEntregaDias: new FormControl<number>(0, [Validators.required, Validators.min(1)]),
    diasCredito: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
    calificacion: new FormControl<number>(5, [Validators.required, Validators.min(0), Validators.max(10)]),
    observaciones: new FormControl<string>('')
  });

  cols: Column[] = [
    { field: 'razonSocial', header: 'Razón Social' },
    { field: 'nombreComercial', header: 'Nombre Comercial' },
    { field: 'email', header: 'Email' },
    { field: 'telefono', header: 'Teléfono' },
    { field: 'ciudad', header: 'Ciudad' },
    { field: 'calificacion', header: 'Calificación' },
    { field: 'acciones', header: 'Acciones' }
  ];

  private readonly confirmationService = inject(ConfirmationService);
  private readonly proveedorService = inject(ProveedorService);

  constructor() {
    this.cargarProveedores();
  }

  private cargarProveedores(): void {
    this.proveedorService.getProveedores().subscribe(proveedores => {
      this.proveedores.set(proveedores);
    });
  }

  showDialog(): void {
    this.isEditing.set(false);
    this.proveedorIdSeleccionado.set(null);
    this.proveedorForm.reset();
    this.visible.set(true);
  }

  editar(proveedor: Proveedor): void {
    this.isEditing.set(true);
    this.proveedorIdSeleccionado.set(proveedor.proveedorId);
    this.proveedorForm.patchValue({
      razonSocial: proveedor.razonSocial,
      nombreComercial: proveedor.nombreComercial,
      rucNit: proveedor.rucNit,
      telefono: proveedor.telefono,
      email: proveedor.email,
      direccion: proveedor.direccion,
      ciudad: proveedor.ciudad,
      pais: proveedor.pais,
      personaContacto: proveedor.personaContacto,
      tiempoEntregaDias: proveedor.tiempoEntregaDias,
      diasCredito: proveedor.diasCredito,
      calificacion: proveedor.calificacion,
      observaciones: proveedor.observaciones
    });
    this.visible.set(true);
  }

  closeDialog(): void {
    this.visible.set(false);
    this.proveedorIdSeleccionado.set(null);
    this.proveedorForm.reset();
    this.isEditing.set(false);
  }

  onSubmit(): void {
    if (this.proveedorForm.valid) {
      const formValue = this.proveedorForm.value as Proveedor;

      if (this.isEditing()) {
        this.proveedorService.updateProveedor(this.proveedorIdSeleccionado()!, formValue).subscribe(() => {
          this.cargarProveedores();
          this.closeDialog();
        });
      } else {
        this.proveedorService.createProveedor(formValue).subscribe(() => {
          this.cargarProveedores();
          this.closeDialog();
        });
      }
    }
  }

  eliminar(proveedor: Proveedor): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar el proveedor "${proveedor.razonSocial}"?`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.proveedorService.deleteProveedor(proveedor.proveedorId).subscribe(() => {
          this.cargarProveedores();
        });
      }
    });
  }
}
