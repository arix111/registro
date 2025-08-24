package com.registro.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_asignaciones")
public class HistorialAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipo_informatico_id", nullable = false)
    private EquipoInformatico equipoInformatico;

    @ManyToOne
    @JoinColumn(name = "usuario_id") // Nullable si el equipo se desasigna y queda disponible
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaAsignacion;

    private LocalDateTime fechaDevolucion;

    private String asignadoPor; // Nombre del admin que hizo la asignación

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EquipoInformatico getEquipoInformatico() {
        return equipoInformatico;
    }

    public void setEquipoInformatico(EquipoInformatico equipoInformatico) {
        this.equipoInformatico = equipoInformatico;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getAsignadoPor() {
        return asignadoPor;
    }

    public void setAsignadoPor(String asignadoPor) {
        this.asignadoPor = asignadoPor;
    }
}
