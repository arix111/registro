package com.registro.config;

import com.registro.service.AuditLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final AuditLogService auditLogService;

    public SecurityConfig(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Configuración principal de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuración de autorización
            .authorizeHttpRequests(authz -> authz
                // Recursos públicos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/error", "/access-denied").permitAll()
                
                // Registro solo para ADMIN
                .requestMatchers("/register").hasRole("ADMIN")
                
                // Rutas de administración - solo ADMIN
                .requestMatchers("/admin/**", "/users/**").hasRole("ADMIN")
                
                // Rutas de gestión - ADMIN y MANAGER
                .requestMatchers("/estadisticas/**", "/reports/**").hasAnyRole("ADMIN", "MANAGER")
                
                // APIs REST - autenticados
                .requestMatchers("/api/**").authenticated()
                
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            
            // Configuración de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            
            // Configuración de logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Configuración de sesiones
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired")
            )
            
            // Configuración de recordar usuario
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 horas
                .rememberMeParameter("remember-me")
            )
            
            // Protección CSRF habilitada
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/usuarios/asignar-equipo", "/usuarios/desasignar-equipo") // Deshabilitar CSRF para APIs REST y endpoints de asignación de equipos
            )
            
            // Headers de seguridad
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(content -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )
            
            // Manejo de excepciones
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    /**
     * Encoder de contraseñas con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Fuerza 12 para mayor seguridad
    }

    /**
     * Manager de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Handler personalizado para login exitoso
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Opcional: Registrar login exitoso
            auditLogService.logLogin(authentication.getName());
            // Redireccionar según el rol
            String redirectUrl = determineTargetUrl(authentication);
            response.sendRedirect(redirectUrl);
        };
    }

    /**
     * Handler personalizado para login fallido con auditoría
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            String errorMessage = "Credenciales inválidas";

            // Registrar el intento fallido en la auditoría
            auditLogService.logActivity(
                username, // Usamos el usuario del formulario
                "LOGIN_FALLIDO",
                "Sistema",
                request.getRemoteAddr(), // Guardamos la IP
                "Intento de login fallido. Motivo: " + exception.getMessage()
            );

            if (exception.getMessage().contains("User account is locked")) {
                errorMessage = "Cuenta bloqueada";
            } else if (exception.getMessage().contains("User is disabled")) {
                errorMessage = "Cuenta deshabilitada";
            } else if (exception.getMessage().contains("User account has expired")) {
                errorMessage = "Cuenta expirada";
            }
            
            response.sendRedirect(request.getContextPath() + "/login?error=true&message=" + errorMessage);
        };
    }

    /**
     * Handler para accesos denegados con auditoría
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String username = auth.getName();
                String uri = request.getRequestURI();
                
                // Registrar el intento de acceso denegado
                auditLogService.logActivity(
                    username,
                    "ACCESO_DENEGADO",
                    "Sistema",
                    uri,
                    "Usuario intentó acceder a recurso sin permisos: " + uri
                );
            }
            
            response.sendRedirect(request.getContextPath() + "/access-denied");
        };
    }

    /**
     * Determinar URL de redirección después del login
     */
    private String determineTargetUrl(org.springframework.security.core.Authentication authentication) {
        // Todos los usuarios van al dashboard principal
        return "/dashboard";
    }
}
