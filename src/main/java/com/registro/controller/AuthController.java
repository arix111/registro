package com.registro.controller;

import com.registro.model.User;
import com.registro.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "expired", required = false) String expired,
                       Model model) {
        
        // Si el usuario ya está autenticado, redirigir al dashboard
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/usuarios";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Credenciales inválidas. Por favor, intente nuevamente.");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "Ha cerrado sesión exitosamente.");
        }
        
        if (expired != null) {
            model.addAttribute("warningMessage", "Su sesión ha expirado. Por favor, inicie sesión nuevamente.");
        }

        return "auth/login";
    }

    /**
     * Página de registro (opcional)
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * Procesar registro de nuevo usuario
     */
    @PostMapping("/register")
    public String processRegistration(@ModelAttribute User user,
                                    @RequestParam String confirmPassword,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Validar que las contraseñas coincidan
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden.");
                return "redirect:/register";
            }

            // Crear usuario con rol USER por defecto
            userService.createUser(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                User.Role.USER
            );

            redirectAttributes.addFlashAttribute("successMessage", 
                "Usuario registrado exitosamente. Puede iniciar sesión.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Página de acceso denegado
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            model.addAttribute("username", auth.getName());
        }
        return "auth/access-denied";
    }

    /**
     * Dashboard de administrador
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        UserService.UserStats stats = userService.getUserStats();
        model.addAttribute("userStats", stats);
        model.addAttribute("recentUsers", userService.getAllUsers());
        return "admin/dashboard";
    }

    /**
     * Mostrar página de cambio de contraseña
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "auth/change-password";
    }

    /**
     * Procesar cambio de contraseña
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Las nuevas contraseñas no coinciden.");
                return "redirect:/change-password";
            }

            userService.changePassword(authentication.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña cambiada exitosamente.");
            return "redirect:/usuarios";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/change-password";
        }
    }

    /**
     * Mostrar página de perfil personal
     */
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "auth/profile";
    }

    /**
     * Actualizar perfil personal
     */
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            userService.updateProfile(username, fullName, email);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado exitosamente.");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }
}
