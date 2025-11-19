import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as pdfMake from 'pdfmake/build/pdfmake';
import * as pdfFonts from 'pdfmake/build/vfs_fonts';
import { TDocumentDefinitions } from 'pdfmake/interfaces';

// Configurar fuentes usando el método correcto
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
}

@Injectable({
  providedIn: 'root'
})
export class OrdenPdfService {
  
  private readonly API_URL = 'http://localhost:8080/api/ordenes';

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
        // Información de la empresa
        {
          text: 'EMPRESA EMISORA',
          style: 'sectionHeader'
        },
        {
          columns: [
            {
              width: '50%',
              stack: [
                { text: datos.empresa.razonSocial, style: 'bold' },
                { text: `RUC: ${datos.empresa.ruc}` },
                { text: datos.empresa.direccion },
                { text: `${datos.empresa.ciudad || ''} - ${datos.empresa.pais || ''}` }
              ]
            },
            {
              width: '50%',
              stack: [
                { text: `Tel: ${datos.empresa.telefono}` },
                { text: datos.empresa.email },
                { text: `Fecha: ${this.formatearFecha(datos.fechaOrden)}` },
                { text: `Estado: ${datos.estadoOrden}`, style: 'bold' }
              ]
            }
          ],
          margin: [0, 5, 0, 15]
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
                { text: `$${detalle.precioUnitario.toFixed(2)}`, alignment: 'right' as const },
                { text: `$${detalle.subtotal.toFixed(2)}`, alignment: 'right' as const }
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
                    { text: `$${datos.subtotal.toFixed(2)}`, alignment: 'right', width: '50%' }
                  ],
                  margin: [0, 2]
                },
                {
                  columns: [
                    { text: 'Impuestos:', alignment: 'right', width: '50%' },
                    { text: `$${datos.impuestos.toFixed(2)}`, alignment: 'right', width: '50%' }
                  ],
                  margin: [0, 2]
                },
                {
                  canvas: [{ type: 'line', x1: 0, y1: 5, x2: 200, y2: 5, lineWidth: 1 }]
                },
                {
                  columns: [
                    { text: 'TOTAL:', alignment: 'right', style: 'bold', width: '50%' },
                    { text: `$${datos.totalOrden.toFixed(2)}`, alignment: 'right', style: 'bold', width: '50%' }
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
}
