package com.registro.controller;

import com.registro.model.User;
import com.registro.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    /**
     * Página principal de gestión de usuarios
     */
    @GetMapping
    public String gestionarUsuarios(Model model) {
        List<User> usuarios = userService.getAllUsers();
        UserService.UserStats stats = userService.getUserStats();
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("userStats", stats);
        model.addAttribute("roles", User.Role.values());
        
        return "admin/user-management";
    }

    /**
     * Formulario para crear nuevo usuario
     */
    @GetMapping("/nuevo")
    public String nuevoUsuarioForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return "admin/user-form";
    }

    /**
     * Procesar creación de nuevo usuario
     */
    @PostMapping("/crear")
    public String crearUsuario(@ModelAttribute User user,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas coincidan
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden.");
                return "redirect:/admin/usuarios/nuevo";
            }

            // Crear usuario
            userService.createUser(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
            );

            redirectAttributes.addFlashAttribute("successMessage", 
                "Usuario '" + user.getUsername() + "' creado exitosamente.");
            return "redirect:/admin/usuarios";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/usuarios/nuevo";
        }
    }

    /**
     * Formulario para editar usuario existente
     */
    @GetMapping("/editar/{id}")
    public String editarUsuarioForm(@PathVariable Long id, Model model) {
        try {
            User user = userService.getAllUsers().stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            model.addAttribute("user", user);
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        } catch (RuntimeException e) {
            return "redirect:/admin/usuarios";
        }
    }

    /**
     * Actualizar rol del usuario
     */
    @PostMapping("/actualizar-rol")
    public String actualizarRol(@RequestParam Long userId,
                               @RequestParam User.Role newRole,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(userId, newRole);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Rol actualizado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Habilitar/deshabilitar usuario
     */
    @PostMapping("/toggle-status")
    public String toggleUserStatus(@RequestParam Long userId,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserEnabled(userId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Estado del usuario actualizado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Cambiar contraseña de usuario
     */
    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam Long userId,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmNewPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas coincidan
            if (!newPassword.equals(confirmNewPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Las contraseñas no coinciden.");
                return "redirect:/admin/usuarios";
            }

            // Validar longitud mínima
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "La contraseña debe tener al menos 6 caracteres.");
                return "redirect:/admin/usuarios";
            }

            // Cambiar contraseña
            userService.changePasswordByAdmin(userId, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Contraseña cambiada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Eliminar usuario
     */
    @PostMapping("/eliminar")
    public String eliminarUsuario(@RequestParam Long userId,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Usuario eliminado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    /**
     * Buscar usuarios
     */
    @GetMapping("/buscar")
    public String buscarUsuarios(@RequestParam(required = false) String searchTerm,
                                Model model) {
        List<User> usuarios;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            usuarios = userService.searchUsers(searchTerm);
        } else {
            usuarios = userService.getAllUsers();
        }
        
        UserService.UserStats stats = userService.getUserStats();
        
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("userStats", stats);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("searchTerm", searchTerm);
        
        return "admin/user-management";
    }
}
