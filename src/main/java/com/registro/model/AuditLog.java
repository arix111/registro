package com.registro.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_username", columnList = "username"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_logs_entity_type", columnList = "entityType")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    @Column
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String ipAddress;

    // Constructores
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String username, String action, String entityType, String entityId, String details) {
        this();
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    // Métodos de utilidad
    public boolean isCritical() {
        return "CREAR".equals(action) || "EDITAR".equals(action) || "ELIMINAR".equals(action);
    }

    public String getActionBadgeClass() {
        switch (action) {
            case "CREAR":
                return "bg-success";
            case "EDITAR":
                return "bg-warning";
            case "ELIMINAR":
                return "bg-danger";
            case "VER":
                return "bg-info";
            case "LOGIN":
                return "bg-primary";
            case "LOGOUT":
                return "bg-secondary";
            default:
                return "bg-light text-dark";
        }
    }

    public String getActionIcon() {
        switch (action) {
            case "CREAR":
                return "bi-plus-circle";
            case "EDITAR":
                return "bi-pencil";
            case "ELIMINAR":
                return "bi-trash";
            case "VER":
                return "bi-eye";
            case "LOGIN":
                return "bi-box-arrow-in-right";
            case "LOGOUT":
                return "bi-box-arrow-right";
            default:
                return "bi-activity";
        }
    }
}
