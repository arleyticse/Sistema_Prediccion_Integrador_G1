# 🚀 Generador de Datos de Prueba - Sistema de Predicción

Herramienta Python para generar datos CSV realistas de un minimarket/tienda de productos cotidianos, compatible con la base de datos existente.

## 📋 Descripción

Este script genera archivos CSV con datos de prueba para:
- **Productos**: Artículos cotidianos de minimarket (alimentos, bebidas, higiene, limpieza)
- **Inventario**: Niveles de stock realistas con ubicaciones en almacén
- **Movimientos (Kardex)**: Compras y ventas históricas
- **Proveedores**: Distribuidores de alimentos y productos
- **Estacionalidad**: Patrones de demanda mensuales realistas

## 🎯 Características

✅ **Datos Realistas**
- Productos cotidianos de minimarket (arroz, leche, pan, agua, etc.)
- Precios y costos coherentes con el mundo real
- Patrones de estacionalidad basados en comportamiento real

✅ **Compatible con BD Actual**
- Respeta las categorías existentes (Alimentos Perecibles, Bebidas, Higiene, etc.)
- Usa unidades de medida ya configuradas (Kg, Litro, Unidad, etc.)
- No genera duplicados con datos existentes

✅ **Variación Aleatoria**
- Cada ejecución genera datos diferentes
- Cantidades y fechas variadas
- Múltiples proveedores y ubicaciones

## 📦 Requisitos

- Python 3.7+
- Ninguna dependencia externa (solo librerías estándar)

## 🚀 Uso

### Opción 1: Ejecución Directa

```bash
cd Scripts
python generar_datos_prueba.py
```

### Opción 2: Con Parámetros (Futuro)

```bash
python generar_datos_prueba.py --productos 25 --movimientos 50
```

## 📂 Estructura de Salida

```
Scripts/
├── datos_prueba/
│   ├── productos_test.csv
│   ├── inventario_test.csv
│   ├── kardex_test.csv
│   ├── proveedores_test.csv
│   └── estacionalidad_test.csv
└── generar_datos_prueba.py
```

## 📊 Contenido de Archivos

### 1. productos_test.csv (20 productos)
Productos cotidianos de minimarket:
- Arroz, Pan, Leche, Queso, Huevos, Pollo
- Vegetales: Tomate, Cebolla, Papa
- Frutas: Plátano, Naranja, Manzana
- Alimentos secos: Fideos, Sal, Azúcar
- Bebidas: Gaseosas, Agua, Jugo
- Higiene: Jabón, Shampoo, Pasta dental
- Limpieza: Papel higiénico, Detergente, Cloro

### 2. inventario_test.csv (20 inventarios)
Stock realista con:
- Stock mínimo, máximo y punto de reorden
- Stock reservado y en tránsito
- Ubicaciones en almacén (A-01-01, B-02-03, etc.)
- Estados: NORMAL, BAJO, CRÍTICO
- Rango: 50-300 unidades por producto

### 3. kardex_test.csv (30 movimientos)
Movimientos históricos:
- Entradas por compra (ENTRADA_COMPRA)
- Salidas por venta (SALIDA_VENTA)
- Ajustes de inventario (ENTRADA_AJUSTE)
- Documentos: Facturas, Guías, Boletas
- Fechas: Últimos 30 días
- Proveedores y lotes asociados

### 4. proveedores_test.csv (8 proveedores)
Distribuidores:
- Razón social y nombre comercial
- RUC/NIT, teléfono, email
- Dirección, ciudad, país
- Tiempo de entrega: 1-7 días
- Calificación: 7.5-9.5
- Días de crédito: 7-60 días

### 5. estacionalidad_test.csv
Patrones mensuales de demanda:
- 5 productos de prueba x 12 meses
- Factores estacionales variables
- Máximos en Nov-Dic (Navidad) y Abr-May (Semana Santa)
- Mínimos en Jul-Ago (Invierno)
- Datos realistas por temporada

## 🔄 Ciclo de Pruebas Recomendado

### Paso 1: Generar Datos
```bash
python generar_datos_prueba.py
```

### Paso 2: Importar en Frontend
1. Abre http://localhost:4200 en el navegador
2. Ve a **Productos** → Clic en "Importar CSV"
3. Descarga la plantilla o carga `productos_test.csv`
4. Valida y luego importa
5. Repite para: Inventario, Movimientos, Proveedores, Estacionalidad

### Paso 3: Validar Datos
- Verifica que los registros aparezcan en las tablas
- Comprueba que los costos son razonables
- Confirma que no hay duplicados

### Paso 4: Pruebas Adicionales
- Genera nuevos datos con más variación
- Prueba con diferentes cantidades de registros
- Simula errores para verificar el manejo

## 💾 Datos de Ejemplo

### Productos
| Nombre | Costo | Categoría | UM |
|--------|-------|-----------|-----|
| Arroz Extra Superior 10kg | $18.50 | Alimentos Perecibles | Kg |
| Leche Fresca 1lt | $2.50 | Lácteos y Derivados | Litro |
| Agua Mineral 625ml | $0.80 | Bebidas | Botella |
| Papel Higiénico x12 | $4.50 | Limpieza del Hogar | Paquete |

### Proveedores Incluidos
- Distribuidora de Alimentos SA
- Productora de Aceites EIRL
- Panadería Central S.A.C.
- Lácteos del Sur LTDA
- Distribuidora Frutas y Verduras
- Avícola Premium EIRL
- Molino Central S.A.
- Quesería El Molino EIRL

## 🔍 Personalización

Para modificar el generador:

```python
# Agregar más productos
PRODUCTOS_MINIMARKET.extend([
    ("Nuevo Producto", 2, 4),  # (nombre, id_categoria, id_um)
])

# Cambiar cantidad de datos generados
generar_productos_csv(num_productos=50)
generar_kardex_csv(num_movimientos=100)

# Modificar rango de precios
costo = round(random.uniform(0.5, 100.0), 2)
```

## 🐛 Solución de Problemas

### Error: "ModuleNotFoundError: No module named 'csv'"
Python 3.7+ incluye `csv` por defecto. Verifica que usas la versión correcta.

### Archivos vacíos o incompletos
- Verifica permisos de escritura en la carpeta `Scripts`
- Comprueba que el directorio `datos_prueba` fue creado
- Revisa la salida de error del script

### Datos no se importan
- Verifica que los encabezados CSV coincidan con las plantillas
- Comprueba que las categorías (id_categoria) existan en la BD
- Valida que los RUC de proveedores sean únicos

## 📝 Notas Importantes

⚠️ **El script NO modifica la BD directamente**
- Solo genera archivos CSV
- La importación debe hacerse por el frontend

⚠️ **Datos de Prueba**
- Todos los RUC/NIT son ficticios
- Los emails y teléfonos son simulados
- Las fechas son relativas (últimos 30 días)

✅ **Seguro para Ejecutar Múltiples Veces**
- Sobrescribe los CSV anteriores
- No afecta la BD
- Puedes generar nuevos datos en cualquier momento

## 🎓 Ejemplo de Uso Completo

```bash
# 1. Generar datos
cd c:/Users/Admin/Desktop/Sistema_Prediccion_Unificado/Scripts
python generar_datos_prueba.py

# 2. Ir a la carpeta de datos generados
cd datos_prueba
dir  # Ver los archivos creados

# 3. Abrir en Excel o editor de texto para revisar
start productos_test.csv

# 4. Usar en la aplicación Angular
# Ir a http://localhost:4200/productos
# Click en "Importar CSV"
# Seleccionar c:/Users/Admin/Desktop/Sistema_Prediccion_Unificado/Scripts/datos_prueba/productos_test.csv
# Validar e importar
```

## 📞 Soporte

Si encuentras problemas:
1. Verifica que Python 3.7+ esté instalado: `python --version`
2. Revisa que el script tenga permisos de ejecución
3. Comprueba los logs del frontend para errores de validación
4. Consulta los README en plantillas-importacion del backend

## 📄 Licencia

Parte del Sistema de Predicción - Uso Interno

---

**Versión**: 1.0
**Última actualización**: Enero 2025
**Autor**: Sistema de Predicción Team
