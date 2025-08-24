package com.registro.controller;

import com.registro.model.EquipoInformatico;
import com.registro.model.Usuario;
import com.registro.service.EquipoInformaticoService;
import com.registro.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estadisticas")
public class EstadisticasController {

    @Autowired
    private EquipoInformaticoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String mostrarEstadisticas(Model model) {
        // Obtener todos los equipos y usuarios
        List<EquipoInformatico> todosLosEquipos = equipoService.listarTodos();
        List<Usuario> todosLosUsuarios = usuarioService.listarUsuarios();
        
        // Estadísticas básicas
        long totalEquipos = todosLosEquipos.size();
        long totalUsuarios = todosLosUsuarios.size();
        
        // === ESTADÍSTICAS POR SITE ===
        
        // Usuarios por site
        Map<String, Long> usuariosPorSite = todosLosUsuarios.stream()
                .filter(u -> u.getSite() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getSite().name(),
                        Collectors.counting()
                ));
        
        // Equipos por site (basado en el usuario asignado)
        Map<String, Long> equiposPorSite = todosLosUsuarios.stream()
                .filter(u -> u.getSite() != null && u.getEquiposInformaticos() != null)
                .flatMap(u -> u.getEquiposInformaticos().stream()
                        .map(e -> u.getSite().name()))
                .collect(Collectors.groupingBy(
                        site -> site,
                        Collectors.counting()
                ));
        
        // Estadísticas detalladas por site
        Map<String, Map<String, Object>> estadisticasPorSite = new LinkedHashMap<>();
        for (String site : usuariosPorSite.keySet()) {
            Map<String, Object> siteStats = new HashMap<>();
            
            // Usuarios en este site
            List<Usuario> usuariosSite = todosLosUsuarios.stream()
                    .filter(u -> u.getSite() != null && u.getSite().name().equals(site))
                    .collect(Collectors.toList());
            
            // Equipos en este site
            List<EquipoInformatico> equiposSite = usuariosSite.stream()
                    .filter(u -> u.getEquiposInformaticos() != null)
                    .flatMap(u -> u.getEquiposInformaticos().stream())
                    .collect(Collectors.toList());
            
            // Tipos de equipo más comunes en este site
            Map<String, Long> tiposEnSite = equiposSite.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getTipo().name(),
                            Collectors.counting()
                    ));
            
            // Marcas más comunes en este site
            Map<String, Long> marcasEnSite = equiposSite.stream()
                    .filter(e -> e.getMarca() != null && !e.getMarca().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            EquipoInformatico::getMarca,
                            Collectors.counting()
                    ));
            
            // Usuarios que tienen al menos un equipo asignado
            long usuariosConEquipos = usuariosSite.stream()
                    .filter(u -> u.getEquiposInformaticos() != null && !u.getEquiposInformaticos().isEmpty())
                    .count();
            
            siteStats.put("usuarios", usuariosSite.size());
            siteStats.put("equipos", equiposSite.size());
            siteStats.put("usuariosConEquipos", usuariosConEquipos);
            siteStats.put("tiposEquipo", tiposEnSite);
            siteStats.put("marcas", marcasEnSite);
            siteStats.put("promedioEquiposPorUsuario", 
                    usuariosSite.size() > 0 ? (double) equiposSite.size() / usuariosSite.size() : 0.0);
            
            estadisticasPorSite.put(site, siteStats);
        }
        
        // === ESTADÍSTICAS GENERALES ===
        
        // Equipos por tipo (global)
        Map<String, Long> equiposPorTipo = todosLosEquipos.stream()
                .collect(Collectors.groupingBy(
                        equipo -> equipo.getTipo().name(),
                        Collectors.counting()
                ));
        
        // Equipos por estado (global)
        Map<String, Long> equiposPorEstado = todosLosEquipos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEstado().name(),
                        Collectors.counting()
                ));
        
        // Top 5 marcas (global)
        Map<String, Long> topMarcas = todosLosEquipos.stream()
                .filter(e -> e.getMarca() != null && !e.getMarca().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        EquipoInformatico::getMarca,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        
        // Top usuarios con más equipos
        List<Map<String, Object>> usuariosConMasEquipos = todosLosUsuarios.stream()
                .filter(u -> u.getEquiposInformaticos() != null && !u.getEquiposInformaticos().isEmpty())
                .sorted((u1, u2) -> Integer.compare(u2.getEquiposInformaticos().size(), u1.getEquiposInformaticos().size()))
                .limit(5)
                .map(u -> {
                    Map<String, Object> usuarioData = new HashMap<>();
                    usuarioData.put("nombre", u.getNombre() + " " + u.getApellido());
                    usuarioData.put("legajo", u.getLegajo());
                    usuarioData.put("cantidadEquipos", u.getEquiposInformaticos().size());
                    usuarioData.put("site", u.getSite() != null ? u.getSite().name() : "Sin asignar");
                    return usuarioData;
                })
                .collect(Collectors.toList());
        
        // Agregar atributos al modelo
        model.addAttribute("totalEquipos", totalEquipos);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("usuariosPorSite", usuariosPorSite);
        model.addAttribute("equiposPorSite", equiposPorSite);
        model.addAttribute("estadisticasPorSite", estadisticasPorSite);
        model.addAttribute("equiposPorTipo", equiposPorTipo);
        model.addAttribute("equiposPorEstado", equiposPorEstado);
        model.addAttribute("topMarcas", topMarcas);
        model.addAttribute("usuariosConMasEquipos", usuariosConMasEquipos);
        
        return "estadisticas";
    }

    // API endpoints para gráficos dinámicos
    @GetMapping("/api/equipos-por-tipo")
    @ResponseBody
    public Map<String, Long> getEquiposPorTipo() {
        List<EquipoInformatico> equipos = equipoService.listarTodos();
        return equipos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getTipo().getLabel(),
                        Collectors.counting()
                ));
    }

    @GetMapping("/api/equipos-por-estado")
    @ResponseBody
    public Map<String, Long> getEquiposPorEstado() {
        List<EquipoInformatico> equipos = equipoService.listarTodos();
        return equipos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEstado().name(),
                        Collectors.counting()
                ));
    }

    @GetMapping("/api/equipos-por-site")
    @ResponseBody
    public Map<String, Long> getEquiposPorSite() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return usuarios.stream()
                .filter(u -> u.getSite() != null && u.getEquiposInformaticos() != null)
                .flatMap(u -> u.getEquiposInformaticos().stream()
                        .map(e -> u.getSite().name()))
                .collect(Collectors.groupingBy(
                        site -> site,
                        Collectors.counting()
                ));
    }

    @GetMapping("/api/top-marcas")
    @ResponseBody
    public Map<String, Long> getTopMarcas() {
        List<EquipoInformatico> equipos = equipoService.listarTodos();
        return equipos.stream()
                .filter(e -> e.getMarca() != null && !e.getMarca().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        EquipoInformatico::getMarca,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // API endpoints para gráficos de usuarios
    @GetMapping("/api/usuarios-por-site")
    @ResponseBody
    public Map<String, Long> getUsuariosPorSite() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return usuarios.stream()
                .filter(u -> u.getSite() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getSite().name(),
                        Collectors.counting()
                ));
    }

    @GetMapping("/api/usuarios-con-equipos")
    @ResponseBody
    public Map<String, Long> getUsuariosConEquipos() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        long conEquipos = usuarios.stream()
                .filter(u -> u.getEquiposInformaticos() != null && !u.getEquiposInformaticos().isEmpty())
                .count();
        long sinEquipos = usuarios.size() - conEquipos;
        
        Map<String, Long> resultado = new LinkedHashMap<>();
        resultado.put("Con Equipos", conEquipos);
        resultado.put("Sin Equipos", sinEquipos);
        return resultado;
    }

    @GetMapping("/api/usuarios-por-mes")
    @ResponseBody
    public Map<String, Long> getUsuariosPorMes() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        // Simulamos datos por mes (en un caso real usarías fechaCreacion)
        Map<String, Long> resultado = new LinkedHashMap<>();
        resultado.put("Enero", 3L);
        resultado.put("Febrero", 5L);
        resultado.put("Marzo", 8L);
        resultado.put("Abril", 6L);
        resultado.put("Mayo", 7L);
        resultado.put("Junio", (long) usuarios.size());
        return resultado;
    }

    @GetMapping("/api/usuarios-con-archivos")
    @ResponseBody
    public Map<String, Long> getUsuariosConArchivos() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        long conArchivos = usuarios.stream()
                .filter(u -> u.getArchivos() != null && !u.getArchivos().isEmpty())
                .count();
        long sinArchivos = usuarios.size() - conArchivos;
        
        Map<String, Long> resultado = new LinkedHashMap<>();
        resultado.put("Con Archivos", conArchivos);
        resultado.put("Sin Archivos", sinArchivos);
        return resultado;
    }
}
