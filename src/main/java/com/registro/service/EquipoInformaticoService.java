// src/main/java/com/registro/service/EquipoInformaticoService.java
package com.registro.service;

import com.registro.model.EquipoInformatico;
import com.registro.model.HistorialAsignacion;
import com.registro.model.Usuario;
import com.registro.repository.HistorialAsignacionRepository;
import com.registro.repository.IEquipoInformaticoRepository;
import com.registro.repository.IUsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipoInformaticoService {

    private final IEquipoInformaticoRepository equipoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final HistorialAsignacionRepository historialAsignacionRepository;

    public EquipoInformaticoService(IEquipoInformaticoRepository equipoRepository,
                                   IUsuarioRepository usuarioRepository,
                                   HistorialAsignacionRepository historialAsignacionRepository) {
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialAsignacionRepository = historialAsignacionRepository;
    }

    public Map<String, Object> getEquipoStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<EquipoInformatico> allEquipos = equipoRepository.findAll();

        // 1. Conteo total por estado
        Map<EquipoInformatico.EstadoEquipo, Long> countByStatus = allEquipos.stream()
                .collect(Collectors.groupingBy(EquipoInformatico::getEstado, Collectors.counting()));

        // 2. Estadísticas por Site
        Map<String, Map<String, Long>> statsBySite = allEquipos.stream()
                .filter(e -> e.getSite() != null)
                .collect(Collectors.groupingBy(e -> e.getSite().getLabel(),
                        Collectors.groupingBy(e -> e.getEstado().name(), Collectors.counting())));

        stats.put("totalEquipos", (long) allEquipos.size());
        stats.put("countByStatus", countByStatus);
        stats.put("statsBySite", statsBySite);
        stats.put("allEquipos", allEquipos);
        stats.put("sites", allEquipos.stream().map(EquipoInformatico::getSite).distinct().collect(Collectors.toList()));
        stats.put("tiposEquipo", EquipoInformatico.TipoEquipo.values());

        return stats;
    }

    /**
     * Crear y asignar un equipo informático a un usuario
     */
    public EquipoInformatico crearEquipo(String legajoUsuario, EquipoInformatico.TipoEquipo tipo, String marca, String modelo,
                                        String numeroSerie, String numeroInventario,
                                        EquipoInformatico.EstadoEquipo estado, String observaciones) {
        Usuario usuario = usuarioRepository.findByLegajo(legajoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + legajoUsuario));

        EquipoInformatico equipo = new EquipoInformatico();
        equipo.setTipo(tipo);
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie(numeroSerie);
        equipo.setNumeroInventario(numeroInventario);
        equipo.setEstado(estado);
        equipo.setFechaAsignacion(LocalDate.now());
        equipo.setObservaciones(observaciones);
        equipo.setUsuario(usuario);
        
        equipo = equipoRepository.save(equipo);

        // Crear registro en el historial
        HistorialAsignacion historial = new HistorialAsignacion();
        historial.setEquipoInformatico(equipo);
        historial.setUsuario(usuario);
        historial.setFechaAsignacion(LocalDateTime.now());
        // AsignadoPor podría venir del contexto de seguridad
        // historial.setAsignadoPor(SecurityContextHolder.getContext().getAuthentication().getName());
        historialAsignacionRepository.save(historial);


        return equipo;
    }

    /**
     * Asignar un equipo existente a un usuario
     */
    public EquipoInformatico asignarEquipoAUsuario(Long equipoId, String legajoUsuario) {
        EquipoInformatico equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado: " + equipoId));
        Usuario usuario = usuarioRepository.findByLegajo(legajoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + legajoUsuario));

        // 1. Cerrar registro de historial anterior si existe
        historialAsignacionRepository.findOpenByEquipoInformatico(equipo).ifPresent(historial -> {
            historial.setFechaDevolucion(LocalDateTime.now());
            historialAsignacionRepository.save(historial);
        });

        // 2. Asignar equipo al nuevo usuario
        equipo.setUsuario(usuario);
        equipo.setFechaAsignacion(LocalDate.now());
        equipoRepository.save(equipo);

        // 3. Crear nuevo registro en el historial
        HistorialAsignacion nuevoHistorial = new HistorialAsignacion();
        nuevoHistorial.setEquipoInformatico(equipo);
        nuevoHistorial.setUsuario(usuario);
        nuevoHistorial.setFechaAsignacion(LocalDateTime.now());
        historialAsignacionRepository.save(nuevoHistorial);

        return equipo;
    }

    /**
     * Desasignar un equipo de su usuario actual
     */
    public EquipoInformatico desasignarEquipo(Long equipoId) {
        EquipoInformatico equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado: " + equipoId));

        // 1. Cerrar registro de historial actual
        historialAsignacionRepository.findOpenByEquipoInformatico(equipo).ifPresent(historial -> {
            historial.setFechaDevolucion(LocalDateTime.now());
            historialAsignacionRepository.save(historial);
        });

        // 2. Desasignar usuario del equipo
        equipo.setUsuario(null);
        equipo.setFechaAsignacion(null);
        
        return equipoRepository.save(equipo);
    }

    /**
     * Actualizar un equipo informático existente
     */
    public EquipoInformatico actualizarEquipo(Long equipoId, EquipoInformatico.TipoEquipo tipo, String marca, String modelo,
                                             String numeroSerie, String numeroInventario,
                                             EquipoInformatico.EstadoEquipo estado, String observaciones) {
        EquipoInformatico equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado: " + equipoId));

        equipo.setTipo(tipo);
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie(numeroSerie);
        equipo.setNumeroInventario(numeroInventario);
        equipo.setEstado(estado);
        equipo.setObservaciones(observaciones);

        return equipoRepository.save(equipo);
    }

    /**
     * Eliminar un equipo informático
     */
    public void eliminarEquipo(Long equipoId) {
        if (!equipoRepository.existsById(equipoId)) {
            throw new RuntimeException("Equipo no encontrado: " + equipoId);
        }
        
        // Obtener el equipo antes de eliminarlo
        EquipoInformatico equipo = obtenerEquipoPorId(equipoId);
        
        // Si el equipo está asignado a un usuario, desasignarlo primero
        if (equipo.getUsuario() != null) {
            equipo.setUsuario(null);
            equipoRepository.save(equipo);
        }
        
        // Ahora eliminar el equipo
        equipoRepository.deleteById(equipoId);
    }

    /**
     * Obtener todos los equipos de un usuario
     */
    public List<EquipoInformatico> obtenerEquiposPorUsuario(String legajo) {
        return equipoRepository.findByUsuarioLegajo(legajo);
    }

    /**
     * Obtener un equipo por ID
     */
    public EquipoInformatico obtenerEquipoPorId(Long equipoId) {
        return equipoRepository.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado: " + equipoId));
    }

    /**
     * Obtener todos los equipos
     */
    public List<EquipoInformatico> listarTodos() {
        return equipoRepository.findAll();
    }

    /**
     * Verificar si un número de serie ya existe
     */
    public boolean existeNumeroSerie(String numeroSerie) {
        return equipoRepository.findAll().stream()
                .anyMatch(equipo -> numeroSerie.equals(equipo.getNumeroSerie()));
    }

    /**
     * Guardar un equipo (crear o actualizar)
     */
    public EquipoInformatico guardarEquipo(EquipoInformatico equipo) {
        return equipoRepository.save(equipo);
    }

    /**
     * Obtener equipos con filtros y paginación
     */
    public Page<EquipoInformatico> obtenerEquiposConFiltros(String buscar, String filtroSite, 
                                                           String filtroEstado, String filtroActivo, 
                                                           Pageable pageable) {
        // Por ahora retornamos todos los equipos paginados
        // TODO: Implementar filtros específicos en el repositorio
        return equipoRepository.findAll(pageable);
    }

    /**
     * Contar total de equipos
     */
    public long contarTotalEquipos() {
        return equipoRepository.count();
    }

    /**
     * Contar equipos activos
     */
    public long contarEquiposActivos() {
        return equipoRepository.findAll().stream()
                .filter(equipo -> equipo.getActivo() != null && equipo.getActivo())
                .count();
    }

    /**
     * Contar equipos asignados a usuarios
     */
    public long contarEquiposAsignados() {
        return equipoRepository.findAll().stream()
                .filter(equipo -> equipo.getUsuario() != null)
                .count();
    }

    /**
     * Contar equipos disponibles (no asignados)
     */
    public long contarEquiposDisponibles() {
        return equipoRepository.findAll().stream()
                .filter(equipo -> equipo.getUsuario() == null)
                .count();
    }

    /**
     * Contar equipos dados de baja
     */
    public long contarEquiposDadosDeBaja() {
        return equipoRepository.findAll().stream()
                .filter(equipo -> equipo.getEstado() == EquipoInformatico.EstadoEquipo.DADO_DE_BAJA)
                .count();
    }

    /**
     * Obtener todos los equipos con paginación
     */
    public Page<EquipoInformatico> obtenerTodosLosEquipos(Pageable pageable) {
        return equipoRepository.findAll(pageable);
    }

    /**
     * Buscar equipos por término de búsqueda con paginación
     */
    public Page<EquipoInformatico> buscarEquipos(String termino, String filtroTipo, Pageable pageable) {
        EquipoInformatico.TipoEquipo tipoEnum = null;
        if (filtroTipo != null && !filtroTipo.isEmpty()) {
            try {
                tipoEnum = EquipoInformatico.TipoEquipo.valueOf(filtroTipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Log the error or handle it as appropriate, e.g., ignore invalid type
                System.err.println("Tipo de equipo inválido: " + filtroTipo);
            }
        }
        return equipoRepository.findBySearchTerm(termino, tipoEnum, pageable);
    }

    /**
     * Obtener el historial de asignaciones de un equipo.
     */
    public List<HistorialAsignacion> getHistorialPorEquipo(EquipoInformatico equipo) {
        return historialAsignacionRepository.findByEquipoInformaticoOrderByFechaAsignacionDesc(equipo);
    }

    /**
     * Verifica si un equipo asignado tiene historial, y si no, crea el registro inicial.
     * Esto soluciona el problema de los equipos asignados antes de implementar el historial.
     */
    public void verificarYCrearHistorialInicial(EquipoInformatico equipo) {
        // Comprobar si el equipo tiene un usuario asignado
        if (equipo.getUsuario() == null) {
            return; // No hay nada que hacer si no está asignado
        }

        // Comprobar si ya existe algún registro en el historial para este equipo
        List<HistorialAsignacion> historial = getHistorialPorEquipo(equipo);
        if (!historial.isEmpty()) {
            return; // Ya tiene historial, no se necesita hacer nada
        }

        // Si llegamos aquí, el equipo está asignado pero no tiene historial. Hay que crearlo.
        HistorialAsignacion primerHistorial = new HistorialAsignacion();
        primerHistorial.setEquipoInformatico(equipo);
        primerHistorial.setUsuario(equipo.getUsuario());
        
        // Usar la fecha de asignación del equipo como referencia. Si es nula, usar la fecha de registro.
        LocalDateTime fechaDeReferencia;
        if (equipo.getFechaAsignacion() != null) {
            fechaDeReferencia = equipo.getFechaAsignacion().atStartOfDay();
        } else if (equipo.getFechaRegistro() != null) {
            fechaDeReferencia = equipo.getFechaRegistro().atStartOfDay();
        } else {
            fechaDeReferencia = LocalDateTime.now(); // Último recurso
        }
        primerHistorial.setFechaAsignacion(fechaDeReferencia);

        historialAsignacionRepository.save(primerHistorial);
    }
}