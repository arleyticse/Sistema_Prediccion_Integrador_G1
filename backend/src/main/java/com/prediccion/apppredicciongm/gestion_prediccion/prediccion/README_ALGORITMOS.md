# Algoritmos de Predicción — Documentación

Este documento describe los algoritmos de predicción usados en el módulo `prediccion` de Sistema_Prediccion_Unificado, su ubicación, reglas de selección automática, preprocesamiento, métricas y trabajos de mantenimiento relevantes.

---

## Resumen de algoritmos

1. SMILE (Machine Learning):
   - OLS (Regresión Lineal - smile.regression.OLS)
     - Uso: regresión lineal para series con dependencia lineal; además se usa para simular ARIMA mediante features de lag.
     - Ejemplo en código: `SmartPredictorServiceImpl.java` (`OLS.fit(formula, dataFrame)`).

   - RandomForest (smile.regression.RandomForest)
     - Uso: modelo de bosque de árboles para regresión, robusto a relaciones no lineales y features irrelevantes.
     - Ejemplo en código: `RandomForest.fit(formula, dataFrame, ...)`.

   - GradientTreeBoost (smile.regression.GradientTreeBoost)
     - Uso: boosting por gradiente sobre árboles (regresión); buena para series con patrones no lineales complejos.
     - Ejemplo en código: `GradientTreeBoost.fit(formula, dataFrame, props)`.

2. Algoritmos clásicos (implementados internamente):
   - Simple Moving Average - `SimpleMovingAverageAlgorithm` (archivo: `prediccion/algorithms/SimpleMovingAverageAlgorithm.java`)
   - Simple Exponential Smoothing - `SimpleExponentialSmoothingAlgorithm`
   - Holt-Winters - `HoltWintersAlgorithm` (estacionalidad explícita)
   - Uso: se mantienen como líneas base (baselines) y disponibles como beans que pueden ser invocados por `PrediccionServiceImpl`.

3. ARIMA (AutoRegressive Integrated Moving Average), implementación local:
   - En el sistema ARIMA se implementa como una regresión lineal con features lag (autoregresivas) — es decir, se usa OLS sobre columns de retardos para simular ARIMA.
   - Archivo clave: `PrediccionService.java` y lógica en `SmartPredictorServiceImpl.java` donde hay casos `ARIMA`.

---

## Reglas de selección automática (`AUTO`)

La clase `SmartPredictorServiceImpl` contiene la lógica para seleccionar automáticamente el algoritmo cuando `Algoritmo= AUTO`:

- Si la serie muestra estacionalidad (detected por `AnalisisEstacionalidadService`), considerar ARIMA o Holt-Winters.
- Si la variabilidad (coeficiente de variación) se encuentra en un rango bajo-moderado y hay autocorrelación → selecciona `ARIMA`.
- Si la serie es corta (insuficiente para ARIMA) → optar por regresión lineal simple (OLS) o una media móvil.
- Si los datos muestran patrones no lineales y hay suficientes registros → `RandomForest` o `GradientTreeBoost`.
- Si `AUTO` no puede decidir por heurística, ARIMA se usa como default cuando hay datos suficientes.

Estas reglas están codificadas en: `SmartPredictorServiceImpl.java` (método `seleccionarMejorAlgoritmo` / reglas heurísticas con umbrales).

---

## Preprocesamiento y feature engineering

- Transformación de series temporales:
  - Se generan columnas `y` y `lagX` (retardos) cuando se simula ARIMA.
  - Se realizan agregaciones por periodo (p. ej., por mes) y normalizaciones si corresponde (ver `normalizacion` package).

- Data frame & formula:
  - SMILE se usa con `DataFrame` y `Formula` (ej.: `Formula.lhs("target").add("lag1").add("lag2")...`).
  - Asegúrate de convertir `double`, `int` y `BigDecimal` a `double`/`Double` compatibles.

- Tratamiento de datos faltantes/ruido:
  - Normalmente se limpian filas sin datos (o se imputan según políticas de negocio) antes de construir `DataFrame`.
  - En `ReporteDemandaService` se usan varios métodos de limpieza y normalización antes de generar entradas de predicción.

---

## Métricas de validación

- Métricas comunes en el proyecto:
  - RMSE (Root Mean Squared Error)
  - MAE (Mean Absolute Error)
  - R2 (Coeficiente de determinación) — cuando aplica

- Validación:
  - `SmartPredictorServiceImpl` usa validación train/test (y en algunos puntos cross-validation) para comparar modelos.
  - Para Auto-selección se toma la métrica principal (p. ej., menor RMSE o mayor R2) según el tipo de serie.

---

## Mantenimiento y tareas programadas

- Limpiezas automáticas detectadas:
  - `AnalisisEstacionalidadService.limpiarAnalisisAntiguos()` — scheduled: primer día de cada mes (cron `0 0 3 1 * ?`) para inactivar análisis con más de 6 meses.
  - `SmartPredictorServiceImpl.limpiarPrediccionesAntiguas(producto, algoritmo, horizonte)` — invocado para mantener solo las últimas N predicciones por producto/algoritmo/horizonte.
  - `ReporteDemandaService.limpiarDemandaProducto(Producto)` — usado en procesos de recálculo y en cleaning manual.

- Comentarios en el código:
  - En `ProductoService.java` hay una línea comentada que sugiere eliminación masiva del `kardex`:
    - `// kardexRepositorio.deleteAll(movimientos.getContent());` — pendiente de decisión sobre archivado vs borrado irreversible.

---

## Consideraciones de despliegue y configuración

- Build & run (desde el root del backend):

```bash
cd backend
./mvnw -DskipTests package   # Linux/macOS \ msys/git-bash on Windows
# o en Windows cmd:
# mvnw.cmd -DskipTests package
```

- Dependencias principales:
  - SMILE (versión usada en el proyecto; comprobar `pom.xml` para la versión exacta)
  - Apache Commons Math (ARIMA y estadísticos)

---

## Rutas y clases clave

- `SmartPredictorServiceImpl.java` — lógica de SMILE, selección `AUTO` y validación
- `PrediccionServiceImpl.java` — reglas y métodos de ARIMA y pipeline de predicción
- `prediccion/algorithms/*.java` — SMA, SES, Holt-Winters (baselines)
- `AnalisisEstacionalidadService.java` — detección de estacionalidad y limpieza agendada
- Repositorios: `prediccion/repository/IPrediccionRepositorio.java` — manejo de registros y eliminación

---

Archivo generado automáticamente: `prediccion/README_ALGORITMOS.md` — contiene la documentación técnica de los algoritmos de predicción, su ubicación, reglas de selección y tareas de mantenimiento.