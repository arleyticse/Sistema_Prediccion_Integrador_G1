# 🚀 Guía Rápida de Implementación

## Resumen de Nuevas Entidades Creadas

### ✅ Modelos Creados (16 archivos)

#### **Entidades Principales:**
1. **Inventario.java** - Control de stock en tiempo real
2. **Kardex.java** - Registro de movimientos (Cardex)
3. **Proveedor.java** - Gestión de proveedores
4. **OrdenCompra.java** - Órdenes de reabastecimiento
5. **DetalleOrdenCompra.java** - Detalle de órdenes
6. **AlertaInventario.java** - Sistema de alertas
7. **EstacionalidadProducto.java** - Patrones estacionales
8. **ImportacionDatos.java** - Registro de importaciones

#### **Enumeraciones (8 archivos):**
1. **EstadoInventario.java** - Estados del inventario
2. **TipoMovimiento.java** - Tipos de movimiento Kardex
3. **EstadoOrdenCompra.java** - Estados de órdenes
4. **TipoAlerta.java** - Tipos de alertas
5. **NivelCriticidad.java** - Niveles de criticidad
6. **EstadoAlerta.java** - Estados de alertas
7. **TipoDatosImportacion.java** - Tipos de importación
8. **EstadoImportacion.java** - Estados de importación

#### **Servicios de Ejemplo:**
1. **ImportacionService.java** - Importación masiva de CSV
2. **AlertaService.java** - Gestión de alertas

---

## 📋 Próximos Pasos de Implementación

### **PASO 1: Configurar Spring Web (para importación de archivos)**
Agregar al `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Para importación de archivos Excel (opcional) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### **PASO 2: Crear Repositorios (DAO)**
Crear en el paquete `dao`:

```java
// InventarioRepository.java
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findByProducto_ProductoId(Integer productoId);
    List<Inventario> findByEstado(EstadoInventario estado);
    List<Inventario> findByStockDisponibleLessThanOrEqualToPuntoReorden();
    
    @Query("SELECT i FROM Inventario i WHERE i.stockDisponible < i.stockMinimo")
    List<Inventario> findInventariosCriticos();
}

// KardexRepository.java
public interface KardexRepository extends JpaRepository<Kardex, Long> {
    List<Kardex> findByProducto_ProductoIdOrderByFechaMovimientoDesc(Integer productoId);
    
    @Query("SELECT k FROM Kardex k WHERE k.producto.productoId = :productoId " +
           "ORDER BY k.fechaMovimiento DESC LIMIT 1")
    Optional<Kardex> findUltimoMovimiento(@Param("productoId") Integer productoId);
    
    List<Kardex> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Kardex> findByTipoMovimiento(TipoMovimiento tipo);
}

// ProveedorRepository.java
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    List<Proveedor> findByEstadoTrue();
    Optional<Proveedor> findByRucNit(String rucNit);
    List<Proveedor> findByCalificacionGreaterThanOrderByCalificacionDesc(BigDecimal minCalificacion);
}

// OrdenCompraRepository.java
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByEstadoOrden(EstadoOrdenCompra estado);
    List<OrdenCompra> findByProveedor_ProveedorId(Integer proveedorId);
    Optional<OrdenCompra> findByNumeroOrden(String numeroOrden);
    
    @Query("SELECT o FROM OrdenCompra o WHERE o.estadoOrden NOT IN " +
           "('RECIBIDA_COMPLETA', 'CANCELADA') ORDER BY o.fechaOrden DESC")
    List<OrdenCompra> findOrdenesActivas();
}

// AlertaInventarioRepository.java
public interface AlertaInventarioRepository extends JpaRepository<AlertaInventario, Long> {
    List<AlertaInventario> findByEstado(EstadoAlerta estado);
    List<AlertaInventario> findByNivelCriticidad(NivelCriticidad nivel);
    List<AlertaInventario> findByProducto_ProductoId(Integer productoId);
    
    @Query("SELECT a FROM AlertaInventario a WHERE a.estado IN ('PENDIENTE', 'EN_PROCESO') " +
           "ORDER BY CASE a.nivelCriticidad " +
           "WHEN 'CRITICA' THEN 1 WHEN 'ALTA' THEN 2 WHEN 'MEDIA' THEN 3 ELSE 4 END, " +
           "a.fechaGeneracion ASC")
    List<AlertaInventario> findAlertasPendientesOrdenadas();
    
    boolean existsByProducto_ProductoIdAndEstadoAndTipoAlerta(
        Integer productoId, EstadoAlerta estado, TipoAlerta tipo);
}

// EstacionalidadProductoRepository.java
public interface EstacionalidadProductoRepository extends JpaRepository<EstacionalidadProducto, Long> {
    List<EstacionalidadProducto> findByProducto_ProductoId(Integer productoId);
    Optional<EstacionalidadProducto> findByProducto_ProductoIdAndMes(Integer productoId, Integer mes);
}

// ImportacionDatosRepository.java
public interface ImportacionDatosRepository extends JpaRepository<ImportacionDatos, Long> {
    List<ImportacionDatos> findByTipoDatos(TipoDatosImportacion tipo);
    List<ImportacionDatos> findByEstadoImportacion(EstadoImportacion estado);
    List<ImportacionDatos> findByUsuario_UsuarioIdOrderByFechaImportacionDesc(Integer usuarioId);
}
```

### **PASO 3: Ejecutar SQL para Crear Tablas**
1. Abrir PostgreSQL
2. Ejecutar el script `database-schema.sql`
3. Verificar que todas las tablas se crearon correctamente

### **PASO 4: Configurar application.properties**
```properties
# Configuración de JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Configuración de importación de archivos
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Configuración de PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/tu_base_datos
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
```

### **PASO 5: Crear Servicios Completos**

Implementar los servicios faltantes:

#### **KardexService.java**
```java
@Service
public class KardexService {
    @Autowired
    private KardexRepository kardexRepository;
    @Autowired
    private InventarioRepository inventarioRepository;
    
    @Transactional
    public Kardex registrarMovimiento(Kardex kardex) {
        // 1. Obtener último saldo
        Integer saldoAnterior = obtenerUltimoSaldo(kardex.getProducto().getProductoId());
        
        // 2. Calcular nuevo saldo
        int movimiento = kardex.getMovimientoNeto();
        kardex.setSaldoCantidad(saldoAnterior + movimiento);
        
        // 3. Calcular saldo valorizado
        BigDecimal saldoValorizado = calcularSaldoValorizado(kardex);
        kardex.setSaldoValorizado(saldoValorizado);
        
        // 4. Guardar movimiento
        Kardex saved = kardexRepository.save(kardex);
        
        // 5. Actualizar inventario
        actualizarInventario(kardex);
        
        return saved;
    }
    
    // ... más métodos
}
```

#### **InventarioService.java**
```java
@Service
public class InventarioService {
    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private AlertaService alertaService;
    
    public Inventario obtenerOCrearInventario(Integer productoId) {
        return inventarioRepository.findByProducto_ProductoId(productoId)
            .orElseGet(() -> crearInventarioInicial(productoId));
    }
    
    @Transactional
    public void actualizarStock(Integer productoId, int cantidad) {
        Inventario inventario = obtenerOCrearInventario(productoId);
        inventario.setStockDisponible(inventario.getStockDisponible() + cantidad);
        inventario.setFechaUltimoMovimiento(LocalDateTime.now());
        
        // Verificar estado
        verificarEstado(inventario);
        
        inventarioRepository.save(inventario);
        
        // Generar alertas si es necesario
        alertaService.verificarYGenerarAlertas(inventario);
    }
    
    // ... más métodos
}
```

#### **OrdenCompraService.java**
```java
@Service
public class OrdenCompraService {
    @Autowired
    private OrdenCompraRepository ordenRepository;
    @Autowired
    private CalculoOptimizacionRepository calculoRepository;
    
    @Transactional
    public OrdenCompra generarOrdenAutomatica(Integer productoId) {
        // 1. Obtener cálculo de optimización
        CalculoOptimizacion calculo = calculoRepository
            .findLastByProducto(productoId);
        
        // 2. Crear orden de compra
        OrdenCompra orden = new OrdenCompra();
        orden.setNumeroOrden(generarNumeroOrden());
        orden.setGeneradaAutomaticamente(true);
        orden.setFechaOrden(LocalDate.now());
        // ... configurar más campos
        
        // 3. Agregar detalles
        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setCantidadSolicitada(calculo.getEoqCantidadOptima());
        // ... configurar más campos
        
        return ordenRepository.save(orden);
    }
    
    // ... más métodos
}
```

### **PASO 6: Crear Controladores REST**

```java
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    @Autowired
    private InventarioService inventarioService;
    
    @GetMapping
    public List<Inventario> listarTodos() {
        return inventarioService.listarTodos();
    }
    
    @GetMapping("/criticos")
    public List<Inventario> listarCriticos() {
        return inventarioService.listarCriticos();
    }
    
    @GetMapping("/{id}")
    public Inventario obtenerPorId(@PathVariable Integer id) {
        return inventarioService.obtenerPorId(id);
    }
    
    @PutMapping("/{id}/ajustar")
    public Inventario ajustarStock(
        @PathVariable Integer id,
        @RequestParam int cantidad) {
        return inventarioService.ajustarStock(id, cantidad);
    }
}

@RestController
@RequestMapping("/api/kardex")
public class KardexController {
    @Autowired
    private KardexService kardexService;
    
    @GetMapping("/producto/{productoId}")
    public List<Kardex> obtenerPorProducto(@PathVariable Integer productoId) {
        return kardexService.obtenerPorProducto(productoId);
    }
    
    @PostMapping("/movimiento")
    public Kardex registrarMovimiento(@RequestBody Kardex kardex) {
        return kardexService.registrarMovimiento(kardex);
    }
    
    @GetMapping("/reporte")
    public List<Kardex> generarReporte(
        @RequestParam LocalDateTime inicio,
        @RequestParam LocalDateTime fin) {
        return kardexService.obtenerMovimientos(inicio, fin);
    }
}

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {
    @Autowired
    private AlertaService alertaService;
    
    @GetMapping("/pendientes")
    public List<AlertaInventario> listarPendientes() {
        return alertaService.listarPendientes();
    }
    
    @GetMapping("/resumen")
    public AlertaService.AlertaResumen obtenerResumen() {
        return alertaService.obtenerResumenAlertas();
    }
    
    @PutMapping("/{id}/resolver")
    public void resolverAlerta(
        @PathVariable Long id,
        @RequestParam String accion,
        @RequestParam Integer usuarioId) {
        alertaService.resolverAlerta(id, accion, usuarioId);
    }
}

@RestController
@RequestMapping("/api/importacion")
public class ImportacionController {
    @Autowired
    private ImportacionService importacionService;
    
    @PostMapping("/kardex")
    public ImportacionDatos importarKardex(
        @RequestParam("file") MultipartFile file,
        @RequestParam Integer usuarioId) {
        return importacionService.importarKardexDesdeCSV(file, usuarioId);
    }
    
    @PostMapping("/inventario")
    public ImportacionDatos importarInventario(
        @RequestParam("file") MultipartFile file,
        @RequestParam Integer usuarioId) {
        return importacionService.importarInventarioDesdeCSV(file, usuarioId);
    }
    
    @GetMapping("/template/{tipo}")
    public String descargarTemplate(@PathVariable TipoDatosImportacion tipo) {
        return importacionService.generarTemplateCSV(tipo);
    }
}
```

### **PASO 7: Configurar Tareas Programadas**

```java
@Configuration
@EnableScheduling
public class ScheduledTasks {
    @Autowired
    private AlertaService alertaService;
    
    // Ejecutar cada hora
    @Scheduled(cron = "0 0 * * * *")
    public void verificarInventarios() {
        alertaService.verificarYGenerarAlertas();
    }
    
    // Ejecutar diariamente a las 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void escalarAlertas() {
        alertaService.escalarAlertasAntiguas(7); // Escalar después de 7 días
    }
}
```

---

## 📊 Flujo Completo de Uso

### **1. Importar Datos Iniciales**
```
POST /api/importacion/inventario
File: inventario_inicial.csv
```

### **2. Registrar Movimientos**
```
POST /api/kardex/movimiento
{
  "producto": {"productoId": 1},
  "tipoMovimiento": "ENTRADA_COMPRA",
  "cantidadEntrada": 100,
  "costoUnitario": 15.50,
  "proveedor": "Proveedor ABC"
}
```

### **3. Verificar Alertas**
```
GET /api/alertas/pendientes
```

### **4. Generar Orden de Compra**
```
POST /api/ordenes/generar-automatica
{
  "productoId": 1
}
```

### **5. Consultar Kardex**
```
GET /api/kardex/producto/1
```

---

## 🎯 Métricas de Éxito

Después de implementar el sistema, deberías poder:

✅ Importar datos masivos desde CSV/Excel  
✅ Registrar todos los movimientos de inventario automáticamente  
✅ Ver alertas en tiempo real  
✅ Generar órdenes de compra automáticas  
✅ Consultar trazabilidad completa (Kardex)  
✅ Analizar patrones estacionales  
✅ Optimizar niveles de inventario  
✅ Reducir quiebres de stock  
✅ Minimizar costos de almacenamiento  

---

## 📚 Recursos Adicionales

- **MEJORAS-SISTEMA.md**: Documentación completa del sistema
- **database-schema.sql**: Script SQL completo
- **ImportacionService.java**: Ejemplo de importación
- **AlertaService.java**: Ejemplo de gestión de alertas

---

## 🆘 Solución de Problemas Comunes

### Error: MultipartFile no se encuentra
**Solución**: Agregar `spring-boot-starter-web` al pom.xml

### Error: Las tablas no se crean
**Solución**: Verificar que `spring.jpa.hibernate.ddl-auto=update` y ejecutar el script SQL

### Error: Los warnings en los servicios
**Solución**: Son normales ya que los métodos están comentados. Al descomentar e implementar desaparecerán.

---

**¡Éxito con tu implementación! 🚀**
