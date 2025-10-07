# 📥 Templates de Importación CSV

Este directorio contiene plantillas CSV de ejemplo para importar datos al sistema.

## 📋 Plantillas Disponibles

### 1. **template_inventario.csv**
Formato para importar inventario inicial o actualizar stock.

**Campos:**
- `codigo_producto`: Código único del producto (debe existir en tabla productos)
- `stock_disponible`: Stock disponible para venta
- `stock_reservado`: Stock reservado/comprometido
- `stock_minimo`: Umbral mínimo de stock
- `stock_maximo`: Umbral máximo de stock
- `punto_reorden`: Nivel que activa reabastecimiento
- `ubicacion`: Ubicación física en almacén
- `lote`: Número de lote actual

**Ejemplo de uso:**
```csv
codigo_producto,stock_disponible,stock_reservado,stock_minimo,stock_maximo,punto_reorden,ubicacion,lote
PROD001,500,50,100,1000,200,A-01-01,LOTE2024001
```

---

### 2. **template_kardex.csv**
Formato para importar movimientos de kardex/cardex.

**Campos:**
- `codigo_producto`: Código único del producto
- `fecha_movimiento`: Fecha y hora (formato: YYYY-MM-DD HH:MM:SS)
- `tipo_movimiento`: Tipo de movimiento (ver tipos válidos abajo)
- `cantidad_entrada`: Cantidad de entrada (0 si es salida)
- `cantidad_salida`: Cantidad de salida (0 si es entrada)
- `costo_unitario`: Costo por unidad
- `proveedor`: Nombre del proveedor (opcional)
- `numero_documento`: Número de factura/guía (opcional)
- `observaciones`: Notas adicionales (opcional)

**Tipos de Movimiento Válidos:**
- ENTRADA_COMPRA
- ENTRADA_DEVOLUCION
- ENTRADA_AJUSTE
- ENTRADA_TRANSFERENCIA
- ENTRADA_INICIAL
- SALIDA_VENTA
- SALIDA_DEVOLUCION
- SALIDA_AJUSTE
- SALIDA_MERMA
- SALIDA_VENCIMIENTO
- AJUSTE_POSITIVO
- AJUSTE_NEGATIVO

**Ejemplo de uso:**
```csv
codigo_producto,fecha_movimiento,tipo_movimiento,cantidad_entrada,cantidad_salida,costo_unitario,proveedor,numero_documento,observaciones
PROD001,2024-10-01 10:00:00,ENTRADA_COMPRA,100,0,15.50,Distribuidora ABC,F001-001,Compra regular
```

---

### 3. **template_proveedores.csv**
Formato para importar base de datos de proveedores.

**Campos:**
- `razon_social`: Razón social del proveedor (requerido)
- `nombre_comercial`: Nombre comercial
- `ruc_nit`: RUC o NIT (único)
- `telefono`: Teléfono de contacto
- `email`: Email de contacto
- `direccion`: Dirección física
- `ciudad`: Ciudad
- `tiempo_entrega_dias`: Lead time en días
- `dias_credito`: Días de crédito otorgados
- `calificacion`: Calificación del 0 al 5

**Ejemplo de uso:**
```csv
razon_social,nombre_comercial,ruc_nit,telefono,email,direccion,ciudad,tiempo_entrega_dias,dias_credito,calificacion
Distribuidora ABC S.A.,Distrib ABC,1234567890,591-2-2345678,ventas@abc.com,Av. Principal 123,La Paz,3,30,4.5
```

---

### 4. **template_estacionalidad.csv**
Formato para importar patrones de estacionalidad por producto.

**Campos:**
- `codigo_producto`: Código único del producto
- `mes`: Mes del año (1-12)
- `factor_estacional`: Factor multiplicador (1.0 = normal, 1.5 = 150% de demanda)
- `demanda_promedio`: Demanda promedio histórica
- `demanda_maxima`: Demanda máxima registrada
- `demanda_minima`: Demanda mínima registrada
- `descripcion_temporada`: Descripción de la temporada (opcional)

**Ejemplo de uso:**
```csv
codigo_producto,mes,factor_estacional,demanda_promedio,demanda_maxima,demanda_minima,descripcion_temporada
PROD001,12,1.50,300,350,280,Temporada navideña
```

---

## 🔧 Instrucciones de Uso

### **Opción 1: API REST**
```bash
# Importar inventario
curl -X POST http://localhost:8080/api/importacion/inventario \
  -F "file=@template_inventario.csv" \
  -F "usuarioId=1"

# Importar kardex
curl -X POST http://localhost:8080/api/importacion/kardex \
  -F "file=@template_kardex.csv" \
  -F "usuarioId=1"
```

### **Opción 2: Desde la Aplicación**
1. Ir al módulo de Importación
2. Seleccionar el tipo de datos a importar
3. Cargar el archivo CSV
4. Validar y confirmar importación

---

## ✅ Validaciones

El sistema valida automáticamente:
- ✅ Formato del archivo (CSV válido)
- ✅ Encabezados correctos
- ✅ Tipos de datos apropiados
- ✅ Referencias a productos existentes
- ✅ Valores dentro de rangos permitidos
- ✅ Fechas en formato correcto
- ✅ Enumeraciones válidas

---

## ⚠️ Consideraciones Importantes

1. **Encoding**: Usar UTF-8 para caracteres especiales
2. **Separador**: Usar coma (,) como separador
3. **Fechas**: Formato YYYY-MM-DD HH:MM:SS
4. **Decimales**: Usar punto (.) como separador decimal
5. **Campos vacíos**: Dejar vacío si es opcional, no usar "null"
6. **Tamaño**: Máximo 10 MB por archivo
7. **Registros**: Validar antes de importar grandes volúmenes

---

## 📊 Proceso de Importación

```
1. Validar formato → 2. Validar datos → 3. Procesar registros → 4. Generar reporte
        ↓                    ↓                   ↓                      ↓
   ¿CSV válido?      ¿Datos válidos?     Insertar en BD        Éxito/Errores
```

---

## 🐛 Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| "Producto no encontrado" | Código de producto no existe | Verificar que el producto esté registrado |
| "Formato de fecha inválido" | Fecha en formato incorrecto | Usar formato YYYY-MM-DD HH:MM:SS |
| "Tipo de movimiento inválido" | Enum no reconocido | Usar valores exactos de la lista |
| "Campo requerido vacío" | Falta campo obligatorio | Completar todos los campos marcados |
| "Valor fuera de rango" | Número negativo o muy grande | Verificar rangos permitidos |

---

## 📞 Soporte

Para problemas con la importación:
1. Verificar el formato del CSV contra la plantilla
2. Revisar el log de errores de la importación
3. Consultar la documentación en `GUIA-IMPLEMENTACION.md`
4. Revisar ejemplos en este directorio

---

## 🎓 Tips para Importaciones Exitosas

1. **Empezar con pocos registros** para validar el formato
2. **Usar los templates** como base, no crear desde cero
3. **Validar en Excel** antes de importar
4. **Hacer backup** antes de importaciones grandes
5. **Importar en orden**: Proveedores → Productos → Inventario → Kardex
6. **Revisar el reporte** después de cada importación
7. **Corregir errores** antes de reimportar

---

**Última actualización:** Octubre 2025
