# Sistema de Gestión de Inventario - Documentación Completa

## 📋 Resumen Ejecutivo

Este documento describe la implementación completa del sistema de gestión de inventario para un negocio de consumo masivo (alimentos, bebidas, productos de limpieza, medicamentos sin receta, productos del hogar).

El sistema está diseñado para un **local único** y proporciona:
- Control de stock en tiempo real
- Registro de movimientos (Kardex)
- Alertas automáticas de stock bajo
- Trazabilidad completa de productos
- Base sólida para implementar predicciones de demanda

---

## 🏗️ Arquitectura del Sistema

### Estructura de Capas

```
┌─────────────────────────────────────────┐
│          CAPA DE PRESENTACIÓN           │
│         (Angular Frontend)              │
└─────────────────┬───────────────────────┘
                  │ HTTP/REST
┌─────────────────▼───────────────────────┐
│       CAPA DE CONTROLADORES             │
│   - InventarioControlador               │
│   - KardexControlador                   │
│   - ProductoControlador                 │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        CAPA DE SERVICIOS                │
│   - InventarioServicio                  │
│   - KardexServicio                      │
│   - ProductoService                     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      CAPA DE REPOSITORIOS               │
│   - IInventarioRepositorio              │
│   - IKardexRepositorio                  │
│   - IProductoRepositorio                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       BASE DE DATOS (PostgreSQL)        │
│   - productos                           │
│   - inventario                          │
│   - kardex                              │
│   - categorias                          │
│   - unidades_medida                     │
└─────────────────────────────────────────┘
```

---

## 📊 Modelo de Datos

### Entidades Principales

#### 1. **Producto**
```java
- productoId (PK)
- nombre
- costoAdquisicion
- costoMantenimiento
- costoPedido
- diasLeadTime
- categoria (FK)
- unidadMedida (FK)
- fechaRegistro
```

#### 2. **Inventario**
```java
- inventarioId (PK)
- producto (FK - One-to-One)
- stockDisponible
- stockReservado
- stockEnTransito
- stockMinimo
- stockMaximo
- puntoReorden
- ubicacionAlmacen
- estado (ACTIVO, BAJO_STOCK, AGOTADO, INACTIVO)
- diasSinVenta
- observaciones
```

#### 3. **Kardex (Movimientos)**
```java
- kardexId (PK)
- producto (FK)
- fechaMovimiento
- tipoMovimiento (COMPRA, VENTA, AJUSTE, etc.)
- cantidad
- saldoCantidad
- costoUnitario
- lote
- fechaVencimiento
- proveedor (FK)
- usuario (FK)
- numeroDocumento
- motivo
- observaciones
```

---

## 🔄 Flujo de Operaciones

### 1. Registro de Producto Nuevo
```mermaid
Cliente → POST /api/productos → ProductoService
  → ProductoRepository → BD
  → Retorna ProductoResponse
```

### 2. Creación de Inventario
```mermaid
Cliente → POST /api/inventario → InventarioService
  → Valida Producto existe
  → Crea Inventario inicial
  → Retorna InventarioResponse
```

### 3. Registro de Movimiento (Compra)
```mermaid
Cliente → POST /api/movimientos → KardexService
  → Valida Producto existe
  → Calcula nuevo saldo
  → Registra en Kardex
  → Actualiza Inventario
  → Retorna KardexResponse
```

### 4. Consulta de Alertas
```mermaid
Cliente → GET /api/inventario/alertas/criticas
  → InventarioService
  → Consulta productos bajo mínimo
  → Retorna lista de alertas
```

---

## 🎯 Endpoints Principales

### Módulo Inventario

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/inventario` | Crear inventario |
| PUT | `/api/inventario/{id}` | Actualizar inventario |
| GET | `/api/inventario` | Listar todos (paginado) |
| GET | `/api/inventario/{id}` | Obtener por ID |
| GET | `/api/inventario/producto/{id}` | Por producto |
| GET | `/api/inventario/alertas/stock-bajo` | Alertas reorden |
| GET | `/api/inventario/alertas/criticas` | Alertas críticas |
| GET | `/api/inventario/alertas/agotados` | Productos agotados |
| GET | `/api/inventario/resumen` | Resumen general |
| POST | `/api/inventario/ajustar-stock` | Ajuste manual |

### Módulo Movimientos (Kardex)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/movimientos` | Registrar movimiento |
| GET | `/api/movimientos` | Listar todos (paginado) |
| GET | `/api/movimientos/{id}` | Obtener por ID |
| GET | `/api/movimientos/producto/{id}` | Por producto |
| GET | `/api/movimientos/tipo/{tipo}` | Por tipo movimiento |
| GET | `/api/movimientos/fecha` | Por rango fechas |
| GET | `/api/movimientos/resumen` | Resumen movimientos |
| GET | `/api/movimientos/producto/{id}/saldo` | Saldo actual |

---

## 🔍 Tipos de Movimiento

### Entradas (Incrementan Stock)
- **COMPRA**: Compra a proveedor
- **DEVOLUCION_CLIENTE**: Cliente devuelve producto
- **AJUSTE_ENTRADA**: Ajuste positivo por conteo
- **PRODUCCION**: Fabricación interna
- **TRANSFERENCIA_ENTRADA**: De otra sucursal

### Salidas (Decrementan Stock)
- **VENTA**: Venta a cliente
- **DEVOLUCION_PROVEEDOR**: Devolución a proveedor
- **AJUSTE_SALIDA**: Ajuste negativo (daños, vencimientos)
- **CONSUMO**: Uso interno (muestras, degustaciones)
- **MERMA**: Pérdida natural
- **TRANSFERENCIA_SALIDA**: Hacia otra sucursal

---

## 🚨 Sistema de Alertas

### Niveles de Alerta

1. **CRÍTICO** (Rojo)
   - Stock = 0 (Agotado)
   - Requiere acción inmediata

2. **BAJO_MINIMO** (Naranja)
   - Stock < Stock Mínimo
   - Urgente reabastecimiento

3. **REORDEN** (Amarillo)
   - Stock <= Punto de Reorden
   - Iniciar proceso de compra

4. **SIN_MOVIMIENTO** (Azul)
   - Días sin venta > 30 días
   - Revisar estrategia de ventas

5. **NORMAL** (Verde)
   - Stock dentro de rangos normales

---

## 📈 Métricas y Reportes

### Dashboard Principal
```json
{
  "totalProductos": 150,
  "productosActivos": 142,
  "productosInactivos": 8,
  "productosConStockBajo": 12,
  "productosAgotados": 3,
  "productosSinMovimiento": 15,
  "valorTotalInventario": 125450.75,
  "stockTotalDisponible": 8550
}
```

### Resumen de Movimientos
```json
{
  "totalMovimientos": 1250,
  "totalEntradas": 450,
  "totalSalidas": 750,
  "totalAjustes": 50,
  "cantidadTotalEntrada": 25000,
  "cantidadTotalSalida": 22000,
  "productoMasMovido": "Aceite Vegetal 1L"
}
```

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Spring Boot 3.5.6**
- **Java 21**
- **Spring Data JPA**
- **PostgreSQL**
- **MapStruct** (para mapeo DTO-Entity)
- **Lombok** (reducir boilerplate)
- **Swagger/OpenAPI** (documentación API)
- **Spring Validation** (validación de datos)

### Dependencias Clave
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.0</version>
</dependency>
```

---

## 📝 Validaciones Implementadas

### DTOs con Validación

#### InventarioCreateRequest
- `productoId`: @NotNull
- `stockDisponible`: @NotNull, @Min(0)
- `stockMinimo`: @NotNull, @Min(1)
- `puntoReorden`: @NotNull, @Min(1)

#### KardexCreateRequest
- `productoId`: @NotNull
- `tipoMovimiento`: @NotNull
- `cantidad`: @NotNull, @Min(1)
- `motivo`: @NotNull

### Validaciones de Negocio
- No permitir stock negativo
- Verificar stock suficiente antes de salidas
- Validar que producto exista antes de crear inventario
- No permitir duplicados (un inventario por producto)
- Calcular saldo automáticamente en cada movimiento

---

## 🔐 Manejo de Errores

### Excepciones Personalizadas

1. **InventarioNotFoundException**
   - HTTP 404
   - Cuando no se encuentra un inventario

2. **InventarioYaExisteException**
   - HTTP 409 (Conflict)
   - Al intentar crear inventario duplicado

3. **StockInsuficienteException**
   - HTTP 400
   - Al intentar salida con stock insuficiente

4. **MovimientoNotFoundException**
   - HTTP 404
   - Cuando no se encuentra un movimiento

### Respuesta de Error Estándar
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente. Stock disponible: 10, Cantidad solicitada: 50",
  "details": {
    "stockDisponible": 10,
    "cantidadSolicitada": 50,
    "productoId": 1
  },
  "path": "/api/movimientos"
}
```

---

## 🚀 Guía de Implementación

### Paso 1: Verificar Base de Datos
```sql
-- Verificar que existan las tablas
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('productos', 'inventario', 'kardex');
```

### Paso 2: Configurar application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/prediccion_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Swagger UI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Paso 3: Compilar el Proyecto
```bash
cd backend/app-prediccion-gm
mvn clean install
```

### Paso 4: Ejecutar la Aplicación
```bash
mvn spring-boot:run
```

### Paso 5: Acceder a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## 📚 Próximos Pasos (Predicción de Demanda)

Con esta base de gestión de inventario, ahora puedes implementar:

### 1. Análisis de Datos Históricos
- Extraer patrones de venta del Kardex
- Identificar productos de alta/baja rotación
- Detectar estacionalidad

### 2. Predicción de Demanda
- Modelos de series temporales (ARIMA, Prophet)
- Machine Learning (Random Forest, LSTM)
- Factores externos (días festivos, promociones)

### 3. Optimización de Inventario
- Punto de reorden óptimo
- Cantidad económica de pedido (EOQ)
- Nivel de servicio vs costo de inventario

### 4. Datos Necesarios para Predicción
Todos disponibles en el Kardex:
- Historial de ventas diarias
- Patrones de compra
- Información de proveedores
- Costos y tiempos de entrega
- Datos de devoluciones y mermas

---

## 📊 Consultas SQL Útiles

### Stock Actual de Todos los Productos
```sql
SELECT 
    p.nombre,
    i.stock_disponible,
    i.stock_minimo,
    i.punto_reorden,
    i.estado
FROM inventario i
JOIN productos p ON i.id_producto = p.id_producto
WHERE i.estado = 'ACTIVO'
ORDER BY i.stock_disponible ASC;
```

### Productos Más Vendidos (Último Mes)
```sql
SELECT 
    p.nombre,
    SUM(k.cantidad) as total_vendido
FROM kardex k
JOIN productos p ON k.id_producto = p.id_producto
WHERE k.tipo_movimiento = 'VENTA'
AND k.fecha_movimiento >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY p.nombre
ORDER BY total_vendido DESC
LIMIT 10;
```

### Valor Total del Inventario
```sql
SELECT 
    SUM(i.stock_disponible * p.costo_adquisicion) as valor_total
FROM inventario i
JOIN productos p ON i.id_producto = p.id_producto
WHERE i.estado = 'ACTIVO';
```

---

## 🎓 Mejores Prácticas Implementadas

1. **Separación de Responsabilidades**
   - DTOs para transferencia de datos
   - Entities para persistencia
   - Mappers para transformación

2. **Validación en Múltiples Niveles**
   - Validación de entrada (DTOs)
   - Validación de negocio (Services)
   - Validación de integridad (BD)

3. **Manejo Centralizado de Errores**
   - @RestControllerAdvice
   - Respuestas consistentes
   - Logging apropiado

4. **Documentación Automática**
   - Swagger/OpenAPI
   - Ejemplos de uso
   - Descripciones detalladas

5. **Trazabilidad Completa**
   - Registro de usuario en movimientos
   - Timestamps automáticos
   - Referencias cruzadas

6. **Transaccionalidad**
   - @Transactional en operaciones críticas
   - Rollback automático en errores
   - Consistencia de datos

---

## 📞 Soporte y Contacto

Para dudas o soporte adicional:
- Revisar documentación en `/schemas/*.md`
- Consultar ejemplos en Swagger UI
- Verificar logs de aplicación

---

## 📄 Licencia

Este proyecto es parte de un sistema de predicción de demanda para negocios de consumo masivo.

---

**Última Actualización**: 14 de Octubre, 2025
**Versión**: 1.0.0
