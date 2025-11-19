import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { HttpClientModule } from '@angular/common/http';

// PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule } from 'primeng/fileupload';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { TextareaModule } from 'primeng/textarea';

// Services & Models
import { ConfiguracionEmpresaService } from '../../../core/services/configuracion/configuracion-empresa.service';
import { ConfiguracionEmpresa } from '../../../shared/models/configuracion/configuracion-empresa.model';

/**
 * Componente para gestionar la configuración de la empresa.
 * 
 * Permite actualizar:
 * - Información básica (nombre, RUC, dirección, contacto)
 * - Logo de la empresa (Base64, max 100KB)
 * 
 * Solo accesible por usuarios con rol GERENTE.
 * 
 * @version 1.0
 * @since 2025-11-19
 */
@Component({
  selector: 'app-configuracion-empresa',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    CardModule,
    InputTextModule,
    ButtonModule,
    FileUploadModule,
    ProgressSpinnerModule,
    ToastModule,
    TextareaModule
  ],
  providers: [MessageService],
  templateUrl: './configuracion-empresa.component.html',
  styleUrls: ['./configuracion-empresa.component.css']
})
export class ConfiguracionEmpresaComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ConfiguracionEmpresaService);
  private readonly messageService = inject(MessageService);

  // Signals
  cargando = signal<boolean>(false);
  guardando = signal<boolean>(false);
  logoUrl = signal<string | null>(null);
  archivoLogo = signal<File | null>(null);

  // Form
  configuracionForm!: FormGroup;

  ngOnInit(): void {
    this.inicializarFormulario();
    this.cargarConfiguracion();
  }

  /**
   * Inicializa el formulario con validaciones.
   */
  private inicializarFormulario(): void {
    this.configuracionForm = this.fb.group({
      nombreEmpresa: ['', [Validators.required, Validators.maxLength(255)]],
      ruc: ['', Validators.maxLength(20)],
      direccion: [''],
      telefono: ['', Validators.maxLength(50)],
      email: ['', [Validators.email, Validators.maxLength(255)]],
      nombreContacto: ['', Validators.maxLength(255)],
      cargoContacto: ['', Validators.maxLength(100)]
    });
  }

  /**
   * Carga la configuración actual de la empresa.
   */
  cargarConfiguracion(): void {
    this.cargando.set(true);

    this.service.obtenerConfiguracion().subscribe({
      next: (config) => {
        this.configuracionForm.patchValue(config);

        // Mostrar logo si existe
        if (config.logoBase64 && config.logoMimeType) {
          const dataUrl = this.service.obtenerDataUrlLogo(config.logoBase64, config.logoMimeType);
          this.logoUrl.set(dataUrl);
        }

        this.cargando.set(false);
      },
      error: (error) => {
        console.error('Error al cargar configuración:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar la configuración de la empresa'
        });
        this.cargando.set(false);
      }
    });
  }

  /**
   * Maneja la selección de un archivo de logo.
   * Evento onSelect de p-fileupload
   */
  async onSeleccionarLogo(event: any): Promise<void> {
    // FileUpload onSelect retorna event.currentFiles
    const files = event.currentFiles || event.files;
    const file: File = files[0];

    if (!file) {
      return;
    }

    // Validar tipo
    if (!this.service.validarTipoImagen(file)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Tipo no válido',
        detail: 'Solo se permiten imágenes PNG, JPEG o WebP'
      });
      event.currentFiles = [];
      return;
    }

    // Si excede 100KB, optimizar
    if (!this.service.validarTamanoArchivo(file, 100)) {
      this.messageService.add({
        severity: 'info',
        summary: 'Optimizando imagen',
        detail: 'La imagen será comprimida para cumplir con el límite de 100KB'
      });

      try {
        const optimizado = await this.service.optimizarImagen(file, 800, 600, 0.7);
        this.archivoLogo.set(optimizado);
        this.mostrarVistaPrevia(optimizado);
      } catch (error) {
        console.error('Error al optimizar imagen:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo optimizar la imagen'
        });
      }
    } else {
      this.archivoLogo.set(file);
      this.mostrarVistaPrevia(file);
    }
  }

  /**
   * Muestra vista previa del logo seleccionado.
   */
  private mostrarVistaPrevia(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.logoUrl.set(e.target.result);
    };
    reader.readAsDataURL(file);
  }

  /**
   * Elimina el logo actual.
   */
  eliminarLogo(): void {
    if (!confirm('¿Está seguro de eliminar el logo de la empresa?')) {
      return;
    }

    this.guardando.set(true);

    this.service.eliminarLogo().subscribe({
      next: () => {
        this.logoUrl.set(null);
        this.archivoLogo.set(null);
        this.guardando.set(false);

        this.messageService.add({
          severity: 'success',
          summary: 'Logo eliminado',
          detail: 'El logo ha sido eliminado correctamente'
        });
      },
      error: (error) => {
        console.error('Error al eliminar logo:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo eliminar el logo'
        });
        this.guardando.set(false);
      }
    });
  }

  /**
   * Guarda la configuración de la empresa.
   */
  async guardarConfiguracion(): Promise<void> {
    if (this.configuracionForm.invalid) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario inválido',
        detail: 'Por favor, corrija los errores en el formulario'
      });
      return;
    }

    this.guardando.set(true);

    try {
      const configuracion: ConfiguracionEmpresa = {
        id: 1,
        ...this.configuracionForm.value
      };

      // Si hay un nuevo logo, convertirlo a Base64
      if (this.archivoLogo()) {
        const file = this.archivoLogo()!;
        const base64 = await this.service.convertirImagenABase64(file);
        configuracion.logoBase64 = base64;
        configuracion.logoMimeType = file.type;
      }

      // Guardar configuración
      this.service.actualizarConfiguracion(configuracion).subscribe({
        next: (actualizada) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Configuración guardada',
            detail: 'La configuración de la empresa ha sido actualizada correctamente'
          });

          // Actualizar vista previa si hay logo nuevo
          if (actualizada.logoBase64 && actualizada.logoMimeType) {
            const dataUrl = this.service.obtenerDataUrlLogo(
              actualizada.logoBase64,
              actualizada.logoMimeType
            );
            this.logoUrl.set(dataUrl);
          }

          // Resetear archivo temporal
          this.archivoLogo.set(null);
          this.guardando.set(false);
        },
        error: (error) => {
          console.error('Error al guardar configuración:', error);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo guardar la configuración'
          });
          this.guardando.set(false);
        }
      });
    } catch (error) {
      console.error('Error al procesar logo:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo procesar la imagen del logo'
      });
      this.guardando.set(false);
    }
  }
}
