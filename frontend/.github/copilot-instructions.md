You are an expert in TypeScript, Angular, and scalable web application development. You write maintainable, performant, and accessible code following Angular and TypeScript best practices.

## TypeScript Best Practices

- Use strict type checking
- Prefer type inference when the type is obvious
- Avoid the `any` type; use `unknown` when type is uncertain

## Angular Best Practices

- Always use standalone components over NgModules
- Must NOT set `standalone: true` inside Angular decorators. It's the default.
- Use signals for state management
- Implement lazy loading for feature routes
- Do NOT use the `@HostBinding` and `@HostListener` decorators. Put host bindings inside the `host` object of the `@Component` or `@Directive` decorator instead
- Use `NgOptimizedImage` for all static images.
  - `NgOptimizedImage` does not work for inline base64 images.

## Components

- Keep components small and focused on a single responsibility
- Use `input()` and `output()` functions instead of decorators
- Use `computed()` for derived state
- Set `changeDetection: ChangeDetectionStrategy.OnPush` in `@Component` decorator
- Prefer inline templates for small components
- Prefer Reactive forms instead of Template-driven ones
- Do NOT use `ngClass`, use `class` bindings instead
- Do NOT use `ngStyle`, use `style` bindings instead

## State Management

- Use signals for local component state
- Use `computed()` for derived state
- Keep state transformations pure and predictable
- Do NOT use `mutate` on signals, use `update` or `set` instead

## Templates

- Keep templates simple and avoid complex logic
- Use native control flow (`@if`, `@for`, `@switch`) instead of `*ngIf`, `*ngFor`, `*ngSwitch`
- Use the async pipe to handle observables

## Services

- Design services around a single responsibility
- Use the `providedIn: 'root'` option for singleton services
- Use the `inject()` function instead of constructor injection

## File Naming Conventions

Do NOT include type-specific suffixes like `.component`, `.service`, `.directive`, or `.pipe` in file names. Use only the base name followed by the extension (e.g., `hero.ts` instead of `hero.component.ts` for a component, or `data.ts` instead of `data.service.ts` for a service). The type is inferred from the decorator (e.g., `@Component` or `@Injectable`).
This applies to all Angular artifacts to align with Angular 20's simplified file structure for standalone elements.

# Instrucciones para Copilot: PrimeNG y Tailwind CSS

## PrimeNG: Deprecación de `styleClass`

### Contexto
En versiones recientes de PrimeNG, la propiedad `styleClass` ha sido marcada como **deprecated**. Esto significa que su uso no es recomendado y podría ser eliminado en futuras versiones.

### Alternativa
En lugar de usar `styleClass`, utiliza la propiedad estándar `class` para aplicar clases CSS. Esto asegura compatibilidad con futuras versiones de PrimeNG y sigue las mejores prácticas de Angular.

#### Ejemplo:
```html
<!-- Antes -->
<p-button label="Enviar" styleClass="custom-class"></p-button>

<!-- Ahora -->
<p-button label="Enviar" class="custom-class"></p-button>
```

## Tailwind CSS: Ajustes de colores

### Paleta de colores
Utiliza los siguientes valores hexadecimales para garantizar consistencia visual:
- **Primario:**
  - `#0077c2` (primary-100)
  - `#59a5f5` (primary-200)
  - `#c8ffff` (primary-300)
- **Acentos:**
  - `#00bfff` (accent-100)
  - `#00619a` (accent-200)
- **Texto:**
  - `#333333` (text-100)
  - `#5c5c5c` (text-200)
- **Fondo:**
  - `#ffffff` (bg-100)
  - `#f5f5f5` (bg-200)
  - `#cccccc` (bg-300)

### Ejemplo de uso en Tailwind CSS
```html
<div class="bg-[#f5f5f5] dark:bg-[#7a7a7a] text-[#333333] dark:text-[#ffffff]">
  <!-- Contenido -->
</div>
```

## Recomendaciones
- **Evita el uso de propiedades obsoletas:** Mantén tu código actualizado con las mejores prácticas.
- **Consistencia visual:** Aplica la paleta de colores proporcionada en todos los componentes.
- **Documentación:** Consulta siempre la documentación oficial de PrimeNG y Tailwind CSS para verificar cambios recientes.

---
Última actualización: 23 de noviembre de 2025