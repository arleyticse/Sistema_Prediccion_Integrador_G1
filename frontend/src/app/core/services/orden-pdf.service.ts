import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as pdfMake from 'pdfmake/build/pdfmake';
import * as pdfFonts from 'pdfmake/build/vfs_fonts';
import { TDocumentDefinitions } from 'pdfmake/interfaces';
import { environment } from '../../environments/environment';

(pdfMake as any).addVirtualFileSystem(pdfFonts);

export interface ResumenOrdenCompraDTO {
  ordenCompraId: number;
  numeroOrden: string;
  estadoOrden: string;
  fechaOrden: string;
  fechaEntregaEsperada?: string;
  fechaEntregaReal?: string;
  empresa: DatosEmpresaDTO;
  proveedor: DatosProveedorDTO;
  detalles: DetalleProductoOrdenDTO[];
  subtotal: number;
  impuestos: number;
  descuentos?: number;
  totalOrden: number;
  generadaAutomaticamente: boolean;
  observaciones?: string;
  usuarioCreador?: string;
  fechaCreacion: string;
}

export interface DatosEmpresaDTO {
  razonSocial: string;
  nombreComercial?: string;
  ruc: string;
  direccion: string;
  ciudad?: string;
  pais?: string;
  telefono: string;
  email: string;
  sitioWeb?: string;
  logoBase64?: string;
  logoMimeType?: string;
}

export interface DatosProveedorDTO {
  razonSocial: string;
  nombreComercial?: string;
  rucNit: string;
  direccion: string;
  ciudad?: string;
  pais?: string;
  telefono: string;
  email: string;
  personaContacto?: string;
}

export interface DetalleProductoOrdenDTO {
  nombreProducto: string;
  unidadMedida: string;
  cantidadSolicitada: number;
  precioUnitario: number;
  subtotal: number;
  detalleId?: number;
  cantidadRecibida?: number;
}

@Injectable({
  providedIn: 'root'
})
export class OrdenPdfService {
  
  private readonly API_URL = `${environment.apiUrl}/ordenes`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los datos completos de una orden para PDF desde el backend
   */
  obtenerDatosOrden(ordenId: number): Observable<ResumenOrdenCompraDTO> {
    return this.http.get<ResumenOrdenCompraDTO>(`${this.API_URL}/${ordenId}/pdf-data`);
  }

  /**
   * Genera y descarga el PDF de la orden de compra
   */
  generarPDFOrdenCompra(ordenId: number): void {
    this.obtenerDatosOrden(ordenId).subscribe({
      next: (datos) => {
        const documentDefinition = this.construirDocumentDefinition(datos);
        pdfMake.createPdf(documentDefinition).download(`Orden_${datos.numeroOrden}.pdf`);
      },
      error: (error) => {
        console.error('Error al obtener datos para PDF:', error);
        alert('No se pudo generar el PDF de la orden');
      }
    });
  }

  /**
   * Abre el PDF en una nueva ventana
   */
  abrirPDFOrdenCompra(ordenId: number): void {
    this.obtenerDatosOrden(ordenId).subscribe({
      next: (datos) => {
        const documentDefinition = this.construirDocumentDefinition(datos);
        pdfMake.createPdf(documentDefinition).open();
      },
      error: (error) => {
        console.error('Error al obtener datos para PDF:', error);
        alert('No se pudo generar el PDF de la orden');
      }
    });
  }

  /**
   * Construye la definición del documento PDF
   */
  private construirDocumentDefinition(datos: ResumenOrdenCompraDTO): TDocumentDefinitions {
    // Preparar el logo si existe
    const logoDataUrl = this.construirDataUrlLogo(datos.empresa.logoBase64, datos.empresa.logoMimeType);
    
    return {
      pageSize: 'A4',
      pageMargins: [40, 60, 40, 60],
      
      header: {
        margin: [40, 20, 40, 0],
        columns: [
          {
            text: 'ORDEN DE COMPRA',
            style: 'header',
            alignment: 'left'
          },
          {
            text: datos.numeroOrden,
            style: 'headerNumero',
            alignment: 'right'
          }
        ]
      },
      
      content: [
        // Logo y encabezado de la empresa
        {
          columns: [
            ...(logoDataUrl ? [{
              image: logoDataUrl,
              width: 120,
              height: 80,
              alignment: 'right' as const
            }] : []),
            // Información de la empresa
            {
              width: logoDataUrl ? '*' : '100%',
              stack: [
                { text: 'EMPRESA EMISORA', style: 'sectionHeader' },
                { text: datos.empresa.razonSocial, style: 'bold', fontSize: 12 },
                { text: `RUC: ${datos.empresa.ruc}`, fontSize: 10 },
                { text: datos.empresa.direccion, fontSize: 9 },
                { text: `${datos.empresa.ciudad || ''} ${datos.empresa.pais || ''}`.trim(), fontSize: 9 },
                { text: `Tel: ${datos.empresa.telefono}`, fontSize: 9 },
                { text: datos.empresa.email, fontSize: 9, color: '#0066cc' }
              ],
              margin: logoDataUrl ? [15, 0, 0, 0] as [number, number, number, number] : [0, 0, 0, 0] as [number, number, number, number]
            }
          ],
          margin: [0, 0, 0, 10] as [number, number, number, number]
        },

        // Línea divisoria y datos de la orden
        {
          canvas: [{ type: 'line', x1: 0, y1: 0, x2: 515, y2: 0, lineWidth: 1, color: '#cccccc' }],
          margin: [0, 5, 0, 10] as [number, number, number, number]
        },
        {
          columns: [
            {
              width: '33%',
              stack: [
                { text: 'FECHA ORDEN', style: 'small', color: '#666666' },
                { text: this.formatearFecha(datos.fechaOrden), style: 'bold' }
              ]
            },
            {
              width: '33%',
              stack: [
                { text: 'ESTADO', style: 'small', color: '#666666' },
                { text: datos.estadoOrden, style: 'bold', color: this.obtenerColorEstado(datos.estadoOrden) }
              ]
            },
            {
              width: '34%',
              stack: [
                { text: 'ENTREGA ESPERADA', style: 'small', color: '#666666' },
                { text: datos.fechaEntregaEsperada ? this.formatearFecha(datos.fechaEntregaEsperada) : 'N/A' }
              ]
            }
          ],
          margin: [0, 0, 0, 15] as [number, number, number, number]
        },

        // Información del proveedor
        {
          text: 'PROVEEDOR',
          style: 'sectionHeader'
        },
        {
          columns: [
            {
              width: '50%',
              stack: [
                { text: datos.proveedor.razonSocial, style: 'bold' },
                { text: `RUC/NIT: ${datos.proveedor.rucNit}` },
                { text: datos.proveedor.direccion }
              ]
            },
            {
              width: '50%',
              stack: [
                { text: `Tel: ${datos.proveedor.telefono}` },
                { text: datos.proveedor.email },
                ...(datos.proveedor.personaContacto ? 
                  [{ text: `Contacto: ${datos.proveedor.personaContacto}` }] : [])
              ]
            }
          ],
          margin: [0, 5, 0, 20]
        },

        // Tabla de productos
        {
          table: {
            headerRows: 1,
            widths: ['*', 'auto', 'auto', 'auto', 'auto'],
            body: [
              // Header
              [
                { text: 'Producto', style: 'tableHeader' },
                { text: 'U.M.', style: 'tableHeader' },
                { text: 'Cantidad', style: 'tableHeader' },
                { text: 'Precio Unit.', style: 'tableHeader' },
                { text: 'Subtotal', style: 'tableHeader' }
              ],
              // Filas de productos
              ...datos.detalles.map(detalle => [
                detalle.nombreProducto,
                { text: detalle.unidadMedida, alignment: 'center' as const },
                { text: detalle.cantidadSolicitada.toString(), alignment: 'center' as const },
                { text: `S/ ${detalle.precioUnitario.toFixed(2)}`, alignment: 'right' as const },
                { text: `S/ ${detalle.subtotal.toFixed(2)}`, alignment: 'right' as const }
              ])
            ]
          },
          layout: {
            fillColor: (rowIndex: number) => {
              return rowIndex === 0 ? '#e8e8e8' : null;
            }
          },
          margin: [0, 5, 0, 20] as [number, number, number, number]
        } as any,

        // Totales
        {
          columns: [
            { width: '*', text: '' },
            {
              width: 200,
              stack: [
                {
                  columns: [
                    { text: 'Subtotal:', alignment: 'right', width: '50%' },
                    { text: `S/ ${datos.subtotal.toFixed(2)}`, alignment: 'right', width: '50%' }
                  ],
                  margin: [0, 2]
                },
                {
                  canvas: [{ type: 'line', x1: 0, y1: 5, x2: 200, y2: 5, lineWidth: 1 }]
                },
                {
                  columns: [
                    { text: 'TOTAL:', alignment: 'right', style: 'bold', width: '50%' },
                    { text: `S/ ${datos.totalOrden.toFixed(2)}`, alignment: 'right', style: 'bold', width: '50%' }
                  ],
                  margin: [0, 5]
                }
              ]
            }
          ]
        },

        // Observaciones
        ...(datos.observaciones ? [{
          stack: [
            { text: 'OBSERVACIONES', style: 'sectionHeader', margin: [0, 20, 0, 5] as [number, number, number, number] },
            { text: datos.observaciones, margin: [0, 0, 0, 10] as [number, number, number, number] }
          ]
        }] : []),

        // Firmas
        {
          columns: [
            {
              width: '45%',
              stack: [
                { text: '_______________________', alignment: 'center', margin: [0, 40, 0, 5] as [number, number, number, number] },
                { text: 'Solicitado por', alignment: 'center', style: 'small' },
                ...(datos.usuarioCreador ? 
                  [{ text: datos.usuarioCreador, alignment: 'center' as const, style: 'small' }] : [])
              ]
            },
            { width: '10%', text: '' },
            {
              width: '45%',
              stack: [
                { text: '_______________________', alignment: 'center', margin: [0, 40, 0, 5] as [number, number, number, number] },
                { text: 'Aprobado por', alignment: 'center', style: 'small' }
              ]
            }
          ],
          margin: [0, 30, 0, 0] as [number, number, number, number]
        }
      ],

      footer: (currentPage: number, pageCount: number) => {
        return {
          columns: [
            {
              text: `Generado automáticamente: ${datos.generadaAutomaticamente ? 'Sí' : 'No'}`,
              alignment: 'left',
              style: 'small'
            },
            {
              text: `Página ${currentPage} de ${pageCount}`,
              alignment: 'right',
              style: 'small'
            }
          ],
          margin: [40, 10, 40, 0]
        };
      },

      styles: {
        header: {
          fontSize: 18,
          bold: true,
          color: '#333333'
        },
        headerNumero: {
          fontSize: 16,
          bold: true,
          color: '#0066cc'
        },
        sectionHeader: {
          fontSize: 12,
          bold: true,
          color: '#0066cc',
          margin: [0, 10, 0, 5]
        },
        tableHeader: {
          bold: true,
          fontSize: 10,
          color: '#000000',
          alignment: 'center'
        },
        bold: {
          bold: true
        },
        small: {
          fontSize: 8,
          color: '#666666'
        }
      },

      defaultStyle: {
        fontSize: 10,
        lineHeight: 1.3
      }
    };
  }

  /**
   * Formatea fecha en formato DD/MM/YYYY
   */
  private formatearFecha(fecha: string): string {
    const date = new Date(fecha);
    const dia = date.getDate().toString().padStart(2, '0');
    const mes = (date.getMonth() + 1).toString().padStart(2, '0');
    const anio = date.getFullYear();
    return `${dia}/${mes}/${anio}`;
  }

  private construirDataUrlLogo(logoBase64?: string, logoMimeType?: string): string | null {
    if (!logoBase64 || !logoMimeType) return null;
    return `data:${logoMimeType};base64,${logoBase64}`;
  }

  private obtenerColorEstado(estado: string): string {
    const colores: {[key: string]: string} = {
      'PENDIENTE': '#FFA500',
      'APROBADA': '#28A745',
      'CANCELADA': '#DC3545',
      'BORRADOR': '#6C757D'
    };
    return colores[estado] || '#000000';
  }
}
