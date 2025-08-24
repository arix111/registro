// src/main/java/com/registro/model/Usuario.java
package com.registro.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    /** Llave técnica */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Llave de negocio, única */
    @Column(nullable = false, unique = true)
    private String legajo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column
    private String telefono;

    @Column(name = "correo_electronico")
    private String correoElectronico;

    @Column
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Site site;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Archivo> archivos = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<EquipoInformatico> equiposInformaticos = new HashSet<>();

    public Usuario() {}

    public Usuario(String legajo, String nombre, String apellido, Site site) {
        this.legajo = legajo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.site = site;
    }

    // getters y setters para todos los campos...
    public Long getId() { return id; }

    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Site getSite() { return site; }
    public void setSite(Site site) { this.site = site; }

    public Set<Archivo> getArchivos() { return archivos; }
    public void setArchivos(Set<Archivo> archivos) { this.archivos = archivos; }

    public Set<EquipoInformatico> getEquiposInformaticos() { return equiposInformaticos; }
    public void setEquiposInformaticos(Set<EquipoInformatico> equiposInformaticos) { this.equiposInformaticos = equiposInformaticos; }

    // helpers bidireccionales para archivos
    public void addArchivo(Archivo archivo) {
        archivos.add(archivo);
        archivo.setUsuario(this);
    }
    public void removeArchivo(Archivo archivo) {
        archivos.remove(archivo);
        archivo.setUsuario(null);
    }

    // helpers bidireccionales para equipos informáticos
    public void addEquipoInformatico(EquipoInformatico equipo) {
        equiposInformaticos.add(equipo);
        equipo.setUsuario(this);
    }
    public void removeEquipoInformatico(EquipoInformatico equipo) {
        equiposInformaticos.remove(equipo);
        equipo.setUsuario(null);
    }
}

