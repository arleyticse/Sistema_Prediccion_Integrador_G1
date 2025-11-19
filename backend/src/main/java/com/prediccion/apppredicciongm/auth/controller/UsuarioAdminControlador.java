package com.prediccion.apppredicciongm.auth.controller;

import com.prediccion.apppredicciongm.auth.dto.UsuarioCreateRequest;
import com.prediccion.apppredicciongm.auth.dto.AuthResponse;
import com.prediccion.apppredicciongm.auth.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de usuarios por gerentes
 * RF001: El sistema permite al gerente registrar usuarios
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestión Usuarios Gerente", description = "RF001 - Gestión de usuarios por gerente")
public class UsuarioAdminControlador {

    private final IUsuarioService usuarioService;

    /**
     * RF001: Registrar nuevo usuario (solo gerentes)
     */
    @PostMapping("/registrar")
    @Operation(summary = "Registrar nuevo usuario", description = "RF001 - Permite al gerente registrar un nuevo usuario en el sistema")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<AuthResponse> registrarUsuario(@RequestBody UsuarioCreateRequest request) {

        try {
            log.info("[GERENTE] Registrando nuevo usuario: {}", request.getEmail());
            AuthResponse response = usuarioService.crearUsuarioAdmin(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error registrando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", e.getMessage())
                    .build();
        }
    }

    /**
     * Listar todos los usuarios (solo gerentes)
     */
    @GetMapping("/listar")
    @Operation(summary = "Listar usuarios", description = "Obtiene lista de todos los usuarios del sistema")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<List<AuthResponse>> listarUsuarios() {

        try {
            log.info("[GERENTE] Consultando lista de usuarios");
            List<AuthResponse> usuarios = usuarioService.listarUsuarios();

            return ResponseEntity.ok(usuarios);

        } catch (Exception e) {
            log.error("Error listando usuarios: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar rol de usuario (solo gerentes)
     */
    @PutMapping("/{usuarioId}/rol")
    @Operation(summary = "Actualizar rol usuario", description = "Permite al gerente cambiar el rol de un usuario")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<AuthResponse> actualizarRolUsuario(
            @PathVariable Long usuarioId,
            @RequestParam String nuevoRol) {

        try {
            log.info("[GERENTE] Actualizando rol de usuario {} a {}", usuarioId, nuevoRol);
            AuthResponse response = usuarioService.actualizarRol(usuarioId, nuevoRol);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error actualizando rol: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", e.getMessage())
                    .build();
        }
    }

    /**
     * Actualizar usuario completo (solo gerentes)
     */
    @PutMapping("/{usuarioId}")
    @Operation(summary = "Actualizar usuario", description = "Permite al gerente actualizar los datos de un usuario")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<AuthResponse> actualizarUsuario(
            @PathVariable Long usuarioId,
            @RequestBody UsuarioCreateRequest request) {

        try {
            log.info("[GERENTE] Actualizando usuario: {}", usuarioId);
            AuthResponse response = usuarioService.actualizarUsuario(usuarioId, request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error actualizando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", e.getMessage())
                    .build();
        }
    }

    /**
     * Eliminar usuario (solo gerentes)
     */
    @DeleteMapping("/{usuarioId}")
    @Operation(summary = "Eliminar usuario", description = "Permite al gerente eliminar un usuario del sistema")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long usuarioId) {

        try {
            log.info("[GERENTE] Eliminando usuario: {}", usuarioId);
            usuarioService.desactivarUsuario(usuarioId);

            return ResponseEntity.ok("Usuario eliminado correctamente");

        } catch (Exception e) {
            log.error("Error eliminando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}