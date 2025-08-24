// src/main/java/com/registro/controller/EquipoInformaticoController.java
package com.registro.controller;

import com.registro.model.EquipoInformatico;
import com.registro.service.AuditLogService;
import com.registro.service.EquipoInformaticoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@Slf4j
public class EquipoInformaticoController {

    private final EquipoInformaticoService equipoService;
    private final AuditLogService auditLogService;

    public EquipoInformaticoController(EquipoInformaticoService equipoService,
                                      AuditLogService auditLogService) {
        this.equipoService = equipoService;
        this.auditLogService = auditLogService;
    }

    /**
     * Crear un nuevo equipo informático para un usuario
     */
    @PostMapping
    public ResponseEntity<?> crearEquipo(@RequestBody EquipoRequest request) {
        try {
            EquipoInformatico equipo = equipoService.crearEquipo(
                request.getLegajoUsuario(),
                request.getTipo(),
                request.getMarca(),
                request.getModelo(),
                request.getNumeroSerie(),
                request.getNumeroInventario(),
                request.getEstado(),
                request.getObservaciones()
            );
            
            // Registrar auditoría de creación de equipo
            auditLogService.logActivity(
                "admin", // TODO: obtener usuario actual del contexto de seguridad
                "CREAR",
                "Equipo",
                equipo.getId().toString(),
                "Equipo creado: " + request.getMarca() + " " + request.getModelo() + 
                " (Serie: " + request.getNumeroSerie() + ") para usuario: " + request.getLegajoUsuario()
            );
            
            return ResponseEntity.ok(equipo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Actualizar un equipo informático existente
     */
    @PutMapping("/{equipoId}")
    public ResponseEntity<?> actualizarEquipo(@PathVariable Long equipoId, @RequestBody EquipoRequest request) {
        try {
            EquipoInformatico equipo = equipoService.actualizarEquipo(
                equipoId,
                request.getTipo(),
                request.getMarca(),
                request.getModelo(),
                request.getNumeroSerie(),
                request.getNumeroInventario(),
                request.getEstado(),
                request.getObservaciones()
            );
            return ResponseEntity.ok(equipo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Eliminar un equipo informático
     */
    @DeleteMapping("/{equipoId}")
    public ResponseEntity<?> eliminarEquipo(@PathVariable Long equipoId) {
        try {
            // Obtener información del equipo antes de eliminarlo para auditoría
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(equipoId);
            String detallesEquipo = equipo != null ? 
                equipo.getMarca() + " " + equipo.getModelo() + " (Serie: " + equipo.getNumeroSerie() + ")" : 
                "Equipo ID: " + equipoId;
            
            equipoService.eliminarEquipo(equipoId);
            
            // Registrar auditoría de eliminación de equipo
            auditLogService.logActivity(
                "admin", // TODO: obtener usuario actual del contexto de seguridad
                "ELIMINAR",
                "Equipo",
                equipoId.toString(),
                "Equipo eliminado: " + detallesEquipo
            );
            
            return ResponseEntity.ok("Equipo eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtener todos los equipos de un usuario
     */
    @GetMapping("/usuario/{legajo}")
    public ResponseEntity<List<EquipoInformatico>> obtenerEquiposPorUsuario(@PathVariable String legajo) {
        List<EquipoInformatico> equipos = equipoService.obtenerEquiposPorUsuario(legajo);
        return ResponseEntity.ok(equipos);
    }

    /**
     * Obtener un equipo por ID
     */
    @GetMapping("/{equipoId}")
    public ResponseEntity<?> obtenerEquipoPorId(@PathVariable Long equipoId) {
        try {
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(equipoId);
            return ResponseEntity.ok(equipo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Verificar si un número de serie ya existe
     */
    @GetMapping("/verificar-serie/{numeroSerie}")
    public ResponseEntity<Boolean> verificarNumeroSerie(@PathVariable String numeroSerie) {
        boolean existe = equipoService.existeNumeroSerie(numeroSerie);
        return ResponseEntity.ok(existe);
    }

    /**
     * Obtener estadísticas de equipos por site para el dashboard
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            // Obtener todos los equipos
            List<EquipoInformatico> equipos = equipoService.listarTodos();
            
            // Organizar estadísticas por site
            java.util.Map<String, java.util.Map<String, Object>> siteData = new java.util.HashMap<>();
            
            for (EquipoInformatico equipo : equipos) {
                String siteName = equipo.getSite() != null ? equipo.getSite().getLabel() : "Sin ubicación";
                
                siteData.computeIfAbsent(siteName, k -> {
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("totalEquipos", 0);
                    data.put("equiposActivos", 0);
                    data.put("equiposInactivos", 0);
                    data.put("equiposAsignados", 0);
                    data.put("equiposDisponibles", 0);
                    data.put("equiposEnReparacion", 0);
                    data.put("equiposEnMantenimiento", 0);
                    data.put("equiposDadosDeBaja", 0);
                    data.put("equiposEstadoActivo", 0);
                    data.put("equiposEstadoInactivo", 0);
                    data.put("usuarios", new java.util.HashSet<String>());
                    data.put("equiposDetalle", new java.util.ArrayList<java.util.Map<String, Object>>());
                    data.put("equiposPorTipo", new java.util.HashMap<String, java.util.Map<String, Object>>());
                    return data;
                });
                
                java.util.Map<String, Object> data = siteData.get(siteName);
                data.put("totalEquipos", (Integer) data.get("totalEquipos") + 1);
                
                // Contar activos/inactivos
                if (equipo.getActivo() != null && equipo.getActivo()) {
                    data.put("equiposActivos", (Integer) data.get("equiposActivos") + 1);
                } else {
                    data.put("equiposInactivos", (Integer) data.get("equiposInactivos") + 1);
                }
                
                // Contar asignados/disponibles
                if (equipo.getUsuario() != null) {
                    data.put("equiposAsignados", (Integer) data.get("equiposAsignados") + 1);
                    @SuppressWarnings("unchecked")
                    java.util.Set<String> usuarios = (java.util.Set<String>) data.get("usuarios");
                    usuarios.add(equipo.getUsuario().getNombre() + " " + equipo.getUsuario().getApellido());
                } else {
                    data.put("equiposDisponibles", (Integer) data.get("equiposDisponibles") + 1);
                }
                
                // Contar por estado específico
                if (equipo.getEstado() != null) {
                    switch (equipo.getEstado()) {
                        case EN_REPARACION:
                            data.put("equiposEnReparacion", (Integer) data.get("equiposEnReparacion") + 1);
                            break;
                        case EN_MANTENIMIENTO:
                            data.put("equiposEnMantenimiento", (Integer) data.get("equiposEnMantenimiento") + 1);
                            break;
                        case DADO_DE_BAJA:
                            data.put("equiposDadosDeBaja", (Integer) data.get("equiposDadosDeBaja") + 1);
                            break;
                        case ACTIVO:
                            data.put("equiposEstadoActivo", (Integer) data.get("equiposEstadoActivo") + 1);
                            break;
                        case INACTIVO:
                            data.put("equiposEstadoInactivo", (Integer) data.get("equiposEstadoInactivo") + 1);
                            break;
                    }
                }
                
                // Agregar detalle del equipo
                @SuppressWarnings("unchecked")
                java.util.List<java.util.Map<String, Object>> equiposDetalle = 
                    (java.util.List<java.util.Map<String, Object>>) data.get("equiposDetalle");
                
                java.util.Map<String, Object> equipoDetalle = new java.util.HashMap<>();
                equipoDetalle.put("id", equipo.getId());
                equipoDetalle.put("tipo", equipo.getTipo() != null ? equipo.getTipo().getLabel() : "No especificado");
                equipoDetalle.put("marca", equipo.getMarca() != null ? equipo.getMarca() : "No especificada");
                equipoDetalle.put("modelo", equipo.getModelo() != null ? equipo.getModelo() : "No especificado");
                equipoDetalle.put("numeroSerie", equipo.getNumeroSerie() != null ? equipo.getNumeroSerie() : "N/A");
                equipoDetalle.put("numeroInventario", equipo.getNumeroInventario() != null ? equipo.getNumeroInventario() : "N/A");
                equipoDetalle.put("estado", equipo.getEstado() != null ? equipo.getEstado().name() : "No especificado");
                equipoDetalle.put("estadoLabel", equipo.getEstado() != null ? equipo.getEstado().name() : "No especificado");
                equipoDetalle.put("activo", equipo.getActivo() != null && equipo.getActivo());
                equipoDetalle.put("usuarioAsignado", equipo.getUsuario() != null ? 
                    equipo.getUsuario().getNombre() + " " + equipo.getUsuario().getApellido() : null);
                equipoDetalle.put("fechaRegistro", equipo.getFechaRegistro());
                equipoDetalle.put("observaciones", equipo.getObservaciones());
                
                equiposDetalle.add(equipoDetalle);
                
                // ===== AGRUPAR POR TIPO DE EQUIPO =====
                @SuppressWarnings("unchecked")
                java.util.Map<String, java.util.Map<String, Object>> equiposPorTipo = 
                    (java.util.Map<String, java.util.Map<String, Object>>) data.get("equiposPorTipo");
                
                String tipoEquipo = equipo.getTipo() != null ? equipo.getTipo().getLabel() : "Sin especificar";
                
                equiposPorTipo.computeIfAbsent(tipoEquipo, k -> {
                    java.util.Map<String, Object> tipoData = new java.util.HashMap<>();
                    tipoData.put("total", 0);
                    tipoData.put("activos", 0);
                    tipoData.put("inactivos", 0);
                    tipoData.put("asignados", 0);
                    tipoData.put("disponibles", 0);
                    tipoData.put("enReparacion", 0);
                    tipoData.put("enMantenimiento", 0);
                    tipoData.put("dadosDeBaja", 0);
                    tipoData.put("estadoActivo", 0);
                    tipoData.put("estadoInactivo", 0);
                    tipoData.put("equipos", new java.util.ArrayList<java.util.Map<String, Object>>());
                    return tipoData;
                });
                
                java.util.Map<String, Object> tipoData = equiposPorTipo.get(tipoEquipo);
                tipoData.put("total", (Integer) tipoData.get("total") + 1);
                
                // Contar por estado activo/inactivo
                if (equipo.getActivo() != null && equipo.getActivo()) {
                    tipoData.put("activos", (Integer) tipoData.get("activos") + 1);
                } else {
                    tipoData.put("inactivos", (Integer) tipoData.get("inactivos") + 1);
                }
                
                // Contar por asignación
                if (equipo.getUsuario() != null) {
                    tipoData.put("asignados", (Integer) tipoData.get("asignados") + 1);
                } else {
                    tipoData.put("disponibles", (Integer) tipoData.get("disponibles") + 1);
                }
                
                // Contar por estado específico
                if (equipo.getEstado() != null) {
                    switch (equipo.getEstado()) {
                        case EN_REPARACION:
                            tipoData.put("enReparacion", (Integer) tipoData.get("enReparacion") + 1);
                            break;
                        case EN_MANTENIMIENTO:
                            tipoData.put("enMantenimiento", (Integer) tipoData.get("enMantenimiento") + 1);
                            break;
                        case DADO_DE_BAJA:
                            tipoData.put("dadosDeBaja", (Integer) tipoData.get("dadosDeBaja") + 1);
                            break;
                        case ACTIVO:
                            tipoData.put("estadoActivo", (Integer) tipoData.get("estadoActivo") + 1);
                            break;
                        case INACTIVO:
                            tipoData.put("estadoInactivo", (Integer) tipoData.get("estadoInactivo") + 1);
                            break;
                    }
                }
                
                // Agregar equipo al detalle del tipo
                @SuppressWarnings("unchecked")
                java.util.List<java.util.Map<String, Object>> equiposTipo = 
                    (java.util.List<java.util.Map<String, Object>>) tipoData.get("equipos");
                equiposTipo.add(equipoDetalle);
            }
            
            // Convertir Set a size para JSON
            for (java.util.Map<String, Object> data : siteData.values()) {
                @SuppressWarnings("unchecked")
                java.util.Set<String> usuarios = (java.util.Set<String>) data.get("usuarios");
                data.put("usuarios", java.util.Map.of("size", usuarios.size()));
            }
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("siteData", siteData);
            response.put("totalEquipos", equipos.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    /**
     * Endpoint para búsqueda global en tiempo real (AJAX)
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarEquipos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String filtroTipo) {
        
        try {
            // Configurar paginación
            org.springframework.data.domain.Sort.Direction direction = 
                sortDir.equalsIgnoreCase("desc") ? 
                org.springframework.data.domain.Sort.Direction.DESC : 
                org.springframework.data.domain.Sort.Direction.ASC;
            org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(direction, sortBy);
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, sort);
            
            // Buscar equipos (global si hay término de búsqueda, todos si no)
            org.springframework.data.domain.Page<EquipoInformatico> equipos;
            if (buscar != null && !buscar.trim().isEmpty() || filtroTipo != null && !filtroTipo.trim().isEmpty()) {
                equipos = equipoService.buscarEquipos(buscar != null ? buscar.trim() : null, filtroTipo, pageable);
            } else {
                equipos = equipoService.obtenerTodosLosEquipos(pageable);
            }
            
            // Crear respuesta compatible con frontend
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("content", equipos.getContent());
            response.put("totalElements", equipos.getTotalElements());
            response.put("totalPages", equipos.getTotalPages());
            response.put("currentPage", equipos.getNumber());
            response.put("size", equipos.getSize());
            response.put("first", equipos.isFirst());
            response.put("last", equipos.isLast());
            response.put("numberOfElements", equipos.getNumberOfElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en la búsqueda de equipos: ", e);
            return ResponseEntity.badRequest().body("Error en búsqueda: " + e.getMessage());
        }
    }

    // Clase interna para el request
    public static class EquipoRequest {
        private String legajoUsuario;
        private EquipoInformatico.TipoEquipo tipo;
        private String marca;
        private String modelo;
        private String numeroSerie;
        private String numeroInventario;
        private EquipoInformatico.EstadoEquipo estado;
        private String observaciones;

        // Getters y Setters
        public String getLegajoUsuario() { return legajoUsuario; }
        public void setLegajoUsuario(String legajoUsuario) { this.legajoUsuario = legajoUsuario; }

        public EquipoInformatico.TipoEquipo getTipo() { return tipo; }
        public void setTipo(EquipoInformatico.TipoEquipo tipo) { this.tipo = tipo; }

        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }

        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }

        public String getNumeroSerie() { return numeroSerie; }
        public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

        public String getNumeroInventario() { return numeroInventario; }
        public void setNumeroInventario(String numeroInventario) { this.numeroInventario = numeroInventario; }

        public EquipoInformatico.EstadoEquipo getEstado() { return estado; }
        public void setEstado(EquipoInformatico.EstadoEquipo estado) { this.estado = estado; }

        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
}