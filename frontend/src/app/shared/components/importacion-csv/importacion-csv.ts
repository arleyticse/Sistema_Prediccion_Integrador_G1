import { Component, input, output, signal, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule, FileUploadHandlerEvent } from 'primeng/fileupload';
import { ProgressBarModule } from 'primeng/progressbar';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { ImportacionService } from '../../service/importacion-service';
import { ImportacionResponse, DetalleErrorImportacion } from '../../models/ImportacionResponse';

@Component({
  selector: 'app-importacion-csv',
  imports: [
    CommonModule,
    ButtonModule,
    FileUploadModule,
    ProgressBarModule,
    TagModule,
    ToastModule,
    DialogModule,
    TableModule
  ],
  providers: [MessageService],
  templateUrl: './importacion-csv.html',
  styleUrl: './importacion-csv.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImportacionCsvComponent {
  // Inputs
  endpoint = input.required<string>();
  tipoImportacion = input.required<string>();
  nombrePlantilla = input.required<string>();
  
  // Outputs
  importacionCompletada = output<void>();
  
  // Services
  private readonly importacionService = inject(ImportacionService);
  private readonly messageService = inject(MessageService);
  
  // State
  visible = signal<boolean>(false);
  archivoSeleccionado = signal<File | null>(null);
  validando = signal<boolean>(false);
  importando = signal<boolean>(false);
  archivoValidado = signal<boolean>(false);
  resultadoValidacion = signal<ImportacionResponse | null>(null);
  mostrarErrores = signal<boolean>(false);

  showDialog(): void {
    this.visible.set(true);
    this.resetearEstado();
  }

  closeDialog(): void {
    this.visible.set(false);
    this.resetearEstado();
  }

  private resetearEstado(): void {
    this.archivoSeleccionado.set(null);
    this.validando.set(false);
    this.importando.set(false);
    this.archivoValidado.set(false);
    this.resultadoValidacion.set(null);
    this.mostrarErrores.set(false);
  }

  onFileSelect(event: FileUploadHandlerEvent): void {
    const file = event.files[0];
    
    if (!file) {
      return;
    }

    // Validar extensión
    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.messageService.add({
        severity: 'error',
        summary: 'Archivo inválido',
        detail: 'Solo se permiten archivos CSV',
        life: 5000
      });
      return;
    }

    this.archivoSeleccionado.set(file);
    this.archivoValidado.set(false);
    this.resultadoValidacion.set(null);
    
    // Auto-validar al seleccionar archivo
    this.validarArchivo();
  }

  validarArchivo(): void {
    const archivo = this.archivoSeleccionado();
    
    if (!archivo) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Sin archivo',
        detail: 'Por favor seleccione un archivo CSV',
        life: 3000
      });
      return;
    }

    this.validando.set(true);
    this.archivoValidado.set(false);

    this.importacionService.validar(archivo, this.endpoint()).subscribe({
      next: (response) => {
        this.validando.set(false);
        this.resultadoValidacion.set(response);
        
        console.log('✅ Validación completada:', response);
        
        // Validación exitosa: estado COMPLETADA o registrosFallidos === 0
        const esValido = response.estado === 'COMPLETADA' || response.registrosFallidos === 0;
        
        if (esValido) {
          this.archivoValidado.set(true);
          this.messageService.add({
            severity: 'success',
            summary: 'Validación exitosa',
            detail: `Archivo válido y listo para importar`,
            life: 5000
          });
        } else {
          this.archivoValidado.set(false);
          this.messageService.add({
            severity: 'error',
            summary: 'Errores de validación',
            detail: `${response.registrosFallidos} errores encontrados en el archivo`,
            life: 7000
          });
        }
      },
      error: (error: Error) => {
        this.validando.set(false);
        console.error('❌ Error validando:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error de validación',
          detail: error.message || 'No se pudo validar el archivo',
          life: 5000
        });
      }
    });
  }

  importarArchivo(): void {
    const archivo = this.archivoSeleccionado();
    
    if (!archivo) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Sin archivo',
        detail: 'Por favor seleccione un archivo CSV',
        life: 3000
      });
      return;
    }

    if (!this.archivoValidado()) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Archivo no validado',
        detail: 'Debe validar el archivo antes de importar',
        life: 3000
      });
      return;
    }

    this.importando.set(true);

    this.importacionService.importar(archivo, this.endpoint()).subscribe({
      next: (response) => {
        this.importando.set(false);
        
        console.log('✅ Importación completada:', response);
        
        if (response.estado === 'COMPLETADA') {
          this.messageService.add({
            severity: 'success',
            summary: 'Importación completada',
            detail: `${response.registrosExitosos} registros importados exitosamente`,
            life: 5000
          });
          this.importacionCompletada.emit();
          this.closeDialog();
        } else if (response.estado === 'PARCIAL') {
          this.resultadoValidacion.set(response);
          this.messageService.add({
            severity: 'warn',
            summary: 'Importación parcial',
            detail: `${response.registrosExitosos} exitosos, ${response.registrosFallidos} fallidos`,
            life: 7000
          });
          this.importacionCompletada.emit();
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Importación fallida',
            detail: 'No se pudo completar la importación',
            life: 5000
          });
        }
      },
      error: (error: Error) => {
        this.importando.set(false);
        console.error('❌ Error importando:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error de importación',
          detail: error.message || 'No se pudo importar el archivo',
          life: 5000
        });
      }
    });
  }

  descargarPlantilla(): void {
    this.importacionService.descargarPlantilla(this.endpoint(), this.nombrePlantilla()).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = this.nombrePlantilla();
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.messageService.add({
          severity: 'success',
          summary: 'Plantilla descargada',
          detail: `Archivo ${this.nombrePlantilla()} descargado`,
          life: 3000
        });
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error al descargar',
          detail: error.message || 'No se pudo descargar la plantilla',
          life: 5000
        });
      }
    });
  }

  verErrores(): void {
    this.mostrarErrores.set(true);
  }

  cerrarErrores(): void {
    this.mostrarErrores.set(false);
  }

  getSeverityTag(estado: string): 'success' | 'warn' | 'danger' {
    switch (estado) {
      case 'COMPLETADA':
        return 'success';
      case 'PARCIAL':
        return 'warn';
      case 'FALLIDA':
      case 'INVALIDO':
      case 'ERROR_LECTURA':
        return 'danger';
      default:
        return 'warn';
    }
  }
}
