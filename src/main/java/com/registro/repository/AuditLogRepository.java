package com.registro.repository;

import com.registro.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Buscar por nombre de usuario
    Page<AuditLog> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    // Buscar solo actividades críticas
    @Query("SELECT a FROM AuditLog a WHERE a.action IN ('CREAR', 'EDITAR', 'ELIMINAR')")
    Page<AuditLog> findCriticalActivities(Pageable pageable);

    // Buscar por tipo de entidad
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    // Buscar por acción
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Buscar actividades de un usuario específico
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    // Contar total de actividades por usuario
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username")
    Long countByUsername(@Param("username") String username);

    // Contar actividades críticas
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action IN ('CREAR', 'EDITAR', 'ELIMINAR')")
    Long countCriticalActivities();

    // Contar logins fallidos desde una fecha
    @Query("SELECT COUNT(l) FROM AuditLog l WHERE l.action = 'LOGIN_FALLIDO' AND l.timestamp >= :since")
    long countFailedLoginsSince(@Param("since") LocalDateTime since);
}
