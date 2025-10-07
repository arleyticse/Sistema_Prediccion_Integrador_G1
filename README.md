# 🎯 Sistema de Predicción de Demanda y Optimización de Inventario

## Descripción del Proyecto

Sistema completo de gestión y predicción de demanda para tiendas de productos de consumo masivo, con control avanzado de inventario, trazabilidad completa mediante Kardex/Cardex, y sistema inteligente de alertas automáticas.

---

## ✨ Características Principales

### 🏭 **Control de Inventario en Tiempo Real**
- Stock disponible, reservado y en tránsito
- Control de umbrales (mínimo, máximo, punto de reorden)
- Ubicaciones en almacén
- Control de lotes y vencimientos
- Cálculo de rotación de inventario
- Detección automática de productos obsoletos

### 📝 **Kardex/Cardex Completo**
- Registro de TODOS los movimientos de inventario
- 15+ tipos de movimientos (entradas, salidas, ajustes)
- Valorización de inventario (método promedio ponderado)
- Trazabilidad total con auditoría
- Control de lotes y fechas de vencimiento
- Integración con proveedores y clientes

### 🔔 **Sistema de Alertas Inteligente**
- 11 tipos de alertas diferentes
- 4 niveles de criticidad (Baja, Media, Alta, Crítica)
- Generación automática basada en reglas
- Asignación a usuarios responsables
- Escalamiento automático de alertas antiguas
- Notificaciones configurables

### 📊 **Predicción de Demanda**
- Análisis de históricos de ventas
- Consideración de patrones estacionales
- Cálculos EOQ (Economic Order Quantity)
- Punto de reorden (ROP) automático
- Stock de seguridad sugerido
- Optimización de costos de inventario

### 🛒 **Gestión de Compras**
- Base de datos de proveedores con calificaciones
- Órdenes de compra automáticas
- 9 estados de seguimiento de órdenes
- Comparación fechas esperadas vs reales
- Lead times por proveedor
- Generación automática basada en predicciones

### 📈 **Análisis de Estacionalidad**
- Factores estacionales por mes
- Ajuste automático de predicciones
- Identificación de temporadas altas/bajas
- Históricos por temporada
- Demanda máxima y mínima por período

### 📥 **Importación Masiva de Datos**
- Soporte para CSV/Excel
- Múltiples tipos de datos (Inventario, Kardex, Productos, etc.)
- Validación automática
- Registro de errores detallado
- Métricas de rendimiento
- Auditoría completa de importaciones

---

## 🗂️ Estructura del Proyecto

```
app-prediccion-gm/
├── src/
│   ├── main/
│   │   ├── java/com/prediccion/apppredicciongm/
│   │   │   ├── models/              # 16 entidades del dominio
│   │   │   │   ├── Inventario.java
│   │   │   │   ├── Kardex.java
│   │   │   │   ├── Proveedor.java
│   │   │   │   ├── OrdenCompra.java
│   │   │   │   ├── AlertaInventario.java
│   │   │   │   ├── EstacionalidadProducto.java
│   │   │   │   ├── ImportacionDatos.java
│   │   │   │   ├── (+ 9 enumeraciones)
│   │   │   │   └── (+ entidades originales)
│   │   │   ├── dao/                 # Repositorios (a crear)
│   │   │   ├── services/            # Lógica de negocio
│   │   │   │   ├── ImportacionService.java
│   │   │   │   └── AlertaService.java
│   │   │   └── view/                # Controladores REST (a crear)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/
├── pom.xml
├── database-schema.sql              # ⭐ Script SQL completo
├── MEJORAS-SISTEMA.md               # ⭐ Documentación detallada
├── GUIA-IMPLEMENTACION.md           # ⭐ Guía paso a paso
├── CONSULTAS-SQL-UTILES.md          # ⭐ 25 consultas SQL útiles
└── README.md                        # Este archivo
```

---

## 🚀 Inicio Rápido

### **1. Clonar y Configurar**
```bash
cd app-prediccion-gm
```

### **2. Configurar Base de Datos**
Editar `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tu_base_datos
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
```

### **3. Ejecutar Script SQL**
```bash
psql -U tu_usuario -d tu_base_datos -f database-schema.sql
```

### **4. Compilar y Ejecutar**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

---

## 📚 Documentación

| Documento | Descripción |
|-----------|-------------|
| **MEJORAS-SISTEMA.md** | Documentación completa de todas las mejoras, entidades y funcionalidades |
| **GUIA-IMPLEMENTACION.md** | Guía paso a paso para implementar el sistema completo |
| **CONSULTAS-SQL-UTILES.md** | 25 consultas SQL útiles para análisis y reportes |
| **database-schema.sql** | Script SQL completo con tablas, vistas, funciones y triggers |

---

## 🛠️ Tecnologías Utilizadas

- **Java 21** - Lenguaje principal
- **Spring Boot 3.5.6** - Framework principal
- **Spring Data JPA** - ORM y persistencia
- **PostgreSQL** - Base de datos
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias

---

## 📊 Nuevas Entidades Implementadas

### **Tablas Principales:**
1. ✅ `inventario` - Control de stock en tiempo real
2. ✅ `kardex` - Registro de movimientos (Cardex)
3. ✅ `proveedores` - Gestión de proveedores
4. ✅ `ordenes_compra` - Órdenes de reabastecimiento
5. ✅ `detalle_orden_compra` - Detalles de órdenes
6. ✅ `alertas_inventario` - Sistema de alertas
7. ✅ `estacionalidad_producto` - Patrones estacionales
8. ✅ `importaciones_datos` - Registro de importaciones

### **Vistas Útiles:**
- `v_resumen_inventario` - Resumen de inventario con alertas
- `v_kardex_resumen` - Resumen de movimientos Kardex
- `v_alertas_pendientes` - Alertas pendientes ordenadas
- `v_ordenes_pendientes` - Órdenes de compra activas

---

## 💡 Casos de Uso Principales

### **1. Importar Inventario Inicial**
```java
POST /api/importacion/inventario
Content-Type: multipart/form-data
File: inventario_inicial.csv
```

### **2. Registrar Venta**
```java
POST /api/kardex/movimiento
{
  "productoId": 1,
  "tipoMovimiento": "SALIDA_VENTA",
  "cantidadSalida": 50,
  "costoUnitario": 15.50
}
```

### **3. Consultar Alertas Pendientes**
```java
GET /api/alertas/pendientes
```

### **4. Generar Orden de Compra Automática**
```java
POST /api/ordenes/generar-automatica
{
  "productoId": 1
}
```

### **5. Ver Kardex de Producto**
```java
GET /api/kardex/producto/1
```

---

## 📈 Flujo del Sistema

```
┌─────────────────┐
│  Importación    │──┐
│    de Datos     │  │
└─────────────────┘  │
                     ▼
┌─────────────────────────────────────┐
│         INVENTARIO ACTUAL            │
│  Stock: Disponible + Reservado +    │
│         En Tránsito                 │
└─────────────────────────────────────┘
         │                    ▲
         │ Verifica           │ Actualiza
         ▼                    │
┌─────────────────┐   ┌──────────────┐
│    ALERTAS      │   │    KARDEX    │
│  - Stock Bajo   │   │  Movimientos │
│  - Vencimientos │   │ Trazabilidad │
│  - Obsoletos    │   └──────────────┘
└─────────────────┘          │
         │                   │
         │                   ▼
         │        ┌──────────────────┐
         └───────▶│  PREDICCIÓN DE   │
                  │     DEMANDA      │
                  │   EOQ / ROP      │
                  └──────────────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ ORDEN DE COMPRA  │
                  │   Automática     │
                  └──────────────────┘
```

---

## 🎯 Beneficios del Sistema

✅ **Reducción de quiebres de stock** - Alertas tempranas y reabastecimiento automático  
✅ **Optimización de inventario** - Cálculos EOQ y ROP precisos  
✅ **Trazabilidad completa** - Kardex/Cardex detallado  
✅ **Mejor control de costos** - Valorización exacta del inventario  
✅ **Decisiones informadas** - Dashboards y reportes en tiempo real  
✅ **Automatización** - Generación automática de órdenes y alertas  
✅ **Escalabilidad** - Soporte para importación masiva  
✅ **Auditoría** - Registro completo de todos los movimientos  

---

## 🔧 Próximos Pasos para Implementar

1. ✅ **Crear Repositorios (DAO)** - Ver `GUIA-IMPLEMENTACION.md`
2. ✅ **Implementar Servicios** - Lógica de negocio completa
3. ✅ **Crear Controladores REST** - APIs para frontend
4. ✅ **Configurar Tareas Programadas** - Verificación automática
5. ✅ **Implementar Notificaciones** - Email/SMS para alertas críticas
6. ✅ **Crear Frontend** - Dashboard y vistas

Consulta `GUIA-IMPLEMENTACION.md` para instrucciones detalladas paso a paso.

---

## 📞 Soporte

Para dudas o problemas:
1. Revisa `MEJORAS-SISTEMA.md` para documentación completa
2. Consulta `CONSULTAS-SQL-UTILES.md` para ejemplos SQL
3. Sigue `GUIA-IMPLEMENTACION.md` para implementación

---

## 📄 Licencia

Este proyecto es un sistema de gestión empresarial. Consulta con el propietario para términos de uso.

---

## 🙏 Agradecimientos

Sistema diseñado para optimizar la gestión de inventario en tiendas de productos de consumo masivo, con enfoque en predicción de demanda y reducción de costos operativos.

---

**Versión:** 2.0  
**Última actualización:** Octubre 2025  
**Estado:** En desarrollo / Listo para implementación

---

## 📊 Estadísticas del Proyecto

- **16 entidades de dominio** implementadas
- **4 documentos de soporte** completos
- **25+ consultas SQL** útiles incluidas
- **200+ líneas de SQL** en script de base de datos
- **Soporte para 15+ tipos de movimientos** de inventario
- **11 tipos de alertas** automáticas
- **9 estados** para órdenes de compra

---

¡Construyamos juntos un sistema de gestión de clase mundial! 🚀
