# 🎯 Guía Rápida de Uso - Flujo de Procesamiento de Alertas

## 📍 Acceso al Sistema

### URL Frontend
```
http://localhost:4200/administracion/alertas-inventario/flujo-procesamiento
```

### URL Backend (API)
```
http://localhost:8080/api/alertas-inventario/
```

---

## 🚀 Cómo Usar el Sistema

### **PASO 1: Seleccionar Alertas** 📋

#### Pantalla Inicial
Verás un dashboard con:
- **Header**: Título "Flujo de Procesamiento de Alertas"
- **Progress Steps**: Barra de progreso con 3 pasos
- **Cards de Resumen**:
  - 🔵 Alertas Seleccionadas: 0
  - 🟢 Productos: 0  
  - 🟣 Costo Estimado: S/ 0.00

#### Configuración
1. **Ajustar Horizonte de Predicción**:
   - Rango: 7 a 365 días
   - Default: 90 días
   - Usa los botones +/- o escribe el valor

#### Seleccionar Alertas
Opciones:
1. **Por Proveedor Completo**:
   - Click en el checkbox del header del proveedor
   - Selecciona todas las alertas de ese proveedor
   
2. **Alertas Individuales**:
   - Click en checkbox de cada fila
   - Selecciona solo las que necesites

#### Ver Información
Cada proveedor muestra:
- Nombre comercial
- Total de alertas activas
- Tabla con:
  - SKU (código de barras)
  - Nombre del producto
  - Stock actual (en rojo si está bajo)
  - Punto ROP
  - Cantidad sugerida (en verde)
  - Costo unitario
  - Criticidad (ALTA/MEDIA/BAJA)

#### Continuar
- Click en **"Generar Predicciones"** (botón azul grande)
- El sistema validará que hayas seleccionado al menos 1 alerta

---

### **PASO 2: Ver Predicciones** 📊

#### Proceso Automático
El sistema:
1. Envía alertas seleccionadas al backend
2. Ejecuta algoritmos SMILE ML:
   - Random Forest (preferido)
   - Gradient Boosting
   - OLS (regresión lineal)
3. Extrae **540 valores históricos** de la base de datos
4. Genera **30 predicciones futuras**
5. Calcula métricas de calidad (MAPE, MAE, RMSE)
6. Optimiza EOQ y ROP

#### Pantalla de Resultados

**Métricas Agregadas por Proveedor** (5 cards):
- 🔵 **MAPE Promedio**: Error porcentual (menor es mejor)
- 🟢 **Calidad General**: EXCELENTE/BUENA/REGULAR/MALA
- 🟣 **Aceptables**: % de predicciones con MAPE ≤ 20%
- 🟡 **Productos**: Total procesados
- 🔴 **MAE Promedio**: Error absoluto medio

**Distribución de Calidad**:
- Excelentes (MAPE < 10%)
- Buenas (MAPE 10-20%)
- Regulares (MAPE 20-50%)
- Malas (MAPE > 50%)

**Tabla de Predicciones**:
- SKU del producto
- Nombre del producto
- Algoritmo usado (tag colorido)
- Calidad (tag: EXCELENTE/BUENA/REGULAR/MALA)
- MAPE % (en verde si < 20%)
- MAE
- EOQ (Cantidad Óptima)
- ROP (Punto de Reorden)
- Botón **"Ver Gráfico"**

#### Ver Detalles del Gráfico

Click en **"Ver Gráfico"** para abrir diálogo con:

**Header Personalizado**:
- Nombre del producto
- SKU

**4 Cards de Info Básica**:
- ⚙️ Algoritmo usado
- 📅 Horizonte (días)
- 💾 Datos históricos (cantidad)
- 📈 Predicciones generadas

**Métricas de Calidad (4 cards grandes)**:
- MAPE (azul): % de error
- MAE (verde): error absoluto
- RMSE (púrpura): error cuadrático
- Calidad (ámbar): tag grande

**Gráfico Interactivo**:
- **Línea azul sólida**: Últimos 60 días de demanda real
- **Línea verde punteada**: 30 días de predicción futura
- **Línea gris**: Conexión entre histórico y predicción
- Hover sobre puntos para ver valores exactos
- Zoom con scroll (si está habilitado)

**Características de la Serie**:
- ✅ Tendencia detectada (o ❌ sin tendencia)
- ✅ Estacionalidad detectada (o ❌ sin estacionalidad)

**Optimización de Inventario**:
- 📦 **EOQ**: Cantidad óptima de pedido
  - Minimiza costos de pedido e inventario
- 🚩 **ROP**: Punto de reorden
  - Momento ideal para realizar nuevo pedido

**Advertencias** (si existen):
- ⚠️ Lista de advertencias sobre la predicción

**Recomendaciones** (si existen):
- 💡 Lista de recomendaciones basadas en el análisis

#### Continuar
- Click en **"Generar Órdenes de Compra"** (botón verde grande)

---

### **PASO 3: Órdenes Generadas** ✅

#### Header de Éxito
- ✅ ícono grande verde
- Mensaje: "¡Proceso Completado Exitosamente!"

#### Estadísticas (4 cards):
- 📥 **Total Procesadas**: Alertas procesadas
- ✅ **Exitosas**: Predicciones correctas
- 🛒 **Órdenes**: Órdenes generadas
- ⚠️ **Fallos**: Predicciones fallidas

#### Lista de Órdenes Generadas
- Tags verdes con IDs de órdenes
- Ejemplo: "Orden #123", "Orden #124"

#### Opciones Finales
1. **Ver Todas las Órdenes**:
   - Navega a `/administracion/ordenes-compra`
   - Muestra todas las órdenes del sistema
   
2. **Iniciar Nuevo Flujo**:
   - Limpia todo el estado
   - Vuelve al Paso 1
   - Recarga alertas desde el backend

---

## 🎨 Interpretación de Colores

### Criticidad de Alertas
- 🔴 **ALTA**: Rojo (urgente, stock muy bajo)
- 🟡 **MEDIA**: Ámbar (moderado, revisar pronto)
- 🟢 **BAJA**: Verde (estable, no urgente)

### Calidad de Predicción
- 🟢 **EXCELENTE**: Verde (MAPE < 10%) - Alta confianza
- 🔵 **BUENA**: Azul (MAPE 10-20%) - Confianza aceptable
- 🟡 **REGULAR**: Ámbar (MAPE 20-50%) - Confianza moderada
- 🔴 **MALA**: Rojo (MAPE > 50%) - Baja confianza

### Algoritmos
- 🟢 **RANDOM_FOREST**: Verde (preferido, más preciso)
- 🔵 **GRADIENT_BOOSTING**: Azul (buena alternativa)
- ⚪ **OLS**: Gris (regresión lineal simple)

---

## 📊 Métricas Explicadas

### MAPE (Mean Absolute Percentage Error)
- **Qué es**: Error porcentual promedio
- **Rango**: 0% a 100% (menor es mejor)
- **Interpretación**:
  - < 10%: Excelente
  - 10-20%: Buena
  - 20-50%: Regular
  - > 50%: Mala

### MAE (Mean Absolute Error)
- **Qué es**: Error absoluto promedio en unidades
- **Ejemplo**: MAE = 5 significa error de ±5 unidades
- **Uso**: Medir desviación en cantidades reales

### RMSE (Root Mean Squared Error)
- **Qué es**: Raíz del error cuadrático medio
- **Característica**: Penaliza más los errores grandes
- **Uso**: Identificar outliers y variabilidad

### EOQ (Economic Order Quantity)
- **Qué es**: Cantidad óptima de pedido
- **Objetivo**: Minimizar costos totales (pedido + almacenamiento)
- **Uso**: Determinar cuánto comprar cada vez

### ROP (Reorder Point)
- **Qué es**: Punto de reorden
- **Objetivo**: Evitar quiebres de stock
- **Uso**: Saber cuándo hacer un nuevo pedido

---

## 🔍 Tips de Uso

### ✅ Buenas Prácticas

1. **Selecciona Horizontes Realistas**:
   - Productos de alta rotación: 30-60 días
   - Productos de baja rotación: 90-180 días
   - Productos estacionales: 365 días (un año completo)

2. **Revisa los Gráficos**:
   - Busca patrones claros en el histórico
   - Verifica que la predicción siga la tendencia
   - Desconfía si hay cambios bruscos sin explicación

3. **Considera la Calidad**:
   - Predicciones MALAS (MAPE > 50%): Revisar manualmente
   - Predicciones REGULARES: Usar con precaución
   - Predicciones BUENAS/EXCELENTES: Confiables

4. **Agrupa por Proveedor**:
   - Optimiza logística
   - Reduce costos de envío
   - Negocia mejores precios por volumen

### ⚠️ Advertencias

1. **Datos Históricos Insuficientes**:
   - Si hay < 30 registros, la predicción puede ser imprecisa
   - Sistema mostrará advertencia en el diálogo

2. **Productos Nuevos**:
   - Sin histórico, no se puede predecir
   - Usar métodos manuales o basados en productos similares

3. **Cambios en el Mercado**:
   - Predicciones asumen condiciones estables
   - Eventos extraordinarios (promociones, crisis) no se predicen

4. **Estacionalidad**:
   - Productos estacionales necesitan al menos 1 año de datos
   - Verificar que el sistema detecte estacionalidad

---

## 🐛 Solución de Problemas

### Problema: No carga alertas
**Solución**:
1. Verificar que el backend esté corriendo: `http://localhost:8080`
2. Revisar consola del navegador (F12)
3. Verificar que haya productos con stock bajo en la BD

### Problema: Error al generar predicciones
**Solución**:
1. Verificar que seleccionaste al menos 1 alerta
2. Revisar que el horizonte esté entre 7-365 días
3. Ver logs del backend para detalles del error

### Problema: Gráfico no se muestra
**Solución**:
1. Verificar que Chart.js esté instalado: `npm list chart.js`
2. Limpiar caché del navegador (Ctrl+Shift+R)
3. Revisar consola del navegador

### Problema: Órdenes no se generan
**Solución**:
1. Verificar que las predicciones fueron exitosas
2. Revisar logs del backend (puede ser problema con EOQ/ROP)
3. Verificar que los productos tengan proveedor asignado

---

## 📞 Soporte

Si tienes problemas:
1. **Revisar logs del backend**: `backend/logs/application.log`
2. **Revisar consola del navegador**: F12 → Console
3. **Revisar documentación completa**: `MEJORAS_FLUJO_PROCESAMIENTO.md`

---

## 🎓 Capacitación Recomendada

### Para Usuarios Finales
1. Entender conceptos de inventario (EOQ, ROP)
2. Interpretar métricas de predicción (MAPE, MAE)
3. Usar el flujo completo (3 pasos)

### Para Administradores
1. Configurar productos y proveedores correctamente
2. Mantener datos históricos actualizados
3. Monitorear calidad de predicciones
4. Ajustar parámetros según necesidad del negocio

---

## ✨ Características Destacadas

### 🚀 Velocidad
- Predicciones en tiempo real
- Interfaz reactiva (signals)
- Carga lazy de componentes

### 🎨 Diseño
- UI moderna con PrimeNG
- Gradientes y animaciones suaves
- Responsive (funciona en móviles)

### 📊 Visualización
- Gráficos interactivos con Chart.js
- 540 datos históricos + 30 predicciones
- Tooltips informativos

### 🧠 Inteligencia
- 3 algoritmos de ML (RANDOM_FOREST, GRADIENT_BOOSTING, OLS)
- Selección automática del mejor algoritmo
- Detección de tendencia y estacionalidad

### ✅ Confiabilidad
- Validaciones en cada paso
- Feedback visual claro
- Manejo de errores

---

**¡Disfruta del nuevo sistema de predicciones!** 🎉
