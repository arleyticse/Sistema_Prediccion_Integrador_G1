# 📘 Módulo: Gestión de Productos e Inventario  

Este módulo permite registrar, actualizar y consultar la información de los productos, controlar los niveles de stock, registrar movimientos de inventario (entradas y salidas) y generar alertas automáticas cuando los niveles mínimos o máximos se superan.  

---

## 🧩 Diccionario de Datos

### 🏷️ Tabla: `productos`

| Campo | Tipo de Dato | Descripción | Clave / Relación |
|--------|---------------|--------------|------------------|
| id_producto | integer | Identificador único del producto. | PK |
| nombre | varchar | Nombre del producto. | — |
| id_categoria | integer | Categoría a la que pertenece el producto. | FK → `categorias.id_categoria` |
| id_um | integer | Unidad de medida asociada. | FK → `unidad_medida.id_um` |
| costo_adquisicion | numeric | Costo de compra del producto. | — |
| costo_pedido | numeric | Costo asociado a cada pedido del producto. | — |
| costo_mantenimiento | numeric | Costo de almacenamiento o mantenimiento por unidad. | — |
| costo_mantenimiento_anual | numeric | Costo anual de mantenimiento. | — |
| stock_actual | integer | Cantidad actual del producto en stock. | — |
| dias_lead_time | integer | Tiempo (en días) que tarda en reabastecerse. | — |
| fecha_registro | timestamp | Fecha de creación o registro del producto. | — |

---

### 📦 Tabla: `inventario`

| Campo | Tipo de Dato | Descripción | Clave / Relación |
|--------|---------------|-------------|------------------|
| id_inventario | integer | Identificador único del registro de inventario. | PK |
| id_producto | integer | Producto asociado al inventario. | FK → `productos.id_producto` |
| stock_disponible | integer | Cantidad actual disponible. | — |
| stock_minimo | integer | Nivel mínimo de stock permitido. | — |
| stock_maximo | integer | Nivel máximo permitido. | — |
| punto_reorden | integer | Punto en el que se debe generar una nueva orden de compra. | — |
| stock_reservado | integer | Unidades reservadas (no disponibles para venta). | — |
| stock_en_transito | integer | Productos en proceso de recepción. | — |
| rotacion_inventario | numeric | Indicador de frecuencia de rotación del producto. | — |
| dias_sin_venta | integer | Días transcurridos sin movimiento de venta. | — |
| valor_total_stock | numeric | Valor total del inventario del producto. | — |
| fecha_ultima_actualizacion | timestamp | Última fecha de modificación. | — |
| fecha_ultimo_movimiento | timestamp | Fecha del último registro en el kardex. | — |
| estado | varchar | Estado actual del inventario (`NORMAL`, `BAJO`, `CRITICO`, `EXCESO`, etc.). | — |
| lote_actual | varchar | Identificador del lote actual. | — |
| ubicacion_almacen | varchar | Ubicación física del producto en el almacén. | — |

---

### 📊 Tabla: `kardex`

| Campo | Tipo de Dato | Descripción | Clave / Relación |
|--------|---------------|-------------|------------------|
| id_kardex | bigint | Identificador del movimiento. | PK |
| id_producto | integer | Producto asociado. | FK → `productos.id_producto` |
| id_usuario | integer | Usuario que registró el movimiento. | FK → `usuarios.id_usuario` |
| cantidad_entrada | integer | Unidades ingresadas. | — |
| cantidad_salida | integer | Unidades retiradas. | — |
| saldo_cantidad | integer | Stock resultante después del movimiento. | — |
| saldo_valorizado | numeric | Valor total del saldo. | — |
| tipo_movimiento | varchar | Tipo de movimiento (entrada/salida/ajuste). | — |
| fecha_movimiento | timestamp | Fecha del movimiento físico. | — |
| fecha_registro | timestamp | Fecha del registro en el sistema. | — |
| motivo | varchar | Motivo del movimiento (venta, compra, ajuste). | — |
| observaciones | varchar | Comentarios o detalles adicionales. | — |

---

### 🧾 Tabla: `categorias`

| Campo | Tipo de Dato | Descripción | Clave / Relación |
|--------|---------------|-------------|------------------|
| id_categoria | integer | Identificador único de la categoría. | PK |
| nombre | varchar | Nombre de la categoría. | — |
| descripcion | varchar | Descripción general. | — |

---

### ⚖️ Tabla: `unidad_medida`

| Campo | Tipo de Dato | Descripción | Clave / Relación |
|--------|---------------|-------------|------------------|
| id_um | integer | Identificador de la unidad de medida. | PK |
| nombre | varchar | Nombre completo (ej. Unidad, Caja, Litro). | — |
| abreviatura | varchar | Símbolo o abreviatura (ej. “u”, “lt”). | — |
| descripcion | varchar | Descripción adicional. | — |

---

### 🚨 Tabla: `alertas_inventario` (relacionada)

| Campo | Tipo de Dato | Descripción | Clave / Relación |
|--------|---------------|-------------|------------------|
| id_alerta | bigint | Identificador único de la alerta. | PK |
| id_producto | integer | Producto asociado. | FK → `productos.id_producto` |
| tipo_alerta | varchar | Tipo (`STOCK_BAJO`, `SOBRESTOCK`, etc.). | — |
| nivel_criticidad | varchar | Nivel (`BAJA`, `MEDIA`, `ALTA`, `CRITICA`). | — |
| estado | varchar | Estado actual (`PENDIENTE`, `RESUELTA`, etc.). | — |
| fecha_generacion | timestamp | Fecha en que se generó la alerta. | — |
| fecha_resolucion | timestamp | Fecha de resolución. | — |
| mensaje | varchar | Descripción breve de la alerta. | — |
| cantidad_sugerida | integer | Cantidad recomendada para ajustar stock. | — |

---

## 💼 Manual de Usuario  
### 👤 Perfil de usuario
- **Operador Logístico:** Gestiona productos, registra movimientos y controla inventarios.  
- **Gerente:** Supervisa los reportes, indicadores y alertas relacionadas al inventario.

---

### 🧭 Flujo de uso

#### 🔹 1. Acceso al módulo
Desde el menú principal, selecciona:  
`Inventario → Gestión de Productos e Inventario`.

---

#### 🔹 2. Registro de Producto
**Ruta:** `Productos → Nuevo`  
**Pasos:**
1. Ingresar nombre del producto.  
2. Seleccionar categoría y unidad de medida.  
3. Registrar costos (adquisición, pedido, mantenimiento).  
4. Guardar el producto.  
📌 *Se crea un registro automático en la tabla `productos`.*

---

#### 🔹 3. Registro de Inventario
**Ruta:** `Inventario → Registrar Stock Inicial`  
**Pasos:**
1. Seleccionar producto.  
2. Definir `stock_disponible`, `stock_minimo`, `stock_maximo`.  
3. Establecer `ubicacion_almacen`.  
4. Guardar registro.  
📌 *Crea un registro en la tabla `inventario`.*

---

#### 🔹 4. Registro de Movimientos (Kardex)
**Ruta:** `Inventario → Movimientos`  
**Pasos:**
1. Seleccionar producto.  
2. Elegir tipo de movimiento (`ENTRADA_COMPRA`, `SALIDA_VENTA`, etc.).  
3. Indicar cantidad y motivo.  
4. Guardar movimiento.  
📌 *Actualiza `inventario.stock_disponible` y registra el evento en `kardex`.*

---

#### 🔹 5. Consulta de Inventario
**Ruta:** `Inventario → Consulta General`  
Permite filtrar por:
- Categoría  
- Estado (`NORMAL`, `CRITICO`, `EXCESO`)  
- Producto específico  

Muestra:
- Stock actual  
- Punto de reorden  
- Valor total del inventario  
- Alertas activas  

---

#### 🔹 6. Generación de Alertas Automáticas
El sistema genera alertas cuando:
- `stock_disponible < stock_minimo` → tipo `STOCK_BAJO`  
- `stock_disponible > stock_maximo` → tipo `SOBRESTOCK`

📌 *Estas alertas se guardan en `alertas_inventario` y se notifican al usuario.*

---

#### 🔹 7. Reporte de Inventario
**Ruta:** `Inventario → Generar Reporte`  
Genera un **PDF** con:
- Resumen de productos y stock.  
- Movimientos recientes (últimos 30 días).  
- Alertas activas.  
📌 *El reporte se obtiene desde la información de `productos`, `inventario` y `kardex`.*

---

## 📊 Indicadores Clave

- **Stock crítico:** productos con `estado = CRITICO`.  
- **Rotación de inventario:** frecuencia de movimiento en el kardex.  
- **Tiempo sin venta:** calculado a partir de `dias_sin_venta`.  
- **Valor total del inventario:** suma de `valor_total_stock`.

---

> ✅ *Este módulo constituye la base del sistema, conectando las operaciones logísticas (movimientos e inventarios) con la analítica predictiva y las alertas inteligentes.*
