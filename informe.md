![](data:image/png;base64...)

## “Sistema de Escritorio para la Predicción de Demanda y Optimización de Recursos en la empresa Global Market S.A.C.”

Rojas Bautista, Josue Jesus. Ticse Torres, Arley Alberto.

Yupanqui De la Cruz, Renzo Estefano.

Zegarra Bastidas, Jeyson Daniel.

## Facultad de Ingeniería, Escuela Profesional de Ingeniería de Sistemas y Software Sección 31727. Curso Integrador I: Sistemas Software

Mg. Carlos Alberto Effio Gonzales

Lima, Setiembre 2025

# ÍNDICE

[Introducción 5](#_bookmark2)

[Capítulo 1 6](#_bookmark3)

[ASPECTOS GENERALES 6](#_bookmark4)

* 1. [Descripción de la Empresa 6](#_bookmark5)
     1. [Misión 6](#_bookmark6)
     2. [Visión 6](#_bookmark7)
     3. [Rubro del Negocio 6](#_bookmark8)
     4. [Recursos Humanos 7](#_bookmark9)
     5. [Equipo Tecnológico 8](#_bookmark10)
        1. [Hardware 9](#_bookmark11)
        2. [Software 9](#_bookmark12)
  2. [Definición de Problemáticas 9](#_bookmark13)
     1. [Dependencia de Métodos Manuales y Subjetivos 10](#_bookmark14)
     2. [Dificultad Para Predecir la Demanda 11](#_bookmark15)
     3. [Ineficiencia en la Gestión de Inventarios 11](#_bookmark16)
     4. [Falta de Información en Tiempo Real 11](#_bookmark17)
  3. [Definición de Objetivos 12](#_bookmark18)
     1. [Objetivo General 12](#_bookmark19)
     2. [Objetivos Específicos 12](#_bookmark20)
     3. [Alcances y Limitaciones 13](#_bookmark21)
        1. [Alcances 13](#_bookmark22)
        2. [Limitaciones 13](#_bookmark23)
     4. [Valor Agregado 14](#_bookmark24)
     5. [Justificación 14](#_bookmark25)
     6. [Estado del Arte 15](#_bookmark26)
        1. [Problemas Generales Relacionados 19](#_bookmark27)

[Capítulo 2 23](#_bookmark28)

[MARCO TEÓRICO 23](#_bookmark29)

* 1. [Fundamento Teórico 23](#_bookmark30)
     1. [Lenguaje De Programación Java 23](#_bookmark31)
     2. [Paradigma De Programación Orientado a Objetos 24](#_bookmark32)
     3. [Commons Math 24](#_bookmark33)
     4. [DataVec 25](#_bookmark34)
     5. [Commons CSV 25](#_bookmark35)
     6. [Apache POI 26](#_bookmark36)
     7. [JFreeChart 26](#_bookmark37)
     8. [JasperReports 26](#_bookmark38)
  2. [Antecedentes 27](#_bookmark39)
  3. [Descripción de los Procesos de Negocio 30](#_bookmark40)

[Capítulo 3 31](#_bookmark41)

* 1. [Requerimientos Funcionales 31](#_bookmark42)
  2. [Requerimientos No Funcionales 32](#_bookmark43)
  3. [Desarrollo de la Solución 33](#_bookmark44)
     1. [Solución a Dependencia de Métodos Manuales y Subjetivos 33](#_bookmark45)
     2. [Solución a Dificultad para Predecir la Demanda 34](#_bookmark46)
     3. [Solución a Ineficiencia en la Gestión de Inventarios 34](#_bookmark47)
     4. [Solución a Falta de Información en Tiempo Real 34](#_bookmark48)
  4. [Diseño de Prototipos 34](#_bookmark49)
     1. [Prototipo 1 34](#_bookmark50)
     2. [Prototipo 2 39](#_bookmark51)
     3. [Prototipo 3 42](#_bookmark52)
  5. [Diagrama de Casos de Uso 46](#_bookmark53)
  6. [Diagrama de Clases 48](#_bookmark54)
  7. [Modelo de Datos 50](#_bookmark55)
     1. [Conceptual 50](#_bookmark56)
     2. [Lógico 50](#_bookmark57)
     3. [Físico 51](#_bookmark58)
  8. [Diccionario de Datos 52](#_bookmark59)
  9. [Bibliografía 58](#_bookmark60)

[ANEXOS 60](#_bookmark61)

[Anexo 1: Diagrama de Gantt 60](#_bookmark62)

[Anexo 2: Work Breakdown Structure 61](#_bookmark63)

[Anexo 3: Project Charter 61](#_bookmark64)

[Anexo 4: Lean Canvas 63](#_bookmark65)

[Anexo 5: Diagrama de Procesos BPM 64](#_bookmark66)

## Introducción

En la actualidad, las empresas dedicadas al rubro de consumo masivo enfrentan el desafío constante de responder a la variabilidad de la demanda de los clientes, lo que impacta directamente en la gestión de inventarios, el uso eficiente de recursos y la rentabilidad del negocio. La falta de herramientas adecuadas para anticipar los niveles de demanda genera problemas como el exceso de inventario, quiebres de stock o una asignación ineficiente de recursos humanos y tecnológicos.

Ante este panorama, la implementación de sistemas inteligentes basados en técnicas de predicción de demanda surge como una alternativa innovadora para optimizar los procesos internos y garantizar una mejor toma de decisiones. El presente proyecto tiene como finalidad desarrollar un sistema de escritorio de predicción de demanda que permita a la empresa Global Market S.A.C. anticipar las necesidades de sus clientes y de esta forma, optimizar sus recursos financieros, logísticos y operativos.

Este sistema busca no solo mejorar la planificación de inventarios y la reducción de costos asociados al desperdicio, sino también potenciar la eficiencia en la cadena de abastecimiento. Con ello, la empresa podrá asegurar una mayor disponibilidad de productos, incrementar la satisfacción de sus clientes y fortalecer su competitividad en el mercado.

## Capítulo 1

En este capítulo se define y describe el ámbito de la empresa, el problema que necesita la implementación de nuestro sistema. Asimismo, se define los objetivos generales y específicos de nuestro proyecto. Además, se establecen los alcances de nuestro sistema y limitaciones para tener en cuenta durante su desarrollo. De este modo, el capítulo termina con la presentación de la justificación del proyecto y la definición del estado del arte.

# ASPECTOS GENERALES

## Descripción de la Empresa

La empresa Global Market S.A.C. es una organización dedicada al sector de consumo masivo, especializada en la comercialización de una amplia gama de productos. Cuenta con una sólida y diversa cartera de clientes, lo que le permite mantener una presencia significativa en el mercado.

### Misión

Proporcionar una herramienta tecnológica integral que permita analizar, procesar y proyectar datos empresariales, con el fin de mejorar la planificación operativa, optimizar los recursos disponibles y apoyar la toma de decisiones estratégicas dentro de Global Market S.A.C..

### Visión

Convertirse en una plataforma inteligente y confiable que impulse la transformación digital de la gestión empresarial en Global Market S.A.C., permitiendo la predicción precisa de la demanda, la optimización de los procesos logísticos y el aprovechamiento eficiente de recursos.

### Rubro del Negocio

Global Market S.A.C. opera en el sector de comercialización de productos de consumo masivo, con un enfoque en la venta minorista. Su portafolio incluye productos de primera

necesidad como abarrotes, bebidas, artículos de higiene personal, productos de limpieza, enlatados, entre otros. La empresa se orienta a satisfacer la demanda cotidiana de los consumidores finales, mediante puntos de venta accesibles y una oferta diversificada.

### Recursos Humanos

### La empresa cuenta con un equipo multidisciplinario conformado por áreas de ventas, logística, almacén, finanzas y tecnología. Sin embargo, la toma de decisiones aun depende en gran medida de métodos manuales y experiencia empírica de los encargados de cada área.

## Tabla 1

*Delimitación de la departamentalización de la empresa Global Market S.A.C.*

## Cargo Cantidad Descripción

Gerente General 1 Supervisa la operación integral de la empresa y

define estrategias de crecimiento.

Área administrativa y Contable

2 Gestiona la contabilidad, facturación y control administrativo de la organización.

Área de Ventas 3 Atiende a los clientes, gestiona pedidos y promueve

los productos en el mercado.

Área de Logística 3 Controla el abastecimiento, almacenamiento y

disponibilidad de los productos.

Área de Soporte Técnico/TI

1 Mantiene la infraestructura tecnológica y brinda soporte a los sistemas internos.

### Equipo Tecnológico

La empresa dispone de una infraestructura tecnológica básica pero funcional, que respalda sus operaciones administrativas, comerciales y logísticas. A continuación, se detalla el equipamiento:

## Hardware

* + - * + 5 computadoras de escritorio distribuidas entre las áreas de administración, ventas y logística.
        + 2 laptops asignadas a los equipos de ventas y logística para operaciones móviles.
        + 1 servidor local para el almacenamiento y gestión de datos internos.
        + 2 impresoras multifuncionales utilizadas para la emisión de comprobantes de pago, documentos administrativos y reportes.
        + Red de área local (LAN) que interconecta los dispositivos dentro de la organización.
        + Conexión a Internet para actividades operativas y acceso a plataformas digitales.

## Software

* + - * + Paquete de ofimática Microsoft Office 365 utilizado para tareas administrativas, elaboración de informes, hojas de cálculo, presentaciones, entre otros.
        + Sistema operativo Windows 11, instalado en los equipos de escritorio y portátiles para garantizar compatibilidad, seguridad y actualización de sistemas.

## Definición de Problemáticas

### Dependencia de Métodos Manuales y Subjetivos

La empresa presenta una marcada dependencia de métodos manuales e intuitivos para la gestión del inventario y la toma de decisiones operativas, sin el respaldo de herramientas tecnológicas ni sistemas de información adecuados. Esta situación genera proyecciones poco precisas, exceso de productos de baja rotación y desabastecimiento de artículos con alta demanda.

Estas deficiencias no solo afectan las ventas y aumentan los costos de almacenamiento, sino que también disminuyen la satisfacción del cliente. Además, la falta de un enfoque basado en datos limita la capacidad de anticiparse a cambios del mercado y reduce la eficiencia operativa. En consecuencia, la empresa pierde competitividad en un entorno cada vez más dinámico, impactando su rentabilidad y crecimiento sostenible.

### Dificultad Para Predecir la Demanda

La empresa Global Market enfrenta dificultades para anticipar con precisión la demanda de sus productos, lo que genera desequilibrios en la gestión de inventario. Esta falta de previsión puede derivar tanto en exceso de stock con sus respectivos costos de almacenamiento como en desabastecimiento de productos clave, afectando directamente las ventas y la experiencia del cliente.

La ausencia de un sistema de predicción limita la planificación operativa y dificulta la asignación eficiente de recursos humanos, tecnológicos y financieros. Esto se traduce en una sobrecarga en algunas áreas, subutilización en otras y en general una menor productividad organizacional.

En un entorno comercial altamente competitivo, esta ineficiencia reduce la capacidad de respuesta de la empresa ante cambios en el comportamiento del consumidor, comprometiendo su rentabilidad y sostenibilidad a largo plazo.

### Ineficiencia en la Gestión de Inventarios

La gestión de inventarios en la empresa presenta deficiencias importantes que afectan tanto la operatividad como la rentabilidad del negocio. La falta de control y seguimiento

adecuado genera errores frecuentes en los niveles de stock, lo cual deriva en dos problemas críticos: exceso de productos con baja rotación y desabastecimiento de productos de alta demanda.

Estas situaciones no solo incrementan los costos operativos (como almacenamiento innecesario y deterioro de productos), sino que también afectan la capacidad de respuesta ante los requerimientos del mercado. Asimismo, la ausencia de procedimientos estandarizados y herramientas tecnológicas para el control de inventario dificulta la toma de decisiones informadas, lo que repercute negativamente en la planificación de compras, ventas y logística.

Una gestión de inventarios ineficiente impacta directamente en la satisfacción del cliente, al no garantizar la disponibilidad constante de productos clave, y en la rentabilidad, al provocar pérdidas económicas por productos obsoletos o fuera de stock.

### Falta de Información en Tiempo Real

La empresa no cuenta con un sistema que le permita acceder a información actualizada y en tiempo real sobre el estado de su inventario y la demanda del mercado. Esta limitación restringe la capacidad de respuesta ante cambios en el comportamiento del consumidor y dificulta la toma de decisiones operativas oportunas.

La falta de datos en tiempo real genera retrasos en la identificación de quiebres de stock, reposiciones tardías y una planificación poco precisa de las compras. Además, impide un monitoreo eficiente del desempeño del negocio, lo cual dificulta la detección temprana de problemas y la implementación de mejoras continuas.

En un entorno comercial altamente dinámico, donde las decisiones deben ser rápidas y fundamentadas, la ausencia de información actualizada coloca a la empresa en desventaja competitiva. Contar con visibilidad inmediata de las operaciones no solo mejora la eficiencia,

sino que permite una gestión más proactiva, orientada a la mejora continua y la satisfacción del cliente.

## Definición de Objetivos

### Objetivo General

Desarrollar un sistema de escritorio para la empresa Global Market S.A.C. que permita la predicción de la demanda de sus productos y la optimización de recursos, con el fin de mejorar la eficiencia operativa, reducir costos y facilitar la toma de decisiones estratégicas basadas en datos.

### Objetivos Específicos

* Diseñar un modelo de base de datos relacional que permita almacenar y gestionar de manera estructurada la información histórica necesaria para los procesos de predicción y optimización.
* Implementar un módulo de predicción de demanda, utilizando algoritmos de análisis de datos avanzados, que permita estimar de forma precisa el comportamiento futuro de las ventas y reducir la incertidumbre en la planificación.
* Evaluar el desempeño y confiabilidad del sistema mediante pruebas funcionales, de usabilidad y rendimiento, asegurando su correcta operación en el entorno empresarial
* Generar informes y visualizaciones de tendencias dinámicas de los datos evidenciando la demanda y el stock de producto

### Alcances y Limitaciones

El presente sistema abarcara los procesos esenciales de la predicción de demanda y la optimización de recursos, integrando un dashboard interactivo con gráficos estadísticos que brinden información en tiempo real.

## Alcances

* + - * + Desarrollar una aplicación de escritorio para predecir la demanda de productos en la empresa Global Market.
        + Implementar un sistema de gestión de inventarios que permita a la empresa administrar sus productos de manera eficiente.
        + Crear un dashboard que muestre información en tiempo real sobre la demanda y el stock de productos.
        + Generar informes u reportes en PDF de análisis predictivos realizados en el sistema.

## Limitaciones

* + - * + El proyecto se limitará a la predicción de la demanda de productos y no incluirá el análisis predictivo de otros factores que puedan afectar a la empresa.
        + El sistema se limitará a la empresa Global Market y no se podrá generalizar a otras empresas.
        + El proyecto requerirá una cantidad significativa de datos históricos para entrenar el modelo de predicción.
        + El sistema no contará con integración ni compatibilidad con dispositivos móviles.

### Valor Agregado

* Recomendaciones de compra más eficientes para mejorar la gestión de abastecimiento.
* Soporte a la toma de decisiones estratégicas mediante indicadores clave de desempeño.

### Justificación

El presente proyecto responde a la necesidad de mejorar la eficiencia operativa y la toma de decisiones de Global Market S.A.C., una empresa que actualmente gestiona su inventario y recursos mediante procesos manuales y basados en la experiencia laboral. Esta dependencia de métodos tradicionales ha generado errores en la planificación, sobrecostos logísticos y una baja capacidad de respuesta ante las variaciones del mercado. Por ello, se propone el desarrollo de un sistema de escritorio que permita automatizar y optimizar la gestión de la demanda, garantizando una administración más precisa y sostenible de los recursos empresariales.

La implementación de un sistema de escritorio para la predicción de demanda permitirá anticipar las tendencias del mercado mediante modelos estadísticos y predictivos, optimizando los recursos financieros, logísticos y humanos. Esto reducirá los costos derivados de una gestión ineficiente, garantizará la disponibilidad oportuna de productos y fortalecerá la competitividad de la empresa.

Desde una perspectiva académica, el proyecto ofrece la oportunidad de aplicar técnicas de inteligencia artificial, análisis de series temporales y aprendizaje automático en un caso real, demostrando el impacto de estas tecnologías en el sector de consumo masivo. Desde el ámbito social, la solución mejorará la experiencia del cliente al proporcionar un servicio más eficiente y confiable.

Para terminar, esta iniciativa se alinea con la misión de la empresa de ofrecer productos de calidad con una gestión eficiente y apoya su visión de posicionarse como líder en innovación tecnológica en el sector de consumo masivo

### Estado del Arte

## Nacionales

En el año 2023, Juan Diego Morales Quispe, realizó la tesis titulada “Aplicación de un sistema de predicción de demanda para la mejora del proceso de planificación logística en la operación ecommerce de electrodomésticos para un supermercado”. La investigación fue elaborada en la Universidad Nacional de Ingeniería, Facultad de Ingeniería Industrial y Sistemas en Lima, Perú. El objetivo principal de su estudio fue implementar un sistema de predicción de demanda para mejorar el proceso de planificación logística en el canal de comercio electrónico de electrodomésticos para un supermercado. El problema abordado por Morales se centró en los desafíos logísticos que enfrentan las empresas del sector retail en Perú debido al aumento de la demanda en el canal de comercio electrónico, que sus sistemas tradicionales no pueden soportar. Esto resultaba en una disminución del nivel de servicio, ventas perdidas y sobrecostos logísticos.

La investigación se justificó con el objetivo de optimizar el proceso logístico para el canal de ecommerce, especialmente en la categoría de productos importados. Para lograr sus objetivos, el autor utilizó un modelo de proyección de demanda innovador, que integra diversas técnicas estadísticas y considera el comportamiento del consumidor peruano. Se realizaron análisis exhaustivos de las frecuencias de compra y las repeticiones en las órdenes para obtener

información clave sobre los patrones de demanda. Como resultado, se obtuvo una efectividad del 93.78% al comparar la venta real con la venta proyectada, lo que demuestra que las desviaciones

de la demanda solo afectaron en un 6.22% a la proyección. Además, se logró una reducción del 66% en los costos de inventario por sobre stock en los almacenes especializados de e-commerce.

Las conclusiones del trabajo señalan que la implementación de la solución propuesta condujo a mejoras significativas en la eficiencia de costos, evitando la acumulación de inventario innecesario. También se observó un aumento en el fill rate y un nivel de servicio excepcional para los consumidores finales. El autor concluye que la planificación logística adecuada puede tener un impacto positivo en el canal de comercio electrónico de un supermercado, contribuyendo al crecimiento y mejora continua del sector en Perú.

De este trabajo de investigación, se utilizará como muestra para referir la metodología de predicción de demanda y la importancia de la optimización de recursos en el sector de consumo masivo.

En el año 2024, Balcazar Gonzales, Junior German, realizó la investigación titulada "Desarrollo de un sistema de recomendación y previsión de demanda de productos utilizando aprendizaje automático en la empresa Selk". Este trabajo de suficiencia profesional fue presentado en la Universidad Privada del Norte, en la Facultad de Ingeniería de Sistemas Computacionales en Lima, Perú. El objetivo principal del autor fue crear un sistema de previsión de demanda para optimizar el proceso de ventas y garantizar un stock mínimo eficiente en la gestión de inventarios en la empresa Selk. La problemática central que abordó el autor era el proceso manual y propenso a errores de consulta del historial de compras de los clientes. Este método manual dificultaba la toma de decisiones para ofrecer productos y generaba tiempos prolongados de abastecimiento en caso de no haber stock, lo que resultaba en insatisfacción y pérdida de clientes. El tipo de investigación fue aplicada, y el autor utilizó un enfoque de aprendizaje supervisado, seleccionando el algoritmo Random Forest para la predicción de la

demanda. Además, se emplearon algoritmos como Cosine Similarity y TfidfVectorizer para la recomendación de productos. El modelo fue evaluado utilizando métricas de rendimiento como el MAE y R2 Score. Los resultados demostraron que el modelo tuvo un rendimiento prometedor, mostrando su capacidad para anticipar la demanda y sugerir productos relevantes a los clientes.

Aunque el sistema no se implementó en producción debido a la falta de inversión, se dejó preparado para su futura implementación, lo que permitiría a la empresa mejorar su estrategia de ventas y la gestión de inventarios. Las conclusiones del estudio afirman que el prototipo desarrollado demuestra la viabilidad de utilizar el aprendizaje automático para mejorar la toma de decisiones en la gestión de inventarios y la personalización de ofertas.

De este trabajo de investigación, se utilizará como muestra para referir la aplicación de la inteligencia artificial para la previsión de demanda y su impacto en la gestión de recursos dentro de una empresa.

## Internacionales

En el año 2024, Karol Jamilet Sánchez Quijije, realizó la investigación titulada “Gestión de Demanda y Logística de Ventas en las Ferreterías de la Ciudad de Montecristi”. El objetivo principal del estudio fue analizar la gestión de demanda y la logística de ventas en las ferreterías de la ciudad de Montecristi. La investigación se basó en la problemática de una planificación y gestión deficiente que afectaba la disponibilidad de productos, el servicio al cliente y los procesos de pedidos. Para su investigación, la autora aplicó una metodología que incluyó encuestas a los clientes de las ferreterías. Los resultados mostraron que el 55% de los clientes percibía una mejora en la disponibilidad de productos, lo que indicaba que una gestión de demanda eficiente se traducía en una mejor experiencia de compra. Asimismo, un 26% de los

encuestados destacó la implementación de sistemas de pedidos anticipados como una forma de mejorar la gestión.

De este trabajo de investigación, se utilizará como muestra para referir la importancia de la gestión de demanda y su impacto directo en la mejora de la disponibilidad de productos y la satisfacción del cliente, factores cruciales para la planificación logística y el éxito de una empresa.

En el año 2025, Ana Sofia Aguirre Tafur, Alejandro Acosta Mejía y Jinna Lorena Rojas Casas, realizaron la investigación titulada “Desarrollo de una aplicación de gestión de inventarios e implementación de IA para predicción de ventas en la Cafetería Doeat”. Este proyecto fue elaborado en la Universidad EAN, Facultad de Ingeniería, en Bogotá D.C. El objetivo del proyecto fue desarrollar un prototipo de aplicación web para mejorar la gestión de inventarios y la predicción de ventas en la empresa Doeat. La problemática que enfrentaba la cafetería era la falta de un control eficiente de los ingredientes, lo que podía generar desperdicios o desabastecimiento de productos clave, y la toma de decisiones basada en la intuición en lugar de datos históricos. La aplicación permitiría un control preciso de los ingredientes, notificando la necesidad de reposición y anticipando la demanda del menú. Además, se buscaba optimizar la administración de insumos, reducir desperdicios y potenciar la oferta gastronómica del negocio.

De este trabajo de investigación, se utilizará como muestra para referir la aplicación de la inteligencia artificial para la predicción de ventas y su impacto en la gestión de inventarios y la optimización de recursos.

## Problemas Generales Relacionados

1. **Dependencia de Métodos de Pronóstico Simples o Desactualizados**

La predicción de la demanda constituye un proceso esencial en la gestión empresarial; sin embargo, muchos negocios aún dependen de métodos simples y tradicionales que no logran capturar adecuadamente la complejidad del mercado actual. Según Manrique (2025), las técnicas convencionales, como el promedio móvil o la regresión lineal, presentan limitaciones notorias en escenarios donde la demanda es altamente variable y estacional, generando resultados poco confiables para la planificación de inventarios. De manera similar, Huamán e Infantes (2024) sostienen que la falta de actualización metodológica en el análisis de ventas conduce a predicciones poco precisas, lo cual incrementa el riesgo de incurrir en costos por exceso de inventario o, por el contrario, en quiebres de stock.

Por su parte, el estudio de Naranjo (2023) resalta que una de las principales deficiencias de los modelos tradicionales radica en su escasa capacidad de adaptación frente a variables externas, como promociones, cambios de tendencia o factores económicos. Esta rigidez metodológica impide que las empresas se ajusten oportunamente a la dinámica del mercado, limitando la utilidad de los pronósticos como herramienta de toma de decisiones estratégicas.

Finalmente, los trabajos de investigación anteriores evidencian que, al no integrar factores adicionales como estacionalidad o comportamiento del consumidor, los métodos simples tienden a ofrecer un nivel de error elevado en las proyecciones.

## Ineficiencia en la Gestión de Inventarios

La gestión ineficiente de inventarios es uno de los problemas más recurrentes en empresas de consumo masivo y otros sectores, dado que afecta directamente la rentabilidad y el nivel de servicio al cliente. Según Manrique (2025), una deficiente planificación de compras provoca tanto exceso de stock que incrementa costos de almacenamiento y riesgo de

obsolescencia como quiebres de inventario que reducen la disponibilidad de productos en momentos clave. De manera similar, Huamán e Infantes (2024) señalan que la ausencia de herramientas automatizadas para gestionar el inventario impide a los negocios contar con un control actualizado y dinámico de sus recursos, lo que repercute en la toma de decisiones estratégicas.

Por otra parte, el estudio de Naranjo (2023) evidencia que una gestión ineficiente del inventario se vincula directamente con la falta de integración de datos relevantes en los modelos de predicción. Esta carencia genera desbalances constantes entre la oferta y la demanda, provocando pérdidas económicas y reduciendo la competitividad empresarial. En conjunto, estas investigaciones destacan que la gestión de inventarios sin apoyo en sistemas inteligentes limita la optimización de recursos y constituye una de las principales barreras para la sostenibilidad de las empresas en el largo plazo.

## Bajo Nivel de Precisión en las Predicciones

El nivel de precisión de los modelos de predicción de demanda es un factor determinante para la efectividad de la planificación empresarial. Sin embargo, múltiples investigaciones coinciden en que este aspecto continúa siendo una debilidad. Manrique (2025) afirma que los métodos tradicionales presentan altos márgenes de error debido a que no consideran adecuadamente la estacionalidad ni las fluctuaciones del mercado. En la misma línea, Huamán e Infantes (2024) sostienen que la falta de exactitud en los pronósticos repercute directamente en la toma de decisiones de abastecimiento, generando compras inadecuadas y comprometiendo la continuidad de las operaciones.

Asimismo, el trabajo de Naranjo (2023) enfatiza que uno de los principales retos para mejorar la precisión radica en la escasa incorporación de variables adicionales como promociones, comportamiento del consumidor o factores externos en los modelos utilizados. Esta limitación metodológica reduce la capacidad predictiva y conduce a estimaciones poco confiables para la planificación logística. De manera conjunta, estos autores ponen de manifiesto que la baja precisión de los pronósticos no solo impacta en el control de inventarios, sino que también afecta la optimización de los recursos y la satisfacción de los clientes, evidenciando la necesidad de migrar hacia modelos basados en técnicas de machine learning y redes neuronales.

## Capítulo 2

En este capítulo se define nuestro marco teórico, el cual sirve de sustento conceptual para nuestro proyecto. Asimismo, se define los antecedentes académicos que servirán para reforzar y afianzar algunos estudios previos que se utilizarán. Además, se establecen los fundamentos teóricos de las herramientas, tecnologías y conceptos útiles que utilizaremos en el desarrollo del proyecto. De este modo, el capítulo termina con la descripción de los procesos de negocio que maneja la empresa.

# MARCO TEÓRICO

Se concibe los fundamentos teóricos de las tecnologías, herramientas y conceptos que sustentan el proyecto, así como los antecedentes académicos bibliográficos que sirvieron para afianzar algunos conceptos que se aplican en el desarrollo de este proyecto.

## Fundamento Teórico

Para el desarrollo de este proyecto se utilizarán ciertas herramientas, tecnologías y se aplicarán algunos conceptos específicos. Los cuales se explican a continuación.

### Lenguaje De Programación Java.

Según Oracle (2010), Java es un lenguaje de desarrollo de propósito general y como tal es válido para realizar todo tipo de aplicaciones profesionales. Fue diseñado para poder funcionar en distintos tipos de procesadores y sistemas operativos, teniendo como lema “Write Once Run Everywhere”, que significa “Programa una vez, ejecútalo en todos lados”. De este modo, la diferencia principal de Java respecto a los demás lenguajes radica en que los programas creados por Java son independientes de la arquitectura del procesador. También, se pueden escribir aplicaciones para intra-redes, aplicaciones cliente servidor y programas distribuidos en redes locales e internet. Además, se remarca el hecho de que Java permite escribir Applets aunque esto

ya no es muy utilizado actualmente, debido a ciertas fallas de seguridad que suponía su implementación en el desarrollo web. Cabe mencionar, que para el desarrollo de este proyecto se utilizará Java en su versión 8. A continuación, se remarca aspectos teóricos fundamentales del lenguaje Java.

### Paradigma De Programación Orientado a Objetos.

Según IBM(2021), a principios de la década de 1980 como parte del desarrollo de la informática surge el paradigma de programación orientada a objetos, el cual se volvió muy popular entre los desarrolladores de la época. Dicho paradigma permitía organizar y estructurar los componentes de un sistema informático en clases y objetos, que a su vez tenían atributos y métodos. De modo que, se podía representar las entidades del mundo real en código a través de objetos, describir sus características como atributos y emular su comportamiento a través de métodos. Este tipo de programación se emplea para estructurar un programa informático en piezas simples y reutilizables de planos de código, las clases que representan a una entidad. De este modo, se puede crear instancias individuales de una clase, lo que se conoce como los objetos. La programación orientada a objetos permite que el código sea reutilizable y organizado. De manera que, evita el duplicado de código haciendo así al programa más eficiente. Asimismo, protege el acceso no deseado a los datos, evitando la exposición de piezas de código críticas.

Esto se puede lograr a través de los conceptos de la encapsulación y la abstracción.

### Commons Math

Apache Commons Math es una biblioteca de código abierto diseñada para ofrecer un conjunto amplio de herramientas matemáticas y estadísticas dentro del ecosistema Java. Esta biblioteca es especialmente útil para la construcción de modelos de predicción de demanda, ya que incluye métodos de regresión, interpolación, optimización y cálculos probabilísticos. Su

diseño modular permite integrar fácilmente funciones avanzadas en aplicaciones sin necesidad de implementar algoritmos desde cero. Además, su curva de aprendizaje es accesible para desarrolladores con conocimientos básicos de estadística y álgebra lineal. Distribuida bajo la licencia Apache 2.0, Commons Math es de uso libre tanto para proyectos académicos como empresariales, lo que la convierte en una solución práctica y económica para el análisis predictivo.

### DataVec

DataVec es una biblioteca de preparación y vectorización de datos para la JVM, diseñada para facilitar el preprocesamiento y transformación de datos en aplicaciones de machine learning. Su principal funcionalidad radica en convertir registros heterogéneos como archivos CSV o series temporales en vectores numéricos aptos para su análisis. Dentro del proyecto, permitirá normalizar, limpiar y estructurar los datos históricos de ventas antes de ser utilizados en los modelos predictivos. Aunque forma parte del ecosistema de DL4J, puede utilizarse de manera independiente para tareas de preparación de datos en proyectos que no requieran redes neuronales profundas. Al igual que otras librerías del stack, es de código abierto y distribuida bajo la licencia Apache 2.0.

### Commons CSV

Apache Commons CSV es una biblioteca ligera que facilita la lectura y escritura de archivos CSV en aplicaciones Java. En el contexto del sistema de predicción de demanda, será la herramienta clave para importar y exportar registros históricos de ventas, inventarios y órdenes de compra en un formato estructurado y ampliamente compatible con otras aplicaciones. Su API es sencilla, lo que reduce la complejidad en el manejo de grandes volúmenes de datos tabulares.

Además, permite trabajar con delimitadores personalizados, cabeceras y validaciones básicas, lo cual garantiza la integridad de la información procesada.

### Apache POI

Apache POI es una librería de código abierto que permite la creación, manipulación y lectura de documentos de Microsoft Office en aplicaciones Java. Para este proyecto, será utilizada principalmente en la generación de reportes de ventas y predicciones dentro de hojas de cálculo Excel, permitiendo a la empresa exportar y analizar información de forma práctica y familiar. Su versatilidad también permite la integración con Word y PowerPoint, ampliando las posibilidades de reportes y documentación. Al estar bajo licencia Apache 2.0, no requiere costos adicionales de implementación.

### JFreeChart

JFreeChart es una biblioteca especializada en la creación de gráficos bidimensionales para aplicaciones Java, compatible con entornos como Swing y JavaFX. En este sistema, será fundamental para la visualización de los resultados de predicción, mostrando tendencias, variaciones estacionales y comparaciones en tiempo real a través de gráficos de líneas, barras y áreas. Su capacidad de personalización permitirá diseñar dashboards interactivos que faciliten la interpretación de datos para la toma de decisiones estratégicas. Es una librería gratuita y de código abierto, lo que la convierte en una opción viable para proyectos académicos y empresariales.

### JasperReports

JasperReports es una de las bibliotecas de generación de reportes más completas en el entorno Java. Permite crear documentos en múltiples formatos (PDF, Excel, HTML, etc.) a partir de plantillas predefinidas y datos estructurados. Dentro del sistema de predicción de demanda,

JasperReports servirá para generar informes detallados que combinen texto, tablas y gráficos, consolidando la información en reportes profesionales para la gerencia. Su integración con JFreeChart amplía las capacidades gráficas de los reportes, brindando una visión más clara de las tendencias de demanda y las recomendaciones de compra.

## Antecedentes Locales

En el contexto peruano, la aplicación de técnicas de machine learning para la predicción de demanda y sistemas de recomendación representa un área de creciente interés, especialmente en el sector de distribución y comercio industrial.

El desarrollo de sistemas de recomendación híbridos, que combinan filtros colaborativos con enfoques basados en contenido, ha mostrado resultados prometedores en empresas de distribución. Balcázar (2024) señala que "el modelo mostró un rendimiento prometed or, con métricas de evaluación que evidencian su capacidad para anticipar la demanda y sugerir productos relevantes a los clientes" (p. 9), evidenciando la viabilidad de implementar herramientas de apoyo a la toma de decisiones en inventarios del sector industrial.

En cuanto a la aplicación de metodologías ágiles adaptadas, algunos proyectos nacionales han modificado marcos de trabajo como SCRUM para proyectos individuales de machine learning (Balcázar, 2024). Esto ha permitido estructurar los proyectos de inteligencia artificial de manera ordenada y adaptativa, especialmente en contextos empresariales con recursos limitados.

Asimismo, el uso de algoritmos de Random Forest ha sido empleado en estudios locales para el análisis predictivo de demanda en empresas medianas (Balcázar, 2024). Dichos enfoques han permitido a las organizaciones manejar grandes volúmenes de datos históricos y obtener predicciones robustas con un R² de 0.85.

Respecto a los métodos de evaluación, en el contexto peruano se ha optado por métricas como RMSE, MAE y R² Score para validar la precisión de los modelos predictivos (Balcázar, 2024). Finalmente, en el ámbito de los sistemas de recomendación, se han empleado métricas de precisión y cobertura para evaluar la efectividad, alcanzando valores de 85% y 81% respectivamente (Balcázar, 2024).

A nivel latinoamericano, diversos autores han desarrollado propuestas en predicción de demanda e inventarios. En Colombia, investigaciones como la de Morales Rodríguez et al. (2025) destacan que la predicción de precios y demanda mediante modelos de series temporales y machine learning es clave para la reducción de la volatilidad y optimización de recursos en sectores como el agrícola y alimentario.

La Metodología CRISP-DM también ha sido utilizada como marco de referencia para proyectos de análisis predictivo, al estructurar el flujo desde la extracción de datos hasta la implementación de resultados (Morales Rodríguez et al., 2025).

Los estudios de series temporales aplicados a sectores como la cafetería universitaria y el mercado agropecuario en la región han demostrado que estas técnicas permiten capturar patrones de consumo y estacionalidad, proyectando escenarios futuros con mayor precisión (Aguirre Tafur et al., 2025).

Por otro lado, los métodos estadísticos clásicos (como SARIMA) se han consolidado como herramientas de fácil implementación para empresas emergentes que no cuentan con infraestructura tecnológica avanzada, aunque se ha evidenciado que modelos de machine learning como árboles de decisión y boosting ofrecen mejor desempeño en predicciones complejas (Morales Rodríguez et al., 2025).

Finalmente, en la región se destacan trabajos que validan los modelos a través de métricas como RMSE, MAE, y MAPE, garantizando la confiabilidad de los pronósticos generados y permitiendo una mejor toma de decisiones en la gestión de inventarios y precios (Morales Rodríguez et al., 2025; Aguirre Tafur et al., 2025).

## Internacionales

En el ámbito internacional, la literatura es extensa y variada. La predicción de demanda ha sido ampliamente estudiada en sectores como retail, energía y manufactura. Según Calvo Martucci (2024), este proyecto aborda la complejidad de la gestión del inventario mediante la creación de un modelo predictivo para estimar con mayor precisión la demanda y facilitar la gestión del stock.

Con relación a la Metodología CRISP-DM, esta se ha convertido en el estándar de facto en proyectos de minería de datos, adoptada globalmente por su carácter flexible y adaptable (Calvo Martucci, 2024). En este proyecto, se sigue esta metodología, que es iterativa y ayuda a refinar continuamente el enfoque de los datos y mejorar los resultados progresivamente.

Las series temporales y su modelado se encuentran entre los enfoques más utilizados internacionalmente, especialmente con variantes de ARIMA y SARIMA para demandas estacionales (Calvo Martucci, 2024). En este proyecto, se utilizan series temporales, regresión, árboles de decisión o redes neuronales para la predicción.

Aunque algunos países avanzan en redes neuronales, muchas empresas optan por métodos estadísticos clásicos por su menor costo computacional y facilidad de implementación (Calvo Martucci, 2024). En este proyecto, se utilizan algoritmos como Naive, Random Forest, Decision Tree, LightGBM, XGBoost y LSTM.

Finalmente, las métricas MAE y WMAPE son de uso común a nivel global, lo que permite la comparación estandarizada entre diferentes modelos y contextos (Calvo Martucci, 2024). En este proyecto, se utilizan métricas como EVS, MSE y RMSE para evaluar la precisión de los modelos predictivos.

## Descripción de los Procesos de Negocio

La empresa Global Market S.A.C., dedicada al rubro de consumo masivo, desarrolla sus operaciones a través de procesos clave que sostienen su funcionamiento y competitividad en el mercado. Entre ellos, se encuentra el proceso de gestión de inventario, el cual permite registrar, controlar y actualizar los productos disponibles en almacén, garantizando un adecuado seguimiento de las existencias. Este proceso resulta fundamental para evitar quiebres de stock o excesos de mercancía que afectan directamente la rentabilidad del negocio.

Otro proceso esencial es el proceso de ventas, encargado de registrar las transacciones realizadas con los clientes. La información generada en este flujo alimenta la base de datos histórica que será utilizada posteriormente por el sistema de predicción de demanda. De esta manera, las ventas no solo cumplen con la función de generar ingresos, sino que se convierten en la fuente principal de información para la toma de decisiones estratégicas.

El proceso de abastecimiento constituye otro eje central en las operaciones de la empresa, ya que involucra la gestión de proveedores y la generación de órdenes de compra. Una adecuada coordinación en este ámbito es indispensable para reponer el inventario en el momento preciso, evitando retrasos o sobrecostos.

Finalmente, se encuentra el proceso de análisis de datos, que permitirá evaluar patrones de venta y aplicar modelos de predicción para anticipar la demanda futura. Este proceso,

potenciado con la implementación del sistema propuesto, contribuirá a fortalecer la planificación y optimización de recursos en la organización.

## Capítulo 3

En este capítulo se definen los requerimientos funcionales y no funcionales del proyecto, continua con la presentación de las soluciones propuestas a las problemáticas de la empresa. Para después presentar los prototipos que se han planteado para cada solución propuesta.

## Requerimientos Funcionales.

**Tabla 2**

*Lista de requerimientos funcionales del sistema con su nivel de prioridad establecida.*

**Código Requerimiento funcional Prioridad RF001** El sistema permite al administrador registrar usuarios. **Media**

**RF002** El sistema permite la autenticación de usuarios. **Alta**

# RF003

El sistema permite el registro e importación de datos externos. (Excel/CSV)

**Alta**

# RF004

El sistema debe mostrar un panel de control principal tipo dashboard.

**Alta**

# RF005

El sistema permite gestionar la variedad de productos de la empresa.

**Media**

# RF006

El sistema debe realizar varios tipos de análisis predictivos de la demanda.

**Alta**

# RF007

El sistema debe mostrar visualizaciones gráficas de los diversos análisis de datos efectuados.

**Alta**

# RF008

El sistema permite actualizar los datos históricos de ventas y demandas de productos.

**Media**

# RF009

El sistema permite generar informes en PDF sobre los análisis efectuados.

## Alta

***Nota.*** Se describe los requerimientos funcionales que se tendrán en cuenta durante el desarrollo del proyecto. Están con su respectivo nivel de prioridad establecido.

## Requerimientos No Funcionales Tabla 3

*Lista de Requerimientos no funcionales del proyecto.*

## Código Requerimiento no funcional Descripción

El sistema debe ser capaz de procesar

**RNF01** Garantizar rendimiento.

grandes cantidades de datos y realizar predicciones en un tiempo razonable.

**RNF02** Asegurar escalabilidad

El sistema sea escalable para adaptarse a crecientes cantidades de datos y usuarios.

**RNF03** Diseñar interfaz intuitiva

Diseñar una interfaz gráfica fácil de usar y entender para los usuarios finales.

# RNF04

Implementar seguridad de datos

Implementa medidas de seguridad para proteger los datos y prevenir accesos no autorizados.

**RNF05** Garantizar compatibilidad

El sistema será compatible con diferentes sistemas operativos de escritorio.

Implementar buenas prácticas de

**RNF06** Diseño modular en capas

programación orientada a objetos, facilitando la escalabilidad y mantenimiento.

Mantener el código fuente bien

**RNF07** Documentar el código

documentado y con un estilo de programación consistente.

El sistema debe ser capaz de gestionar

**RNF08** Controlar diversos errores

errores de entrada de datos y fallas de conexión a la base de datos sin pérdida de información.

## Desarrollo de la Solución

### Solución a Dependencia de Métodos Manuales y Subjetivos

Para superar esta problemática, se propone el desarrollo de un sistema de escritorio de predicción de demanda para la optimización de recursos, diseñado específicamente para la empresa Global Market S.A.C. Este sistema, a través de modelos predictivos avanzados y técnicas de análisis de datos, permitirá anticipar con precisión el comportamiento de la demanda, incluyendo variaciones estacionales y eventos especiales.

La implementación de esta herramienta permitirá a la empresa abandonar las decisiones basadas en la intuición y fundamentarlas en datos precisos. Con ello, se busca mejorar la

planificación del inventario, reducir los costos asociados con el exceso o la escasez de productos, y lograr una distribución más eficiente de los recursos financieros y logísticos, fortaleciendo así la posición competitiva de la empresa.

### Solución a Dificultad para Predecir la Demanda

Desarrollar un modelo de predicción de predicción que pueda predecir la demanda futura de productos con alta precisión, permitiendo a la empresa contar con una visión clara y anticipada de las necesidades operativas, lo que posibilitará una mejor planificación de compras, personal y logística. Gracias a los modelos predictivos, la organización podrá distribuir sus recursos de manera más eficiente, evitar sobrecostos y garantizar una respuesta oportuna ante la variabilidad del mercado. Esto transformará la gestión de recursos en un proceso estratégico, pasando de un enfoque reactivo a uno proactivo y basado en datos confiables.

### Solución a Ineficiencia en la Gestión de Inventarios

Implementar un sistema de gestión de inventarios que permita a la empresa gestionar sus inventarios de manera eficiente.

### Solución a Falta de Información en Tiempo Real

Crear un panel dashboard que muestre información en tiempo real sobre la demanda y el stock de productos.

## Diseño de Prototipos

### Prototipo 1

Se diseña una interfaz simple para el login de los usuarios la cuál permite también

realizar el registro de usuarios. De este modo, luego de efectuada la autenticación de credenciales se procede a mostrar la ventana principal del aplicativo siendo que se muestra la vista del panel

de control principal del sistema, en donde se encuentran los resúmenes del rendimiento de productos de la empresa, gráficos estadísticos de tendencia central de la demanda en tiempo real y también se mostrará una tabla con información relevante acerca de la métricas clave. Cabe resaltar, que todos estos datos se obtienen en base a los históricos que el usuario debe importar la primera vez que ingresa al sistema.

## Figura 1

*Pantalla de login del sistema y vista del panel de control principal.*

*![](data:image/png;base64...)*

**Fuente:** Elaboración propia.

El diseño contempla un esquema por pestañas en donde el usuario puede desplazarse de una sección a otra de forma fácil e intuitiva, en la pestaña de productos se podrá mantener una

gestión de estos y llevar a cabo varios tipos de análisis en base a los datos proporcionados. Este apartado cuenta además de una vista general del listado de productos junto a sus gráficos estadísticos, una subsección personalizada para cada producto que se quiera llevar a cabo el análisis respectivo en donde también se podrá realizar la exportación de este en formato Excel o CSV.

## Figura 2

*Pantalla de la vista productos, detalles y exportación de consulta de datos del sistema.*

*![](data:image/jpeg;base64...)*

**Fuente:** Elaboración propia.

Luego se tiene al panel de informes el cual permite crear varios tipos de reportes detallados sobre la demanda de productos, las previsiones y las recomendaciones a tener en cuenta en base al análisis efectuado. Todos los aspectos del reporte se podrán configurar desde

aquí, siendo desde la asignación del nombre del informe, tipo en base a una plantilla precargada o crear un formato propio, así también se puede configurar el periodo de tiempo, región y diversas métricas a tener en cuenta en el reporte, desde apartado se puede descargar el reporte en formato PDF.

## Figura 3

*Pantalla de la vista del panel de informes del sistema.*

*![](data:image/png;base64...)*

**Fuente:** Elaboración propia.

En la siguiente pantalla se muestra la configuración del sistema en donde cada usuario podrá configurar sus credenciales de acceso de su cuenta, así también se puede modificar aspectos estéticos funcionales de la interfaz de visualización de datos como el tipo de moneda y el formato de fecha.

## Figura 4

*Pantalla de configuración del sistema.*

*![](data:image/png;base64...)*

**Fuente:** Elaboración propia.

### Prototipo 2

Para el segundo prototipo se enfatizó la accesibilidad a cada sección ante todo siendo que existe una barra superior que permite al usuario moverse de un panel a otro.

## Figura 5

*Pantalla de inicio en donde se muestra el dashboard principal del sistema.*

*![](data:image/png;base64...)*

**Fuente:** Elaboración propia.

En esta vista se llevará a cabo una gestión de los productos de la empresa y también se podrá efectuar el análisis personalizado a cada producto seleccionado.

## Figura 6

*Pantalla de análisis de rendimiento de producto.*

*![Interfaz de usuario gráfica, Aplicación  El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)*

**Fuente:** Elaboración propia.

Pantalla de la subsección anterior de análisis de rendimiento de producto, aquí se podrá indicar los parámetros para tener en cuenta en la obtención de una predicción para el producto seleccionado.

## Figura 7

*Pantalla de análisis de predicción de demanda por producto.*

*![](data:image/png;base64...)*

**Fuente:** Elaboración propia.

La siguiente pantalla pertenece a la vista reportes, en donde el usuario podrá generar hasta 3 tipos de reportes generales de los productos que tenga administrados la empresa, se puede configurar el tipo y periodo de tiempo del informe, el sistema exporta los informes en formato PDF.

## Figura 8

*Pantalla de vista reportes del sistema.*

![](data:image/png;base64...)

**Fuente:** Elaboración propia.

### Prototipo 3

Para el tercer prototipo se tomo como referencia el estilo de un sistema web, de modo que sea más compacto el diseño teniendo una barra de navegación lateral estática que permanece

para todas las secciones del aplicativo.

## Figura 9

*Pantalla de login y vista del dashboard principal.*

![](data:image/jpeg;base64...)

**Fuente:** Elaboración propia.

En la siguiente pantalla se llevará a cabo la gestión de productos registrados en el sistema, puesto que tiene una barra de búsqueda junto a filtros para mayor disección de datos así como una tabla que muestra los datos encontrados, además esta pantalla acompaña con dos gráficos estadísticos que mostrarán detalles sobre los productos consultados para mayor manejo de decisiones.

## Figura 10

*Pantalla de gestión de productos.*

![](data:image/jpeg;base64...)

**Fuente:** Elaboración propia.

Luego se tiene a la pantalla de la vista reportes, la cual permite generar varios tipos de informes de análisis de rendimiento de productos y de la demanda de estos, así también permite la selección de un informe personalizado con datos seleccionados por el usuario. Todos los informes se generan en formato PDF.

## Figura 11

*Pantalla de vista de reportes del sistema.*

![](data:image/jpeg;base64...)

**Fuente:** Elaboración propia.

Por último, se muestra la pantalla de ajustes del sistema en donde además de que cada usuario podrá gestionar sus credenciales de acceso, se encuentran algunas configuraciones puntuales para el uso del sistema, tales como cambiar el tipo de moneda y el formato de fecha que se muestran en la UI. También esta la sección más importante del aplicativo, la cuál es la importación de datos al sistema, en donde mediante un archivo Excel o CSV el usuario puede importar esta información, de modo que se utilice en la aplicación.

## Figura 12

*Pantalla de ajustes del sistema.*

*![](data:image/jpeg;base64...)*

**Fuente:** Elaboración propia.

## Diagrama de Casos de Uso

Para el proyecto se prevé a dos actores o usuarios que van a interactuar con el sistema, los cuales son el administrador o gerente de logística, que tendrá acceso total al sistema y el operador logístico, quien tendrá acceso limitado como analista en el sistema.

## Tabla 4

*Distribución de los casos de uso definidos.*

|  |  |  |
| --- | --- | --- |
| **Código** | **Caso de Uso** | **Descripción** |
| CU-01 | Registrar usuario | Solo disponible para Administrador |
| CU-02 | Autenticar usuario | Obligatorio para acceder al sistema. |
| CU-03 | Visualizar dashboard | Punto de acceso principal luego de |
|  |  | autenticación. |
| CU-04 | Gestionar productos CRUD | Crear, leer, actualizar y eliminar productos. |
| CU-05 | Importar datos históricos | Desde archivos Excel/CSV. |
| CU-06 | Actualizar datos históricos | Modificación posterior a la importación. |
| CU-07 | Realizar análisis predictivo | Depende de que haya datos históricos cargados. |
| CU-08 | Visualizar análisis | Requiere que exista un análisis previamente |
| CU-09 | Generar reportes en PDF | realizado.  Puede extender la visualización de análisis. |

## Figura 13

*Diagrama de Casos de Uso.*

![](data:image/png;base64...)

**Fuente:** Elaboración propia.

## Diagrama de Clases

Para el desarrollo del proyecto de software se propone una arquitectura en capas en donde se tenga distribuido las distintas responsabilidades del funcionamiento del sistema en paquetes independientes de manera que se asegura la escalabilidad y mantenimiento a largo plazo.

## Figura 14

*Diagrama de clases para el desarrollo del software.*

## 49

**![PlantUML diagram](data:image/png;base64...)**

**Fuente:** Elaboración propia.

## Modelo de Datos

Para el desarrollo del sistema se eligió la base de datos relacional PostgreSQL, en donde de acuerdo con el contexto de negocio se han definido 8 entidades, las cuales corresponderán a tablas contenedoras de datos para el funcionamiento del software.

### Conceptual

## Figura 15

*Diagrama Entidad Relación – Modelo Conceptual.*

*![Gráfico, Gráfico de cajas y bigotes  El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)*

**Fuente:** Elaboración propia.

### Lógico

## Figura 16

*Diagrama Entidad Relación – Modelo Lógico*

![](data:image/jpeg;base64...)

**Fuente:** Elaboración propia.

### Físico

## Figura 17

## ![Imagen que contiene Escala de tiempo El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)

## ![Tabla El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)

**![Tabla  El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)**

**![Imagen que contiene Tabla  El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)**

**![Tabla  El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)**

*Diagrama Entidad Relación – Modelo Físico*

**Fuente:** Elaboración propia.

## Diccionario de Datos

Para el modelo de datos planteado para el desarrollo del sistema es necesario establecer un diccionario de datos para una mayor gestión y comprensión del tipo de información a almacenar en la base de datos, se va a utilizar PostgreSQL como SGBD.

**Tabla: alertas\_inventario**

Número de columnas: 14

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| cantidad\_sugerida | integer(32) | Sí |  | - | - |
| id\_producto | integer(32) | Sí |  | - | - |
| id\_usuario\_asignado | integer(32) | Sí |  | - | - |
| stock\_actual | integer(32) | Sí |  | - | - |
| stock\_minimo | integer(32) | Sí |  | - | - |
| fecha\_generacion | timestamp without time zone | No |  | - | - |
| fecha\_resolucion | timestamp without time zone | Sí |  | - | - |
| id\_alerta | bigint(64) | No | PK | - | - |
| accion\_tomada | character varying(500) | Sí |  | - | - |
| mensaje | character varying(500) | No |  | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| estado | character varying(255) | Sí |  | - | CHECK: ((estado) = ANY ((ARRAY['PENDIENTE', 'EN\_PROCES... |
| nivel\_criticidad | character varying(255) | No |  | - | CHECK: ((nivel\_criticidad) = ANY ((ARRAY['BAJA', 'MEDI... |
| tipo\_alerta | character varying(255) | No |  | - | CHECK: ((tipo\_alerta) = ANY ((ARRAY['STOCK\_BAJO', 'PUN... |

**Tabla: calculo\_optimizacion**

Número de columnas: 17

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| costo\_total\_inventario | numeric(10,2) | Sí |  | - | - |
| demanda\_anual\_estimada | integer(32) | Sí |  | - | - |
| eoq\_cantidad\_optima | integer(32) | Sí |  | - | - |
| id\_calculo | integer(32) | No | PK | - | - |
| id\_producto | integer(32) | Sí | FK ? productos.id\_producto | - | - |
| rop\_punto\_reorden | integer(32) | Sí |  | - | - |
| stock\_seguridad\_sugerido | integer(32) | Sí |  | - | - |
| fecha\_calculo | timestamp without time zone | Sí |  | - | - |
| costo\_mantenimiento | numeric(10,2) | Sí |  | - | - |
| costo\_pedido | numeric(10,2) | Sí |  | - | - |
| costo\_unitario | numeric(10,2) | Sí |  | - | - |
| dias\_entre\_lotes | integer(32) | Sí |  | - | - |
| dias\_lead\_time | integer(32) | Sí |  | - | - |
| fecha\_actualizacion | timestamp without time zone | Sí |  | - | - |
| numero\_ordenes\_anuales | integer(32) | Sí |  | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| stock\_seguridad | integer(32) | Sí |  | - | - |

**Tabla: categorias**

Número de columnas: 2

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| id\_categoria | integer(32) | No | PK | - | - |
| nombre | character varying(255) | No |  | - | - |

**Tabla: detalle\_orden\_compra**

Número de columnas: 8

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| cantidad\_recibida | integer(32) | Sí |  | - | - |
| cantidad\_solicitada | integer(32) | No |  | - | - |
| id\_producto | integer(32) | No | FK ? productos.id\_producto | - | - |
| precio\_unitario | numeric(10,2) | No |  | - | - |
| subtotal | numeric(12,2) | Sí |  | - | - |
| id\_detalle | bigint(64) | No | PK | - | - |
| id\_orden\_compra | bigint(64) | No | FK ? ordenes\_compra.id\_orden\_compra | - | - |
| observaciones | character varying(255) | Sí |  | - | - |

**Tabla: estacionalidad\_producto**

Número de columnas: 10

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| anio\_referencia | integer(32) | Sí |  | - | - |
| demanda\_maxima | integer(32) | Sí |  | - | - |
| demanda\_minima | integer(32) | Sí |  | - | - |
| demanda\_promedio\_historica | integer(32) | Sí |  | - | - |
| factor\_estacional | numeric(5,2) | Sí |  | - | - |
| id\_producto | integer(32) | No | FK ? productos.id\_producto | - | - |
| mes | integer(32) | No |  | - | - |
| id\_estacionalidad | bigint(64) | No | PK | - | - |
| observaciones | character varying(300) | Sí |  | - | - |
| descripcion\_temporada | character varying(255) | Sí |  | - | - |

**Tabla: importaciones\_datos**

Número de columnas: 14

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| id\_usuario | integer(32) | Sí | FK ? usuarios.id\_usuario | - | - |
| registros\_exitosos | integer(32) | Sí |  | - | - |
| registros\_fallidos | integer(32) | Sí |  | - | - |
| registros\_procesados | integer(32) | Sí |  | - | - |
| fecha\_importacion | timestamp without time zone | No |  | - | - |
| id\_importacion | bigint(64) | No | PK | - | - |
| tiempo\_procesamiento\_ms | bigint(64) | Sí |  | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| errores | character varying(2000) | Sí |  | - | - |
| estado\_importacion | character varying(255) | Sí |  | - | CHECK: ((estado\_importacion) = ANY ((ARRAY['EN\_PROCESO... |
| nombre\_archivo | character varying(255) | Sí |  | - | - |
| ruta\_archivo | character varying(255) | Sí |  | - | - |
| tipo\_datos | character varying(255) | No |  | - | CHECK: ((tipo\_datos) = ANY ((ARRAY['PRODUCTOS', 'INVEN... |
| fecha\_actualizacion | timestamp without time zone | Sí |  | - | - |

**Tabla: inventario**

Número de columnas: 14

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| dias\_sin\_venta | integer(32) | Sí |  | - | - |
| id\_inventario | integer(32) | No | PK | - | - |
| id\_producto | integer(32) | Sí | FK ? productos.id\_producto | - | - |
| punto\_reorden | integer(32) | Sí |  | - | - |
| stock\_disponible | integer(32) | No |  | - | - |
| stock\_en\_transito | integer(32) | Sí |  | - | - |
| stock\_maximo | integer(32) | Sí |  | - | - |
| stock\_minimo | integer(32) | No |  | - | - |
| stock\_reservado | integer(32) | Sí |  | - | - |
| fecha\_ultima\_actualizacion | timestamp without time zone | Sí |  | - | - |
| fecha\_ultimo\_movimiento | timestamp without time zone | Sí |  | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| estado | character varying(255) | Sí |  | - | CHECK: ((estado) = ANY ((ARRAY['NORMAL', 'BAJO', 'CRIT... |
| ubicacion\_almacen | character varying(255) | Sí |  | - | - |

**Tabla: kardex**

Número de columnas: 19

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| cantidad | integer(32) | No |  | - | - |
| costo\_unitario | numeric(10,2) | Sí |  | - | - |
| id\_producto | integer(32) | No | FK ? productos.id\_producto | - | - |
| id\_proveedor | integer(32) | Sí | FK ? proveedores.id\_proveedor | - | - |
| id\_usuario | integer(32) | Sí | FK ? usuarios.id\_usuario | - | - |
| saldo\_cantidad | integer(32) | No |  | - | - |
| fecha\_movimiento | timestamp without time zone | No |  | - | - |
| fecha\_registro | timestamp without time zone | No |  | - | - |
| fecha\_vencimiento | timestamp without time zone | Sí |  | - | - |
| id\_kardex | bigint(64) | No | PK | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| lote | character varying(255) | Sí |  | - | - |
| motivo | character varying(255) | Sí |  | - | - |
| numero\_documento | character varying(255) | Sí |  | - | - |
| referencia | character varying(255) | Sí |  | - | - |
| tipo\_documento | character varying(255) | Sí |  | - | - |
| tipo\_movimiento | character varying(255) | No |  | - | CHECK: ((tipo\_movimiento) = ANY ((ARRAY['ENTRADA\_COMPR... |
| ubicacion | character varying(255) | Sí |  | - | - |
| anulado | boolean | No |  | - | - |

**Tabla: ordenes\_compra**

Número de columnas: 12

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| fecha\_entrega\_esperada | date | Sí |  | - | - |
| fecha\_entrega\_real | date | Sí |  | - | - |
| fecha\_orden | date | No |  | - | - |
| generada\_automaticamente | boolean | Sí |  | - | - |
| id\_proveedor | integer(32) | Sí | FK ? proveedores.id\_proveedor | - | - |
| id\_usuario | integer(32) | Sí | FK ? usuarios.id\_usuario | - | - |
| total\_orden | numeric(12,2) | Sí |  | - | - |
| fecha\_registro | timestamp without time zone | Sí |  | - | - |
| id\_orden\_compra | bigint(64) | No | PK | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| estado\_orden | character varying(255) | Sí |  | - | CHECK: ((estado\_orden) = ANY ((ARRAY['BORRADOR', 'PEND... |
| numero\_orden | character varying(255) | No |  | - | - |

**Tabla: parametro\_algoritmo**

Número de columnas: 10

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| id\_parametro | integer(32) | No | PK | - | - |
| valor\_parametro | numeric(10,2) | Sí |  | - | - |
| nombre\_parametro | character varying(255) | Sí |  | - | - |
| tipo\_algoritmo | character varying(255) | Sí |  | - | - |
| activo | boolean | Sí |  | - | - |
| descripcion | character varying(500) | Sí |  | - | - |
| fecha\_actualizacion | timestamp without time zone | Sí |  | - | - |
| fecha\_creacion | timestamp without time zone | Sí |  | - | - |
| valor\_maximo | numeric(10,2) | Sí |  | - | - |
| valor\_minimo | numeric(10,2) | Sí |  | - | - |

**Tabla: prediccion**

Número de columnas: 9

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| demanda\_predicha\_total | integer(32) | Sí |  | - | - |
| horizonte\_tiempo | integer(32) | Sí |  | - | - |
| id\_parametro | integer(32) | Sí | FK ? parametro\_algoritmo.id\_parametro | - | - |
| id\_prediccion | integer(32) | No | PK | - | - |
| id\_producto | integer(32) | Sí | FK ? productos.id\_producto | - | - |
| id\_usuario | integer(32) | Sí | FK ? usuarios.id\_usuario | - | - |
| metricas\_error | numeric(38,2) | Sí |  | - | - |
| fecha\_ejecucion | timestamp without time zone | Sí |  | - | - |
| algoritmo\_usado | character varying(255) | Sí |  | - | - |

**Tabla: productos**

Número de columnas: 10

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| costo\_adquisicion | numeric(10,2) | Sí |  | - | - |
| costo\_mantenimiento | numeric(10,2) | Sí |  | - | - |
| costo\_mantenimiento\_anual | numeric(10,2) | Sí |  | - | - |
| costo\_pedido | numeric(10,2) | Sí |  | - | - |
| dias\_lead\_time | integer(32) | Sí |  | - | - |
| id\_categoria | integer(32) | Sí | FK ? categorias.id\_categoria | - | - |
| id\_producto | integer(32) | No | PK | - | - |
| id\_um | integer(32) | Sí | FK ? unidad\_medida.id\_um | - | - |
| fecha\_registro | timestamp without time zone | Sí |  | - | - |
| nombre | character varying(255) | Sí |  | - | - |

**Tabla: proveedores**

Número de columnas: 16

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| calificacion | numeric(3,2) | Sí |  | - | - |
| dias\_credito | integer(32) | Sí |  | - | - |
| estado | boolean | Sí |  | - | - |
| id\_proveedor | integer(32) | No | PK | - | - |
| tiempo\_entrega\_dias | integer(32) | Sí |  | - | - |
| fecha\_registro | timestamp without time zone | Sí |  | - | - |
| observaciones | character varying(500) | Sí |  | - | - |
| ciudad | character varying(255) | Sí |  | - | - |
| direccion | character varying(255) | Sí |  | - | - |
| email | character varying(255) | Sí |  | - | - |
| nombre\_comercial | character varying(255) | Sí |  | - | - |
| pais | character varying(255) | Sí |  | - | - |
| persona\_contacto | character varying(255) | Sí |  | - | - |
| razon\_social | character varying(255) | No |  | - | - |
| ruc\_nit | character varying(255) | Sí |  | - | - |
| telefono | character varying(255) | Sí |  | - | - |

**Tabla: registro\_demanda**

Número de columnas: 6

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| cantidad\_historica | integer(32) | Sí |  | - | - |
| id\_producto | integer(32) | Sí | FK ? productos.id\_producto | - | - |
| id\_registro | integer(32) | No | PK | - | - |
| id\_usuario | integer(32) | Sí | FK ? usuarios.id\_usuario | - | - |
| fecha\_registro | timestamp without time zone | Sí |  | - | - |
| periodo\_registro | character varying(255) | Sí |  | - | - |

**Tabla: unidad\_medida**

Número de columnas: 3

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| id\_um | integer(32) | No | PK | - | - |
| abreviatura | character varying(255) | Sí |  | - | - |
| nombre | character varying(255) | Sí |  | - | - |

**Tabla: usuarios**

Número de columnas: 5

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| Columna | Tipo de Dato | Nulo | Clave | Default | Restricciones |
| id\_usuario | integer(32) | No | PK | - | - |
| clave\_hash | character varying(255) | Sí |  | - | - |
| email | character varying(255) | Sí |  | - | - |
| nombre | character varying(255) | Sí |  | - | - |
| rol | character varying(255) | Sí |  | - | - |

## Bibliografía

Balcazar Gonzales, J. G. (2024). Desarrollo de un sistema de recomendación y previsión de demanda de productos utilizando aprendizaje automático en la empresa Selk [Trabajo de suficiencia profesional, Universidad Privada del Norte]. Repositorio de la Universidad Privada del Norte. <https://hdl.handle.net/11537/38781>

Morales Quispe, J. D. (2023). Aplicación de un sistema de predicción de demanda para la mejora del proceso de planificación logística en la operación E-commerce de electrodomésticos para un supermercado. Repositorio Institucional Universidad Nacional de Ingeniería. <http://hdl.handle.net/20.500.14076/27104>

Sánchez Quijije, K. J. (2025). Gestión de demanda y logística de ventas en las ferreterías de la ciudad de Montecristi [Tesis de licenciatura, Universidad Estatal del Sur de Manabí]. Repositorio Institucional UNESUM. [https://repositorio.unesum.edu.ec/handle/53000/7605](https://repositorio.unesum.edu.ec/handle/53000/7605?utm_source=chatgpt.com)

Aguirre Tafur, A, Acosta Mejía, A y Rojas Casas, J. (2025). Desarrollo de una aplicación de gestión de inventarios e implementación de IA para predicción de ventas en la Cafetería Doeat. Disponible en: <https://hdl.handle.net/10882/14960>

Manrique Rodriguez D.S. (2025). Machine learning para la gestión de medicamentos en el hospital José Angulo Tello de Chosica. [Tesis para optar el Título Profesional de Ingeniero de Sistemas e Informática, Universidad Tecnológica del Perú]. Repositorio de la UTP. <https://hdl.handle.net/20.500.12867/11476>

Huaman, P., & Infantes, O. E. (2024). Técnicas de predicción aplicadas al análisis del precio del oro: un enfoque comparativo [Artículo científico de licenciatura, Universidad Privada del Norte]. Repositorio de la Universidad Privada del Norte. <https://hdl.handle.net/11537/38247>

Naranjo San Martin J.E. (2023). Evaluación de la incorporación de nuevas variables en la predicción de demanda para una empresa de alimentos. [Tesis Postgrado, Universidad de Chile]. Repositorio de la UC. <https://repositorio.uchile.cl/handle/2250/198603>

(2025-06) Pronóstico de precios de aguacate Hass y papelillo en Colombia mediante métodos de series temporales. Recuperado de: <https://hdl.handle.net/20.500.12495/14957>

Calvo Martucci S. (2024). Previsión de demanda mediante técnicas de machine learning. [, Universitat Oberta de Catalunya (UOC)]. [https://openaccess.uoc.edu/server/api/core/bitstreams/6c632ac4-6756-47a2-b257-](https://openaccess.uoc.edu/server/api/core/bitstreams/6c632ac4-6756-47a2-b257-b113d63d07a5/content)

[b113d63d07a5/content](https://openaccess.uoc.edu/server/api/core/bitstreams/6c632ac4-6756-47a2-b257-b113d63d07a5/content)

IBM (17 de Agosto de 2021). Programación Orientada a Objetos. Ibm.com. <https://www.ibm.com/docs/es/spss-modeler/SaaS?topic=language-object-oriented-programming>

Oracle (2010). Estructura arquitectónica de Java SE. docs.oracle.com. <https://docs.oracle.com/cd/E19528-01/820-0888/6ncjkpnh8/index.html>

**60**

# ANEXOS

## Anexo 1: Diagrama de Gantt Figura 18

*Cronograma de Actividades del proyecto*

*![Gráfico  El contenido generado por IA puede ser incorrecto.](data:image/jpeg;base64...)*

***Fuente:*** *Elaboración propi*

## Anexo 2: Work Breakdown Structure Figura 19

*Planificación y desarrollo del proyecto a través de un WBS.*

*![Diagrama  El contenido generado por IA puede ser incorrecto.](data:image/png;base64...)*

**Fuente:** Elaboración propia.

## Anexo 3: Project Charter Figura 20

*Carta de Proyecto especificada.*

![Tabla  El contenido generado por IA puede ser incorrecto.](data:image/jpeg;base64...)

**Fuente:** Elaboración propia.

## Anexo 4: Lean Canvas Figura 21

*![Escala de tiempo  El contenido generado por IA puede ser incorrecto.](data:image/jpeg;base64...)Gráfico del Lean Canvas para el proyecto.*

**Fuente:** Elaboración propia.

## Anexo 5: Diagrama de Procesos BPM Figura 22

*Diagrama de Procesos de Negocio del Sistema.*

*![](data:image/jpeg;base64...)*
