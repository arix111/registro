package com.registro.controller;

import com.registro.model.AuditLog;
import com.registro.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ActivityController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String showActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "false") boolean criticalOnly,
            Model model) {
        
        // Configurar paginación (ordenar por fecha descendente)
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        
        Page<AuditLog> activities;
        
        // Aplicar filtros según los parámetros
        if (criticalOnly) {
            // Solo acciones críticas (CREAR, EDITAR, ELIMINAR)
            activities = auditLogService.findCriticalActivities(pageable);
        } else if (!search.isEmpty()) {
            // Búsqueda por texto
            activities = auditLogService.findByUsernameContaining(search, pageable);
        } else {
            // Todas las actividades
            activities = auditLogService.findAll(pageable);
        }
        
        // Obtener estadísticas para las tarjetas
        long totalActivities = auditLogService.getTotalActivities();
        long criticalActivitiesCount = auditLogService.getCriticalActivitiesCount();
        long failedLoginsToday = auditLogService.getFailedLoginsToday();

        // Agregar atributos al modelo
        model.addAttribute("activities", activities);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", activities.getTotalPages());
        model.addAttribute("totalElements", totalActivities); // Usar el total real
        model.addAttribute("criticalActivitiesCount", criticalActivitiesCount);
        model.addAttribute("failedLoginsToday", failedLoginsToday);
        model.addAttribute("search", search);
        model.addAttribute("criticalOnly", criticalOnly);
        
        return "activity/list";
    }
}
