// src/main/java/com/registro/model/Archivo.java
package com.registro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "archivos")
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Column(nullable = false)
    private String url;

    /** Path exacto dentro del bucket (sin “gs://” ni URL completa) */
    @Column(nullable = false)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_legajo", referencedColumnName = "legajo", nullable = false)
    private Usuario usuario;

    public Archivo() {}

    public Archivo(String nombreOriginal, String url, String path, Usuario usuario) {
        this.nombreOriginal = nombreOriginal;
        this.url            = url;
        this.path           = path;
        this.usuario        = usuario;
    }

    // getters / setters omitidos por brevedad…
    public Long getId() { return id; }
    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String n) { nombreOriginal = n; }
    public String getUrl() { return url; }
    public void setUrl(String u) { url = u; }
    public String getPath() { return path; }
    public void setPath(String p) { path = p; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario u) { usuario = u; }
}
