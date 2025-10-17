package com.prediccion.apppredicciongm.gestion_inventario.producto.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.request.ProductoCreateRequest;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoEliminadoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.dto.response.ProductoResponse;
import com.prediccion.apppredicciongm.gestion_inventario.producto.schemas.ProductoExamples;
import com.prediccion.apppredicciongm.gestion_inventario.producto.services.IProductoServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Productos", description = "API para gestión de productos del inventario")
public class ProductoControlador {
    
    private final IProductoServicio productoServicio;
    
    // ==================== CRUD BÁSICO ====================
    
    @PostMapping
    @Operation(
        summary = "Crear nuevo producto",
        description = "Crea un nuevo producto en el catálogo. Requiere categoría y unidad de medida válidas. " +
                     "Se calcula automáticamente el costo de mantenimiento anual."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductoResponse.class),
                examples = @ExampleObject(
                    name = "Producto Creado",
                    value = ProductoExamples.RESPONSE_PRODUCTO_CREADO
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o categoría/unidad de medida no encontrada",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Error Validación",
                        value = ProductoExamples.ERROR_VALIDACION
                    ),
                    @ExampleObject(
                        name = "Categoría Inválida",
                        value = ProductoExamples.ERROR_CATEGORIA_INVALIDA
                    )
                }
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del producto a crear (diferentes tipos según categoría)",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Producto Alimento",
                    value = ProductoExamples.CREAR_PRODUCTO_ALIMENTO
                ),
                @ExampleObject(
                    name = "Producto Bebida",
                    value = ProductoExamples.CREAR_PRODUCTO_BEBIDA
                ),
                @ExampleObject(
                    name = "Producto Limpieza",
                    value = ProductoExamples.CREAR_PRODUCTO_LIMPIEZA
                ),
                @ExampleObject(
                    name = "Producto Medicamento",
                    value = ProductoExamples.CREAR_PRODUCTO_MEDICAMENTO
                )
            }
        )
    )
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody ProductoCreateRequest request) {
        ProductoResponse response = productoServicio.crearProducto(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza la información de un producto existente. " +
                     "Se recalcula automáticamente el costo de mantenimiento anual si se modifica el costo de mantenimiento."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductoResponse.class),
                examples = @ExampleObject(
                    name = "Producto Actualizado",
                    value = ProductoExamples.RESPONSE_PRODUCTO_CON_INVENTARIO
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Producto No Encontrado",
                    value = ProductoExamples.ERROR_PRODUCTO_NO_ENCONTRADO
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del producto a actualizar",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Actualizar Producto",
                value = ProductoExamples.ACTUALIZAR_PRODUCTO
            )
        )
    )
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer id,
            @Valid @RequestBody ProductoCreateRequest request) {
        ProductoResponse response = productoServicio.actualizarProducto(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del catálogo. No se puede eliminar si tiene inventario con stock disponible. " +
                     "Los movimientos históricos se mantienen para auditoría."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto eliminado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductoEliminadoResponse.class),
                examples = @ExampleObject(
                    name = "Producto Eliminado",
                    value = ProductoExamples.RESPONSE_PRODUCTO_ELIMINADO
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Producto No Encontrado",
                    value = ProductoExamples.ERROR_PRODUCTO_NO_ENCONTRADO
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "No se puede eliminar porque tiene inventario activo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Producto Con Inventario",
                    value = ProductoExamples.ERROR_PRODUCTO_CON_INVENTARIO
                )
            )
        )
    })
    public ResponseEntity<ProductoEliminadoResponse> eliminarProducto(
            @Parameter(description = "ID del producto") @PathVariable Integer id) {
        ProductoEliminadoResponse response = productoServicio.eliminarProducto(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID",
        description = "Obtiene la información completa de un producto, incluyendo datos de inventario si existen."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductoResponse.class),
                examples = @ExampleObject(
                    name = "Producto Con Inventario",
                    value = ProductoExamples.RESPONSE_PRODUCTO_CON_INVENTARIO
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Producto No Encontrado",
                    value = ProductoExamples.ERROR_PRODUCTO_NO_ENCONTRADO
                )
            )
        )
    })
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(
            @Parameter(description = "ID del producto") @PathVariable Integer id) {
        ProductoResponse response = productoServicio.obtenerProductoPorId(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los productos",
        description = "Obtiene un listado paginado de todos los productos con información de inventario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Lista Productos",
                    value = ProductoExamples.RESPONSE_LISTA_PRODUCTOS
                )
            )
        )
    })
    public ResponseEntity<Page<ProductoResponse>> listarProductos(
            @Parameter(description = "Número de página (inicia en 0)") 
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") 
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        Page<ProductoResponse> productos = productoServicio.listarProductos(page, size);
        return ResponseEntity.ok(productos);
    }
    @GetMapping("/todos")
    public ResponseEntity<List<ProductoResponse>> listarTodosProductos() {
        List<ProductoResponse> productos = productoServicio.listarTodos();
        return ResponseEntity.ok(productos);
    }
    // ==================== BÚSQUEDAS Y FILTROS ====================
    
    @GetMapping("/categoria/{categoriaId}")
    @Operation(
        summary = "Buscar productos por categoría",
        description = "Obtiene todos los productos de una categoría específica."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Productos encontrados",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Lista Productos",
                    value = ProductoExamples.RESPONSE_LISTA_PRODUCTOS
                )
            )
        )
    })
    public ResponseEntity<Page<ProductoResponse>> buscarPorCategoria(
            @Parameter(description = "ID de la categoría") @PathVariable Integer categoriaId,
            @Parameter(description = "Número de página") 
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @Parameter(description = "Tamaño de página") 
            @RequestParam(defaultValue = "20") @Min(1) int tamanioPagina) {
        Page<ProductoResponse> productos = productoServicio.buscarPorCategoria(categoriaId, pagina, tamanioPagina);
        return ResponseEntity.ok(productos);
    }
    
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar productos por nombre",
        description = "Realiza una búsqueda parcial de productos por nombre."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Productos encontrados",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Lista Productos",
                    value = ProductoExamples.RESPONSE_LISTA_PRODUCTOS
                )
            )
        )
    })
    public ResponseEntity<Page<ProductoResponse>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre del producto") 
            @RequestParam String nombre,
            @Parameter(description = "Número de página") 
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @Parameter(description = "Tamaño de página") 
            @RequestParam(defaultValue = "20") @Min(1) int tamanioPagina) {
        Page<ProductoResponse> productos = productoServicio.buscarPorNombre(nombre, pagina, tamanioPagina);
        return ResponseEntity.ok(productos);
    }
}
