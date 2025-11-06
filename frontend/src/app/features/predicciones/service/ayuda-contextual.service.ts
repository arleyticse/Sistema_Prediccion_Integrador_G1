import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';

/**
 * Interfaz para la estructura del JSON de ayuda contextual
 */
export interface AyudaContextual {
  wizard: any;
  campos: any;
  algoritmos: any;
  parametros: any;
  estados: any;
  recomendacion: any;
  validacion: any;
  glosario: any;
  ayudaGeneral: any;
}

/**
 * Servicio para gestionar las ayudas contextuales del módulo de predicciones
 * Lee un archivo JSON con descripciones, tooltips y ejemplos para mejorar la UX
 * 
 * @ejemplo
 * ```typescript
 * // En un componente
 * tooltipHorizonte = this.ayudaService.obtenerTooltipCampo('horizontePrediccion');
 * ayudaAlpha = this.ayudaService.obtenerAyudaParametro('simpleExponentialSmoothingAlgorithm', 'alpha');
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class AyudaContextualService {
  private readonly RUTA_JSON = 'assets/ayuda/predicciones-ayuda.json';
  
  // Signal que contiene todo el JSON cargado
  private ayudaDatos = signal<AyudaContextual | null>(null);
  
  // Flag para saber si ya se cargó el JSON
  private cargado = signal<boolean>(false);

  constructor(private http: HttpClient) {
    // Cargar el JSON al inicializar el servicio
    this.cargarAyuda();
  }

  /**
   * Carga el archivo JSON de ayuda
   */
  private cargarAyuda(): void {
    this.http.get<AyudaContextual>(this.RUTA_JSON)
      .subscribe({
        next: (datos) => {
          this.ayudaDatos.set(datos);
          this.cargado.set(true);
          console.log('Ayuda contextual cargada correctamente', datos);
        },
        error: (error) => {
          console.error('Error al cargar ayuda contextual:', error);
          console.error('Ruta intentada:', this.RUTA_JSON);
          this.cargado.set(false);
        }
      });
  }

  /**
   * Obtiene el tooltip de un campo específico
   * @param campo Nombre del campo (ej: 'producto', 'horizontePrediccion', 'modoAutomatico')
   * @returns Texto del tooltip o string vacío si no existe
   */
  obtenerTooltipCampo(campo: string): string {
    const datos = this.ayudaDatos();
    if (!datos) {
      console.warn(' Datos de ayuda aún no cargados para campo:', campo);
      return 'Cargando ayuda...';
    }
    if (!datos.campos || !datos.campos[campo]) {
      console.warn(' No existe ayuda para el campo:', campo);
      return '';
    }
    return datos.campos[campo].tooltip || '';
  }

  /**
   * Obtiene la ayuda completa de un campo (incluye tooltip, ayuda, ejemplo, recomendación)
   * @param campo Nombre del campo
   * @returns Objeto con toda la información del campo
   */
  obtenerAyudaCampo(campo: string): any {
    const datos = this.ayudaDatos();
    if (!datos || !datos.campos[campo]) return null;
    return datos.campos[campo];
  }

  /**
   * Obtiene la descripción completa de un algoritmo
   * @param codigoAlgoritmo Código del algoritmo (ej: 'simpleMovingAverageAlgorithm')
   * @returns Objeto con nombre, descripción, cuándo usar, ventajas, desventajas, ejemplo
   */
  obtenerAyudaAlgoritmo(codigoAlgoritmo: string): any {
    const datos = this.ayudaDatos();
    if (!datos || !datos.algoritmos[codigoAlgoritmo]) return null;
    return datos.algoritmos[codigoAlgoritmo];
  }

  /**
   * Obtiene el tooltip de un parámetro específico de un algoritmo
   * @param codigoAlgoritmo Código del algoritmo
   * @param parametro Nombre del parámetro (ej: 'alpha', 'ventana', 'periodo')
   * @returns Texto del tooltip
   */
  obtenerTooltipParametro(codigoAlgoritmo: string, parametro: string): string {
    const datos = this.ayudaDatos();
    if (!datos) {
      console.warn(' Datos de ayuda aún no cargados para parámetro:', codigoAlgoritmo, parametro);
      return 'Cargando ayuda...';
    }
    if (!datos.parametros || !datos.parametros[codigoAlgoritmo]) {
      console.warn(' No existe ayuda para el algoritmo:', codigoAlgoritmo);
      return '';
    }
    const paramInfo = datos.parametros[codigoAlgoritmo][parametro];
    return paramInfo?.tooltip || '';
  }

  /**
   * Obtiene la ayuda completa de un parámetro
   * @param codigoAlgoritmo Código del algoritmo
   * @param parametro Nombre del parámetro
   * @returns Objeto con label, tooltip, ayuda, recomendación, impacto, ejemplo
   */
  obtenerAyudaParametro(codigoAlgoritmo: string, parametro: string): any {
    const datos = this.ayudaDatos();
    if (!datos || !datos.parametros[codigoAlgoritmo]) return null;
    return datos.parametros[codigoAlgoritmo][parametro] || null;
  }

  /**
   * Obtiene el mensaje de un estado específico
   * @param estado 'cargando' | 'exito' | 'error.sinDatos' | 'error.sinProducto' | 'error.sinAlgoritmo'
   * @returns Mensaje del estado
   */
  obtenerMensajeEstado(estado: string): string {
    const datos = this.ayudaDatos();
    if (!datos) return '';
    
    // Navegación por clave anidada (ej: 'error.sinDatos')
    const partes = estado.split('.');
    let resultado: any = datos.estados;
    
    for (const parte of partes) {
      if (resultado && typeof resultado === 'object') {
        resultado = resultado[parte];
      } else {
        return '';
      }
    }
    
    return typeof resultado === 'string' ? resultado : '';
  }

  /**
   * Obtiene información del glosario
   * @param termino 'estacionalidad' | 'tendencia' | 'volatilidad' | 'suavizado' | 'autocorrelacion'
   * @returns Objeto con término, definición y ejemplo
   */
  obtenerGlosario(termino: string): any {
    const datos = this.ayudaDatos();
    if (!datos || !datos.glosario[termino]) return null;
    return datos.glosario[termino];
  }

  /**
   * Obtiene ayuda general sobre predicciones
   * @param tema 'queEsUnaPrediccion' | 'comoInterpretarResultados'
   * @returns Objeto con título, contenido y lista de beneficios/recomendaciones
   */
  obtenerAyudaGeneral(tema: string): any {
    const datos = this.ayudaDatos();
    if (!datos || !datos.ayudaGeneral[tema]) return null;
    return datos.ayudaGeneral[tema];
  }

  /**
   * Obtiene el mensaje de validación para un parámetro
   * @param clave 'horizonteMin' | 'horizonteMax' | 'alphaRango' | etc.
   * @returns Mensaje de validación
   */
  obtenerMensajeValidacion(clave: string): string {
    const datos = this.ayudaDatos();
    if (!datos || !datos.validacion[clave]) return '';
    return datos.validacion[clave];
  }

  /**
   * Obtiene información sobre la recomendación automática
   * @param seccion 'confianza' | 'justificacion' | 'parametros'
   * @returns Objeto con información de la sección
   */
  obtenerAyudaRecomendacion(seccion: string): any {
    const datos = this.ayudaDatos();
    if (!datos || !datos.recomendacion[seccion]) return null;
    return datos.recomendacion[seccion];
  }

  /**
   * Obtiene el tooltip para un paso del wizard
   * @param paso Número del paso (1, 2 o 3)
   * @returns Descripción del paso
   */
  obtenerTooltipPaso(paso: number): string {
    const datos = this.ayudaDatos();
    if (!datos || !datos.wizard.pasos[paso.toString()]) return '';
    return datos.wizard.pasos[paso.toString()].descripcion || '';
  }

  /**
   * Verifica si el servicio ya cargó los datos
   */
  estaCargado(): boolean {
    return this.cargado();
  }

  /**
   * Obtiene todos los datos de ayuda (útil para debugging)
   */
  obtenerTodosLosDatos(): AyudaContextual | null {
    return this.ayudaDatos();
  }
}
