package com.registro.controller;

import com.registro.model.User;
import com.registro.service.UserService;
import com.registro.service.UsuarioService;
import com.registro.service.EquipoInformaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final UsuarioService usuarioService;
    private final EquipoInformaticoService equipoService;

    /**
     * Dashboard principal - aparece después del login
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        log.info("Usuario {} accedió al dashboard principal", authentication.getName());
        
        // Obtener información del usuario actual
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);
        
        // Obtener estadísticas básicas
        UserService.UserStats userStats = userService.getUserStats();
        long totalUsuarios = usuarioService.listarUsuarios().size();
        long totalEquipos = equipoService.listarTodos().size();
        
        // Agregar datos al modelo
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userStats", userStats);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalEquipos", totalEquipos);
        
        // Determinar permisos según el rol
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER"));
        
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        
        return "dashboard/main";
    }
}
