// src/main/java/com/registro/controller/UsuarioController.java
package com.registro.controller;

import com.registro.model.Archivo;
import com.registro.model.EquipoInformatico;
import com.registro.model.Site;
import com.registro.model.Usuario;
import com.registro.service.ArchivoService;
import com.registro.service.AuditLogService;
import com.registro.service.EquipoInformaticoService;
import com.registro.service.UsuarioService;

import jakarta.transaction.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ArchivoService archivoService;
    private final EquipoInformaticoService equipoService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public UsuarioController(UsuarioService usuarioService,
                             ArchivoService archivoService,
                             EquipoInformaticoService equipoService,
                             AuditLogService auditLogService) {
        this.usuarioService = usuarioService;
        this.archivoService = archivoService;
        this.equipoService = equipoService;
        this.auditLogService = auditLogService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        
        // Estadísticas básicas para mostrar en el dashboard
        long totalUsuarios = usuarios.size();
        long totalEquipos = usuarios.stream()
            .mapToLong(u -> u.getEquiposInformaticos() != null ? u.getEquiposInformaticos().size() : 0)
            .sum();
        
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalEquipos", totalEquipos);
        
        return "usuario-list";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario-form";
    }

    @PostMapping
    @Transactional // Add this annotation
    public String crearYSubir(
            @RequestParam("legajo") String legajo,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("telefono") String telefono,
            @RequestParam("correoElectronico") String correoElectronico,
            @RequestParam("direccion") String direccion,
            @RequestParam("site") Site site,
            @RequestParam(name="archivos", required=false) MultipartFile[] archivos,
            @RequestParam(name="equiposJson", required=false) String equiposJson,
            Model model
    ) {
        try {
            // 1) Crear usuario
            Usuario creado = usuarioService.crearUsuario(
                legajo, nombre, apellido, telefono, correoElectronico, direccion, site
            );
            
            // Registrar auditoría de creación de usuario
            auditLogService.logActivity(
                "CREAR",
                "Usuario",
                creado.getLegajo(),
                "Usuario creado: " + nombre + " " + apellido + " (Legajo: " + legajo + ")"
            );
            // 2) Subir sólo archivos no vacíos
            if (archivos != null && Arrays.stream(archivos).anyMatch(f -> f != null && !f.isEmpty())) {
                archivoService.subirArchivos(legajo, archivos);
            }
            
            // 3) Procesar equipos informáticos
            if (equiposJson != null && !equiposJson.trim().isEmpty() && !"[]".equals(equiposJson.trim())) {
                try {
                    TypeReference<List<EquipoRequest>> typeRef = new TypeReference<List<EquipoRequest>>() {};
                    List<EquipoRequest> equipos = objectMapper.readValue(equiposJson, typeRef);
                    
                    for (EquipoRequest equipoReq : equipos) {
                        // Si el ID del equipo está presente, es una asignación de equipo existente
                        if (equipoReq.getId() != null) {
                            equipoService.asignarEquipoAUsuario(equipoReq.getId(), legajo);
                        } else {
                            // Si no hay ID, es un equipo nuevo
                            try {
                                equipoService.crearEquipo(
                                    legajo,
                                    convertirTipoEquipo(equipoReq.getTipo()),
                                    equipoReq.getMarca(),
                                    equipoReq.getModelo(),
                                    equipoReq.getNumeroSerie(),
                                    equipoReq.getNumeroInventario(),
                                    convertirEstadoEquipo(equipoReq.getEstado()),
                                    equipoReq.getObservaciones()
                                );
                            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                                // Error de clave duplicada - mostrar mensaje amigable
                                String numeroInventario = equipoReq.getNumeroInventario();
                                String numeroSerie = equipoReq.getNumeroSerie();
                                String tipoEquipo = equipoReq.getTipo();
                                
                                String mensajeError = "❌ No se pudo crear el equipo " + tipoEquipo + ": ";
                                
                                // Detectar si es error de clave duplicada
                                String errorMessage = e.getMessage().toLowerCase();
                                if (errorMessage.contains("duplicate") || errorMessage.contains("duplicado") || 
                                    errorMessage.contains("unique") || errorMessage.contains("único")) {
                                    
                                    if (numeroInventario != null && !numeroInventario.isEmpty()) {
                                        mensajeError += "Ya existe un equipo con el número de inventario '" + numeroInventario + "'. ";
                                    } else if (numeroSerie != null && !numeroSerie.isEmpty()) {
                                        mensajeError += "Ya existe un equipo con el número de serie '" + numeroSerie + "'. ";
                                    } else {
                                        mensajeError += "Ya existe un equipo con las mismas características. ";
                                    }
                                    
                                    mensajeError += "Por favor, verifica los datos.";
                                } else {
                                    mensajeError += "Error de integridad de datos: " + e.getMessage();
                                }
                                
                                throw new RuntimeException(mensajeError);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error procesando equipos: " + e.getMessage());
                }
            }
            
            model.addAttribute("mensaje",
                "Usuario '" + creado.getLegajo() + "' creado con archivos y equipos OK");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        // 3) Limpiar formulario
        model.addAttribute("usuario", new Usuario());
        return "usuario-form";
    }

    @GetMapping("/editar")
    public String mostrarEdicion(@RequestParam("legajo") String legajo, Model model) {
        Usuario usuario = usuarioService.listarUsuarios().stream()
            .filter(u -> u.getLegajo().equals(legajo))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + legajo));
        model.addAttribute("usuario", usuario);
        
        // Los archivos ya vienen cargados con el usuario por la relación JPA
        model.addAttribute("archivos", usuario.getArchivos());
        
        return "usuario-edit";
    }

    @PostMapping(value = "/editar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String procesarEdicion(
            @RequestParam("legajoOriginal") String legajoOriginal,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("telefono") String telefono,
            @RequestParam("correoElectronico") String correoElectronico,
            @RequestParam("direccion") String direccion,
            @RequestParam("site") Site site,
            @RequestParam(name="archivos", required=false) MultipartFile[] archivos,
            @RequestParam(name="equiposJson", required=false) String equiposJson,
            Model model
    ) {
        try {
            // 1) Cargar y actualizar datos
            Usuario usuario = usuarioService.listarUsuarios().stream()
                .filter(u -> u.getLegajo().equals(legajoOriginal))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + legajoOriginal));
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setTelefono(telefono);
            usuario.setCorreoElectronico(correoElectronico);
            usuario.setDireccion(direccion);
            usuario.setSite(site);
            usuarioService.actualizarUsuario(usuario);
            
            // Registrar auditoría de edición de usuario
            auditLogService.logActivity(
                "EDITAR",
                "Usuario",
                usuario.getLegajo(),
                "Usuario actualizado: " + nombre + " " + apellido + " (Legajo: " + legajoOriginal + ")"
            );

            // 2) Subir sólo archivos no vacíos
            if (archivos != null && Arrays.stream(archivos).anyMatch(f -> f != null && !f.isEmpty())) {
                archivoService.subirArchivos(legajoOriginal, archivos);
            }
            
            // 3) Procesar equipos informáticos (eliminar existentes y crear nuevos)
            if (equiposJson != null && !equiposJson.trim().isEmpty()) {
                try {
                    // Eliminar equipos existentes del usuario
                    List<EquipoInformatico> equiposExistentes = equipoService.obtenerEquiposPorUsuario(legajoOriginal);
                    for (EquipoInformatico equipo : equiposExistentes) {
                        equipoService.eliminarEquipo(equipo.getId());
                    }
                    
                    // Crear nuevos equipos si hay datos
                    if (!"[]".equals(equiposJson.trim())) {
                        TypeReference<List<EquipoRequest>> typeRef = new TypeReference<List<EquipoRequest>>() {};
                        List<EquipoRequest> equipos = objectMapper.readValue(equiposJson, typeRef);
                        
                        for (EquipoRequest equipoReq : equipos) {
                            try {
                                equipoService.crearEquipo(
                                    legajoOriginal,
                                    convertirTipoEquipo(equipoReq.getTipo()),
                                    equipoReq.getMarca(),
                                    equipoReq.getModelo(),
                                    equipoReq.getNumeroSerie(),
                                    equipoReq.getNumeroInventario(),
                                    convertirEstadoEquipo(equipoReq.getEstado()),
                                    equipoReq.getObservaciones()
                                );
                            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                                // Error de clave duplicada - mostrar mensaje amigable
                                String numeroInventario = equipoReq.getNumeroInventario();
                                String numeroSerie = equipoReq.getNumeroSerie();
                                String tipoEquipo = equipoReq.getTipo();
                                
                                String mensajeError = "❌ No se pudo crear el equipo " + tipoEquipo + ": ";
                                
                                // Detectar si es error de clave duplicada
                                String errorMessage = e.getMessage().toLowerCase();
                                if (errorMessage.contains("duplicate") || errorMessage.contains("duplicado") || 
                                    errorMessage.contains("unique") || errorMessage.contains("único")) {
                                    
                                    if (numeroInventario != null && !numeroInventario.isEmpty()) {
                                        mensajeError += "Ya existe un equipo con el número de inventario '" + numeroInventario + "'. ";
                                    } else if (numeroSerie != null && !numeroSerie.isEmpty()) {
                                        mensajeError += "Ya existe un equipo con el número de serie '" + numeroSerie + "'. ";
                                    } else {
                                        mensajeError += "Ya existe un equipo con las mismas características. ";
                                    }
                                    
                                    mensajeError += "Por favor, verifica los datos o usa 'Asignar Equipo Existente' si el equipo ya está registrado.";
                                } else {
                                    mensajeError += "Error de integridad de datos: " + e.getMessage();
                                }
                                
                                throw new RuntimeException(mensajeError);
                            } catch (Exception e) {
                                // Verificar si el mensaje de error contiene indicios de clave duplicada
                                String errorMessage = e.getMessage().toLowerCase();
                                if (errorMessage.contains("duplicate") || errorMessage.contains("unique") || 
                                    errorMessage.contains("constraint") || errorMessage.contains("uk87b0d2d8")) {
                                    
                                    String numeroInventario = equipoReq.getNumeroInventario();
                                    String numeroSerie = equipoReq.getNumeroSerie();
                                    String tipoEquipo = equipoReq.getTipo();
                                    
                                    String mensajeError = "❌ No se pudo crear el equipo " + tipoEquipo + ": ";
                                    
                                    if (numeroInventario != null && !numeroInventario.isEmpty()) {
                                        mensajeError += "Ya existe un equipo con el número de inventario '" + numeroInventario + "'. ";
                                    } else if (numeroSerie != null && !numeroSerie.isEmpty()) {
                                        mensajeError += "Ya existe un equipo con el número de serie '" + numeroSerie + "'. ";
                                    } else {
                                        mensajeError += "Ya existe un equipo con las mismas características. ";
                                    }
                                    
                                    mensajeError += "Por favor, verifica los datos o usa 'Asignar Equipo Existente' si el equipo ya está registrado.";
                                    throw new RuntimeException(mensajeError);
                                } else {
                                    // Otros errores
                                    throw new RuntimeException("Error creando equipo " + equipoReq.getTipo() + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error procesando equipos: " + e.getMessage());
                }
            }

            model.addAttribute("mensaje", "Usuario actualizado correctamente");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        // 3) Recargar usuario actualizado con sus archivos
        Usuario actualizado = usuarioService.listarUsuarios().stream()
            .filter(u -> u.getLegajo().equals(legajoOriginal))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + legajoOriginal));
        model.addAttribute("usuario", actualizado);
        return "usuario-edit";
    }

    // Endpoint para actualizar usuario por ID (desde formulario de edición)
    @PostMapping("/{id}")
    public String actualizarUsuario(
            @PathVariable Long id,
            @RequestParam("legajo") String legajo,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("telefono") String telefono,
            @RequestParam("correoElectronico") String correoElectronico,
            @RequestParam("direccion") String direccion,
            @RequestParam("site") Site site,
            @RequestParam(name="archivos", required=false) MultipartFile[] archivos,
            @RequestParam(name="equiposJson", required=false) String equiposJson,
            Model model
    ) {
        try {
            // 1) Buscar usuario por ID
            Usuario usuario = usuarioService.listarUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            String legajoOriginal = usuario.getLegajo();
            
            // 2) Actualizar datos del usuario
            usuario.setLegajo(legajo);
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setTelefono(telefono);
            usuario.setCorreoElectronico(correoElectronico);
            usuario.setDireccion(direccion);
            usuario.setSite(site);
            usuarioService.actualizarUsuario(usuario);

            // 3) Subir nuevos archivos si existen
            if (archivos != null && Arrays.stream(archivos).anyMatch(f -> f != null && !f.isEmpty())) {
                archivoService.subirArchivos(legajoOriginal, archivos);
            }
            
            // 4) Procesar equipos informáticos (SOLO AGREGAR NUEVOS)
            if (equiposJson != null && !equiposJson.trim().isEmpty() && !"[]".equals(equiposJson.trim())) {
                try {
                    TypeReference<List<EquipoRequest>> typeRef = new TypeReference<List<EquipoRequest>>() {};
                    List<EquipoRequest> equiposNuevos = objectMapper.readValue(equiposJson, typeRef);
                    
                    // Solo crear los nuevos equipos (NO eliminar existentes)
                    for (EquipoRequest equipoReq : equiposNuevos) {
                        equipoService.crearEquipo(
                            legajoOriginal,
                            EquipoInformatico.TipoEquipo.valueOf(equipoReq.getTipo()),
                            equipoReq.getMarca(),
                            equipoReq.getModelo(),
                            equipoReq.getNumeroSerie(),
                            equipoReq.getNumeroInventario(),
                            EquipoInformatico.EstadoEquipo.valueOf(equipoReq.getEstado()),
                            equipoReq.getObservaciones()
                        );
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error procesando equipos: " + e.getMessage());
                }
            }

            model.addAttribute("mensaje", "Usuario actualizado correctamente");
            
            // Recargar usuario actualizado
            Usuario actualizado = usuarioService.obtenerUsuarioPorLegajo(legajo);
            model.addAttribute("usuario", actualizado);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            // En caso de error, recargar el usuario original
            try {
                Usuario usuario = usuarioService.listarUsuarios().stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElse(new Usuario());
                model.addAttribute("usuario", usuario);
            } catch (Exception ex) {
                model.addAttribute("usuario", new Usuario());
            }
        }
        
        return "usuario-edit";
    }

    // Endpoint para eliminar equipo desde la página web
    @GetMapping("/equipos/eliminar/{equipoId}")
    public String eliminarEquipo(@PathVariable Long equipoId, @RequestParam Long usuarioId, Model model) {
        try {
            // Obtener información del equipo antes de eliminarlo para auditoría
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(equipoId);
            String detallesEquipo = equipo != null ? 
                equipo.getMarca() + " " + equipo.getModelo() + " (Serie: " + equipo.getNumeroSerie() + ")" : 
                "Equipo ID: " + equipoId;
            
            equipoService.eliminarEquipo(equipoId);
            
            // Registrar auditoría de eliminación de equipo
            auditLogService.logActivity(
                "ELIMINAR",
                "Equipo",
                equipoId.toString(),
                "Equipo eliminado: " + detallesEquipo
            );
            
            model.addAttribute("mensaje", "Equipo eliminado correctamente");
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar equipo: " + e.getMessage());
        }
        
        // Recargar la página de edición del usuario
        Usuario usuario = usuarioService.listarUsuarios().stream()
            .filter(u -> u.getId().equals(usuarioId))
            .findFirst()
            .orElse(new Usuario());
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("archivos", usuario.getArchivos());
        
        return "usuario-edit";
    }

    // Endpoint para eliminar archivo desde la página web
    @GetMapping("/archivos/eliminar/{archivoId}")
    public String eliminarArchivo(@PathVariable Long archivoId, @RequestParam Long usuarioId, Model model) {
        try {
            // Obtener información del archivo antes de eliminarlo para auditoría
            Archivo archivo = archivoService.obtenerArchivoPorId(archivoId);
            String detallesArchivo = archivo != null ? 
                archivo.getNombreOriginal() + " (" + archivo.getPath() + ")" : 
                "Archivo ID: " + archivoId;
            
            archivoService.eliminarArchivo(archivoId);
            
            // Registrar auditoría de eliminación de archivo
            auditLogService.logActivity(
                "ELIMINAR",
                "Archivo",
                archivoId.toString(),
                "Archivo eliminado: " + detallesArchivo
            );
            
            model.addAttribute("mensaje", "Archivo eliminado correctamente");
        } catch (Exception e) {
            model.addAttribute("error", "Error al eliminar archivo: " + e.getMessage());
        }
        
        // Recargar la página de edición del usuario
        Usuario usuario = usuarioService.listarUsuarios().stream()
            .filter(u -> u.getId().equals(usuarioId))
            .findFirst()
            .orElse(new Usuario());
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("archivos", usuario.getArchivos());
        
        return "usuario-edit";
    }

    // Endpoint para descargar archivo
    @GetMapping("/archivos/descargar/{archivoId}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long archivoId) {
        try {
            Archivo archivo = archivoService.obtenerArchivoPorId(archivoId);
            byte[] data = archivoService.descargarArchivoBytes(archivo.getPath());
            ByteArrayResource resource = new ByteArrayResource(data);

            // Registrar auditoría de descarga de archivo
            String detalles = "Archivo descargado: " + archivo.getNombreOriginal() + " (Usuario Propietario: " + archivo.getUsuario().getLegajo() + ")";
            auditLogService.logActivity(
                "DESCARGAR",
                "Archivo",
                archivoId.toString(),
                detalles
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombreOriginal() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            // Opcional: Registrar intento de descarga fallido
            auditLogService.logActivity(
                "DESCARGA_FALLIDA",
                "Archivo",
                archivoId.toString(),
                "Intento de descarga fallido. Motivo: " + e.getMessage()
            );
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Convierte el tipo de equipo del frontend al enum correspondiente
     */
    private EquipoInformatico.TipoEquipo convertirTipoEquipo(String tipoFrontend) {
        if (tipoFrontend == null) {
            throw new IllegalArgumentException("Tipo de equipo no puede ser null");
        }
        
        switch (tipoFrontend) {
        case "CPU":
            return EquipoInformatico.TipoEquipo.CPU;
        case "Monitor":
        case "MONITOR":
            return EquipoInformatico.TipoEquipo.MONITOR;
        case "Notebook":
        case "NOTEBOOK":
            return EquipoInformatico.TipoEquipo.NOTEBOOK;
        case "Only One":
        case "ONLY_ONE":
            return EquipoInformatico.TipoEquipo.ONLY_ONE;
        case "Mouse":
        case "MOUSE":
            return EquipoInformatico.TipoEquipo.MOUSE;
        case "Teclado":
        case "TECLADO":
            return EquipoInformatico.TipoEquipo.TECLADO;
        case "Vincha":
        case "VINCHA":
            return EquipoInformatico.TipoEquipo.VINCHA;
        case "Cables (especificar)":
        case "CABLES":
            return EquipoInformatico.TipoEquipo.CABLES;
        case "Router":
        case "ROUTER":
            return EquipoInformatico.TipoEquipo.ROUTER;
        case "Switch":
        case "SWITCH":
            return EquipoInformatico.TipoEquipo.SWITCH;
        case "Servidores":
        case "SERVIDORES":
            return EquipoInformatico.TipoEquipo.SERVIDORES;
        case "Proyectores":
        case "PROYECTORES":
            return EquipoInformatico.TipoEquipo.PROYECTORES;
        case "Otros (especificar)":
        case "OTROS":
            return EquipoInformatico.TipoEquipo.OTROS;
            default:
                throw new IllegalArgumentException("Tipo de equipo no válido: " + tipoFrontend);
        }
    }

    /**
     * Convierte el estado de equipo del frontend al enum correspondiente
     */
    private EquipoInformatico.EstadoEquipo convertirEstadoEquipo(String estadoFrontend) {
        if (estadoFrontend == null) {
            return EquipoInformatico.EstadoEquipo.ACTIVO; // Valor por defecto
        }
        
        switch (estadoFrontend) {
            case "Activo":
            case "ACTIVO":
                return EquipoInformatico.EstadoEquipo.ACTIVO;
            case "Inactivo":
            case "INACTIVO":
                return EquipoInformatico.EstadoEquipo.INACTIVO;
            case "En Reparación":
            case "EN_REPARACION":
                return EquipoInformatico.EstadoEquipo.EN_REPARACION;
            case "Dado de Baja":
            case "DADO_DE_BAJA":
                return EquipoInformatico.EstadoEquipo.DADO_DE_BAJA;
            case "En Mantenimiento":
            case "EN_MANTENIMIENTO":
                return EquipoInformatico.EstadoEquipo.EN_MANTENIMIENTO;
            default:
                return EquipoInformatico.EstadoEquipo.ACTIVO; // Valor por defecto
        }
    }

    // Clase interna para recibir datos de equipos desde el frontend
    public static class EquipoRequest {
        private Long id; // Campo para el ID temporal del frontend
        private String tipo; // Mantenemos como String para recibir del frontend
        private String marca;
        private String modelo;
        private String numeroSerie;
        private String numeroInventario;
        private String estado;
        private String observaciones;

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }

        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }

        public String getNumeroSerie() { return numeroSerie; }
        public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

        public String getNumeroInventario() { return numeroInventario; }
        public void setNumeroInventario(String numeroInventario) { this.numeroInventario = numeroInventario; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    // Endpoint para mostrar formulario de edición de usuario
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        try {
            Usuario usuario = usuarioService.listarUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            model.addAttribute("usuario", usuario);
            return "usuario-edit";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "redirect:/usuarios";
        }
    }

    // Endpoint para ver detalles de usuario
    @GetMapping("/{id}/ver")
    public String verUsuario(@PathVariable Long id, Model model) {
        try {
            Usuario usuario = usuarioService.listarUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            model.addAttribute("usuario", usuario);
            return "usuario-detalle";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "redirect:/usuarios";
        }
    }
    
    /**
     * Asignar un equipo existente a un usuario
     */
    @PostMapping("/asignar-equipo")
    public ResponseEntity<String> asignarEquipo(@RequestParam Long equipoId, @RequestParam String legajoUsuario) {
        try {
            equipoService.asignarEquipoAUsuario(equipoId, legajoUsuario);
            
            // Registrar auditoría de asignación de equipo
            auditLogService.logActivity(
                "ASIGNAR",
                "Equipo",
                equipoId.toString(),
                "Equipo asignado al usuario: " + legajoUsuario
            );
            
            return ResponseEntity.ok("Equipo asignado correctamente al usuario");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al asignar equipo: " + e.getMessage());
        }
    }
    
    /**
     * Desasignar un equipo de un usuario
     */
    @PostMapping("/desasignar-equipo")
    public ResponseEntity<String> desasignarEquipo(@RequestParam Long equipoId) {
        try {
            equipoService.desasignarEquipo(equipoId);
            
            // Registrar auditoría de desasignación de equipo
            auditLogService.logActivity(
                "DESASIGNAR",
                "Equipo",
                equipoId.toString(),
                "Equipo desasignado del usuario"
            );
            
            return ResponseEntity.ok("Equipo desasignado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al desasignar equipo: " + e.getMessage());
        }
    }
}