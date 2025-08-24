// src/main/java/com/registro/model/EquipoInformatico.java
package com.registro.model;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "equipos_informaticos")
public class EquipoInformatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEquipo tipo;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(name = "numero_serie", unique = true)
    private String numeroSerie;

    @Column(name = "numero_inventario")
    private String numeroInventario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEquipo estado;

    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Site site;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_legajo", referencedColumnName = "legajo")
    private Usuario usuario;

    // Enum para el tipo de equipo
    public enum TipoEquipo {
        CPU("CPU"),
        MONITOR("Monitor"),
        NOTEBOOK("Notebook"),
        ONLY_ONE("Only One"),
        MOUSE("Mouse"),
        TECLADO("Teclado"),
        VINCHA("Vincha"),
        CABLES("Cables (especificar)"),
        ROUTER("Router"),
        SWITCH("Switch"),
        SERVIDORES("Servidores"),
        PROYECTORES("Proyectores"),
        OTROS("Otros (especificar)");

        private final String label;

        TipoEquipo(String label) {
            this.label = label;
        }

        @JsonValue
        public String getLabel() {
            return label;
        }
    }

    // Enum para el estado del equipo
    public enum EstadoEquipo {
        ACTIVO("Activo"),
        INACTIVO("Inactivo"),
        EN_REPARACION("En Reparación"),
        DADO_DE_BAJA("Dado de Baja"),
        EN_MANTENIMIENTO("En Mantenimiento");

        private final String label;

        EstadoEquipo(String label) {
            this.label = label;
        }

        @JsonValue
        public String getLabel() {
            return label;
        }
    }

    // Método para asignar valores por defecto automáticamente
    @PrePersist
    protected void onCreate() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDate.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.site == null) {
            this.site = Site.BUENOS_AIRES; // Valor por defecto
        }
    }

    // Constructores
    public EquipoInformatico() {}

    public EquipoInformatico(TipoEquipo tipo, String marca, String modelo, String numeroSerie, 
                            String numeroInventario, EstadoEquipo estado, LocalDate fechaAsignacion, 
                            LocalDate fechaRegistro, Site site, Boolean activo, String observaciones, Usuario usuario) {
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.numeroInventario = numeroInventario;
        this.estado = estado;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaRegistro = fechaRegistro;
        this.site = site;
        this.activo = activo;
        this.observaciones = observaciones;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoEquipo getTipo() {
        return tipo;
    }

    public void setTipo(TipoEquipo tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getNumeroInventario() {
        return numeroInventario;
    }

    public void setNumeroInventario(String numeroInventario) {
        this.numeroInventario = numeroInventario;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public void setEstado(EstadoEquipo estado) {
        this.estado = estado;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}