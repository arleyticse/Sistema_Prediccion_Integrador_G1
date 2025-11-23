import { Injectable, inject } from '@angular/core';
import * as pdfMake from 'pdfmake/build/pdfmake';
import * as pdfFonts from 'pdfmake/build/vfs_fonts';
import { TDocumentDefinitions } from 'pdfmake/interfaces';
import { ReportePrediccionDTO, ProductoConPrediccion, PrediccionDetalle } from '../models/ReportePrediccion';
import { ReporteInventarioDTO, ProductoCritico } from '../models/ReporteInventario';
import { CurrencyService } from '../../../shared/services/currency.service';

(pdfMake as any).addVirtualFileSystem(pdfFonts);

@Injectable({
  providedIn: 'root'
})
export class ReportePdfService {
  private currencyService = inject(CurrencyService);

  generarPDFPredicciones(datos: ReportePrediccionDTO): void {
    const docDefinition = this.construirDocumentoPredicciones(datos);
    pdfMake.createPdf(docDefinition).download(`reporte-predicciones-${this.formatearFechaArchivo(new Date())}.pdf`);
  }

  abrirPDFPredicciones(datos: ReportePrediccionDTO): void {
    const docDefinition = this.construirDocumentoPredicciones(datos);
    pdfMake.createPdf(docDefinition).open();
  }

  generarPDFInventario(datos: ReporteInventarioDTO): void {
    const docDefinition = this.construirDocumentoInventario(datos);
    pdfMake.createPdf(docDefinition).download(`reporte-inventario-${this.formatearFechaArchivo(new Date())}.pdf`);
  }

  abrirPDFInventario(datos: ReporteInventarioDTO): void {
    const docDefinition = this.construirDocumentoInventario(datos);
    pdfMake.createPdf(docDefinition).open();
  }

  private construirDocumentoPredicciones(datos: ReportePrediccionDTO): TDocumentDefinitions {
    const fechaGeneracion = new Date();
    
    return {
      pageSize: 'A4',
      pageOrientation: 'portrait',
      pageMargins: [40, 60, 40, 60],
      
      header: (currentPage, pageCount) => ({
        text: `Página ${currentPage} de ${pageCount}`,
        alignment: 'right',
        margin: [0, 20, 40, 0],
        fontSize: 9,
        color: '#666'
      }),
      
      footer: (currentPage, pageCount) => ({
        text: `Generado el ${this.formatearFechaLarga(fechaGeneracion)}`,
        alignment: 'center',
        margin: [0, 10, 0, 0],
        fontSize: 9,
        color: '#666'
      }),
      
      content: [
        {
          text: 'REPORTE DE PREDICCIONES',
          style: 'header',
          alignment: 'center',
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Resumen General',
          style: 'subheader',
          margin: [0, 10, 0, 10]
        },
        
        {
          columns: [
            { text: `Total Predicciones: ${datos.resumenGeneral.totalPredicciones}`, width: '*' },
            { text: `Éxito: ${datos.resumenGeneral.porcentajeExito.toFixed(2)}%`, width: '*' }
          ],
          margin: [0, 0, 0, 10]
        },
        
        {
          columns: [
            { text: `Excelentes: ${datos.resumenGeneral.prediccionesExcelentes}`, style: 'successText' },
            { text: `Buenas: ${datos.resumenGeneral.prediccionesBuenas}`, style: 'infoText' },
            { text: `Regulares: ${datos.resumenGeneral.prediccionesRegulares}`, style: 'warningText' },
            { text: `Malas: ${datos.resumenGeneral.prediccionesMalas}`, style: 'dangerText' }
          ],
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Estadísticas',
          style: 'subheader',
          margin: [0, 10, 0, 10]
        },
        
        {
          columns: [
            { text: `MAPE Promedio: ${datos.estadisticas.mapePromedio.toFixed(2)}%`, width: '*' },
            { text: `Algoritmo más usado: ${datos.estadisticas.algoritmoMasUsado}`, width: '*' }
          ],
          margin: [0, 0, 0, 10]
        },
        
        {
          text: `Demanda Total Predicha: ${datos.estadisticas.demandaTotalPredicha.toFixed(2)}`,
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Top 10 Productos con más Predicciones',
          style: 'subheader',
          margin: [0, 10, 0, 10]
        },
        
        {
          table: {
            headerRows: 1,
            widths: ['*', 'auto', 'auto', 'auto', '*'],
            body: [
              [
                { text: 'Producto', style: 'tableHeader' },
                { text: 'Código', style: 'tableHeader' },
                { text: 'Predicciones', style: 'tableHeader' },
                { text: 'MAPE Prom.', style: 'tableHeader' },
                { text: 'Categoría', style: 'tableHeader' }
              ],
              ...datos.topProductos.map((p: ProductoConPrediccion) => [
                p.nombreProducto,
                p.codigoProducto,
                p.cantidadPredicciones.toString(),
                `${p.mapePromedio.toFixed(2)}%`,
                p.categoria
              ])
            ]
          },
          layout: {
            fillColor: (rowIndex: number) => rowIndex === 0 ? '#2196F3' : rowIndex % 2 === 0 ? '#f5f5f5' : null,
            hLineWidth: () => 0.5,
            vLineWidth: () => 0.5,
            hLineColor: () => '#ddd',
            vLineColor: () => '#ddd'
          },
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Detalle de Predicciones (Últimas 30)',
          style: 'subheader',
          margin: [0, 10, 0, 10],
          pageBreak: 'before'
        },
        
        {
          table: {
            headerRows: 1,
            widths: ['auto', '*', 'auto', 'auto', 'auto', 'auto'],
            body: [
              [
                { text: 'Fecha', style: 'tableHeader' },
                { text: 'Producto', style: 'tableHeader' },
                { text: 'Algoritmo', style: 'tableHeader' },
                { text: 'Demanda', style: 'tableHeader' },
                { text: 'MAPE', style: 'tableHeader' },
                { text: 'Precisión', style: 'tableHeader' }
              ],
              ...datos.predicciones.slice(0, 30).map((p: PrediccionDetalle) => [
                this.formatearFechaLarga(p.fechaEjecucion),
                p.nombreProducto || 'N/A',
                p.algoritmoUsado || 'N/A',
                p.demandaPredichaTotal?.toFixed(2) || '0',
                p.mape?.toFixed(2) + '%' || 'N/A',
                {
                  text: p.nivelPrecision || 'N/A',
                  color: this.obtenerColorPrecision(p.nivelPrecision)
                }
              ])
            ]
          },
          layout: {
            fillColor: (rowIndex: number) => rowIndex === 0 ? '#2196F3' : rowIndex % 2 === 0 ? '#f5f5f5' : null,
            hLineWidth: () => 0.5,
            vLineWidth: () => 0.5,
            hLineColor: () => '#ddd',
            vLineColor: () => '#ddd'
          }
        }
      ],
      
      styles: {
        header: {
          fontSize: 22,
          bold: true,
          color: '#2196F3'
        },
        subheader: {
          fontSize: 16,
          bold: true,
          color: '#424242'
        },
        tableHeader: {
          bold: true,
          fontSize: 11,
          color: 'white',
          fillColor: '#2196F3'
        },
        successText: {
          color: '#4CAF50'
        },
        infoText: {
          color: '#2196F3'
        },
        warningText: {
          color: '#FF9800'
        },
        dangerText: {
          color: '#F44336'
        }
      }
    };
  }

  private construirDocumentoInventario(datos: ReporteInventarioDTO): TDocumentDefinitions {
    const fechaGeneracion = new Date();
    
    return {
      pageSize: 'A4',
      pageOrientation: 'landscape',
      pageMargins: [40, 60, 40, 60],
      
      header: (currentPage, pageCount) => ({
        text: `Página ${currentPage} de ${pageCount}`,
        alignment: 'right',
        margin: [0, 20, 40, 0],
        fontSize: 9,
        color: '#666'
      }),
      
      footer: (currentPage, pageCount) => ({
        text: `Generado el ${this.formatearFechaLarga(fechaGeneracion)}`,
        alignment: 'center',
        margin: [0, 10, 0, 0],
        fontSize: 9,
        color: '#666'
      }),
      
      content: [
        {
          text: 'REPORTE DE INVENTARIO',
          style: 'header',
          alignment: 'center',
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Resumen General',
          style: 'subheader',
          margin: [0, 10, 0, 10]
        },
        
        {
          columns: [
            { text: `Total Productos: ${datos.resumenGeneral.totalProductos}`, width: '*' },
            { text: `En Stock: ${datos.resumenGeneral.productosConStock}`, width: '*' },
            { text: `Sin Stock: ${datos.resumenGeneral.productosSinStock}`, width: '*' }
          ],
          margin: [0, 0, 0, 10]
        },
        
        {
          columns: [
            { text: `Bajo Mínimo: ${datos.resumenGeneral.productosBajoMinimo}`, style: 'warningText' },
            { text: `Requieren Reorden: ${datos.resumenGeneral.productosEnReorden}`, style: 'dangerText' },
            { text: `Obsoletos: ${datos.resumenGeneral.productosObsoletos}`, style: 'infoText' }
          ],
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Estadísticas',
          style: 'subheader',
          margin: [0, 10, 0, 10]
        },
        
        {
          columns: [
            { text: `Stock Total: ${datos.estadisticas.stockTotalGeneral.toFixed(2)}`, width: '*' },
            { text: `Días Promedio Sin Venta: ${datos.estadisticas.diasPromedioSinVenta.toFixed(2)}`, width: '*' }
          ],
          margin: [0, 0, 0, 10]
        },
        
        {
          text: `Stock Disponible Total: ${datos.estadisticas.stockDisponibleTotal}`,  
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Valoración de Inventario',
          style: 'subheader',
          margin: [0, 10, 0, 10]
        },
        
        {
          columns: [
            { text: `Valor Total: ${this.currencyService.format(datos.valoracion.valorTotalInventario)}`, width: '*' },
            { text: `Valor Disponible: ${this.currencyService.format(datos.valoracion.valorStockDisponible)}`, width: '*' }
          ],
          margin: [0, 0, 0, 10]
        },
        
        {
          columns: [
            { text: `Valor Reservado: ${this.currencyService.format(datos.valoracion.valorStockReservado)}`, width: '*' },
            { text: `Valor En Tránsito: ${this.currencyService.format(datos.valoracion.valorStockEnTransito)}`, style: 'warningText' }
          ],
          margin: [0, 0, 0, 20]
        },
        
        {
          text: 'Productos Críticos (Top 15)',
          style: 'subheader',
          margin: [0, 10, 0, 10],
          pageBreak: 'before'
        },
        
        {
          table: {
            headerRows: 1,
            widths: ['*', 'auto', 'auto', 'auto', 'auto', 'auto', '*'],
            body: [
              [
                { text: 'Producto', style: 'tableHeader' },
                { text: 'Código', style: 'tableHeader' },
                { text: 'Stock', style: 'tableHeader' },
                { text: 'Mínimo', style: 'tableHeader' },
                { text: 'Días Sin Venta', style: 'tableHeader' },
                { text: 'Criticidad', style: 'tableHeader' },
                { text: 'Razón', style: 'tableHeader' }
              ],
              ...datos.productosCriticos.slice(0, 15).map((p: ProductoCritico) => [
                p.nombreProducto || 'N/A',
                p.codigoProducto || 'N/A',
                p.stockDisponible?.toString() || '0',
                p.stockMinimo?.toString() || '0',
                (p.diasSinVenta?.toString() || 'N/A') + ' días',
                {
                  text: p.nivelCriticidad || 'N/A',
                  color: this.obtenerColorCriticidad(p.nivelCriticidad)
                },
                p.razon || 'N/A'
              ])
            ]
          },
          layout: {
            fillColor: (rowIndex: number) => rowIndex === 0 ? '#2196F3' : rowIndex % 2 === 0 ? '#f5f5f5' : null,
            hLineWidth: () => 0.5,
            vLineWidth: () => 0.5,
            hLineColor: () => '#ddd',
            vLineColor: () => '#ddd'
          }
        }
      ],
      
      styles: {
        header: {
          fontSize: 22,
          bold: true,
          color: '#2196F3'
        },
        subheader: {
          fontSize: 16,
          bold: true,
          color: '#424242'
        },
        tableHeader: {
          bold: true,
          fontSize: 11,
          color: 'white',
          fillColor: '#2196F3'
        },
        successText: {
          color: '#4CAF50'
        },
        infoText: {
          color: '#2196F3'
        },
        warningText: {
          color: '#FF9800'
        },
        dangerText: {
          color: '#F44336'
        }
      }
    };
  }

  private formatearFechaArchivo(fecha: Date): string {
    const dia = fecha.getDate().toString().padStart(2, '0');
    const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
    const anio = fecha.getFullYear();
    return `${dia}${mes}${anio}`;
  }

  private formatearFechaLarga(fecha: Date | string): string {
    const fechaObj = typeof fecha === 'string' ? new Date(fecha) : fecha;
    return fechaObj.toLocaleDateString('es-ES', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

  private obtenerColorPrecision(nivel?: string): string {
    switch (nivel) {
      case 'Excelente': return '#4CAF50';
      case 'Buena': return '#2196F3';
      case 'Regular': return '#FF9800';
      case 'Mala': return '#F44336';
      default: return '#666';
    }
  }

  private obtenerColorCriticidad(nivel?: string): string {
    switch (nivel) {
      case 'CRITICO': return '#F44336';
      case 'ALTO': return '#FF9800';
      case 'MEDIO': return '#FFC107';
      case 'BAJO': return '#4CAF50';
      default: return '#666';
    }
  }
}
