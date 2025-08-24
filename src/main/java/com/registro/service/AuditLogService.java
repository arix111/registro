package com.registro.service;

import com.registro.model.AuditLog;
import com.registro.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Registrar una actividad
    public void logActivity(String action, String entityType, String entityId, String details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "SYSTEM";
        
        AuditLog log = new AuditLog(username, action, entityType, entityId, details);
        auditLogRepository.save(log);
    }

    // Registrar actividad con usuario específico
    public void logActivity(String username, String action, String entityType, String entityId, String details) {
        AuditLog log = new AuditLog(username, action, entityType, entityId, details);
        auditLogRepository.save(log);
    }

    // Obtener todas las actividades
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    // Buscar por nombre de usuario
    public Page<AuditLog> findByUsernameContaining(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    // Obtener solo actividades críticas
    public Page<AuditLog> findCriticalActivities(Pageable pageable) {
        return auditLogRepository.findCriticalActivities(pageable);
    }

    // Buscar por tipo de entidad
    public Page<AuditLog> findByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    // Buscar por acción
    public Page<AuditLog> findByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    // Obtener estadísticas
    public Long getTotalActivities() {
        return auditLogRepository.count();
    }

    public Long getCriticalActivitiesCount() {
        return auditLogRepository.countCriticalActivities();
    }

    public long getFailedLoginsToday() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return auditLogRepository.countFailedLoginsSince(todayStart);
    }

    public Long getActivitiesByUser(String username) {
        return auditLogRepository.countByUsername(username);
    }

    // Métodos de conveniencia para registrar actividades específicas
    public void logUserCreated(String userLegajo, String details) {
        logActivity("CREAR", "Usuario", userLegajo, details);
    }

    public void logUserUpdated(String userLegajo, String details) {
        logActivity("EDITAR", "Usuario", userLegajo, details);
    }

    public void logUserDeleted(String userLegajo, String details) {
        logActivity("ELIMINAR", "Usuario", userLegajo, details);
    }

    public void logEquipmentCreated(String equipmentId, String details) {
        logActivity("CREAR", "Equipo", equipmentId, details);
    }

    public void logEquipmentUpdated(String equipmentId, String details) {
        logActivity("EDITAR", "Equipo", equipmentId, details);
    }

    public void logEquipmentDeleted(String equipmentId, String details) {
        logActivity("ELIMINAR", "Equipo", equipmentId, details);
    }

    public void logLogin(String username) {
        logActivity(username, "LOGIN", "Sistema", null, "Usuario inició sesión");
    }

    public void logLogout(String username) {
        logActivity(username, "LOGOUT", "Sistema", null, "Usuario cerró sesión");
    }

    public void logPageView(String pageName) {
        logActivity("VER", "Página", null, "Acceso a: " + pageName);
    }
}
