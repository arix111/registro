// src/main/java/com/registro/controller/EquipoWebController.java
package com.registro.controller;

import com.registro.model.EquipoInformatico;
import com.registro.model.HistorialAsignacion;
import com.registro.model.Site;
import com.registro.service.EquipoInformaticoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/equipos")
public class EquipoWebController {

    private final EquipoInformaticoService equipoService;

    public EquipoWebController(EquipoInformaticoService equipoService) {
        this.equipoService = equipoService;
    }

    /**
     * Página para ver el historial de un equipo
     */
    @GetMapping("/{id}/historial")
    public String verHistorialEquipo(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(id);
            
            // Verificar y crear el historial inicial si es necesario.
            equipoService.verificarYCrearHistorialInicial(equipo);
            
            // Obtener el historial actualizado.
            List<HistorialAsignacion> historial = equipoService.getHistorialPorEquipo(equipo);
            
            model.addAttribute("equipo", equipo);
            model.addAttribute("historial", historial);
            
            return "equipos/historial-equipo";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al buscar el historial: " + e.getMessage());
            return "redirect:/equipos/gestionar";
        }
    }

    /**
     * Página principal del ecosistema de gestión de equipos
     */
    @GetMapping("/gestionar")
    public String gestionarEquipos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String filtroSite,
            @RequestParam(required = false) String filtroEstado,
            @RequestParam(required = false) String filtroActivo,
            Model model) {
        
        // Configurar paginación real usando los parámetros recibidos
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EquipoInformatico> equiposPage = equipoService.obtenerEquiposConFiltros(
            buscar, filtroSite, filtroEstado, filtroActivo, pageable);
        
        // Estadísticas
        long totalEquipos = equipoService.contarTotalEquipos();
        long equiposActivos = equipoService.contarEquiposActivos();
        long equiposAsignados = equipoService.contarEquiposAsignados();
        long equiposDisponibles = equipoService.contarEquiposDisponibles();
        long equiposDadosDeBaja = equipoService.contarEquiposDadosDeBaja();
        
        model.addAttribute("equipos", equiposPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", equiposPage.getTotalPages());
        model.addAttribute("totalElements", equiposPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("buscar", buscar);
        model.addAttribute("filtroSite", filtroSite);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("filtroActivo", filtroActivo);
        
        // Estadísticas
        model.addAttribute("totalEquipos", totalEquipos);
        model.addAttribute("equiposActivos", equiposActivos);
        model.addAttribute("equiposAsignados", equiposAsignados);
        model.addAttribute("equiposDisponibles", equiposDisponibles);
        model.addAttribute("equiposDadosDeBaja", equiposDadosDeBaja);
        
        // Enums para los filtros
        model.addAttribute("sites", Site.values());
        model.addAttribute("estados", EquipoInformatico.EstadoEquipo.values());
        model.addAttribute("tiposEquipo", EquipoInformatico.TipoEquipo.values());
        
        // Nuevo equipo para el formulario
        model.addAttribute("nuevoEquipo", new EquipoInformatico());
        
        return "equipos/gestionar";
    }

    /**
     * Página del Dashboard Ejecutivo separado
     */
    @GetMapping("/dashboard")
    public String dashboardEjecutivo(Model model) {
        // Estadísticas
        long totalEquipos = equipoService.contarTotalEquipos();
        long equiposActivos = equipoService.contarEquiposActivos();
        long equiposAsignados = equipoService.contarEquiposAsignados();
        long equiposDisponibles = equipoService.contarEquiposDisponibles();
        long equiposDadosDeBaja = equipoService.contarEquiposDadosDeBaja();
        
        model.addAttribute("totalEquipos", totalEquipos);
        model.addAttribute("equiposActivos", equiposActivos);
        model.addAttribute("equiposAsignados", equiposAsignados);
        model.addAttribute("equiposDisponibles", equiposDisponibles);
        model.addAttribute("equiposDadosDeBaja", equiposDadosDeBaja);
        
        return "equipos/dashboard";
    }

    @GetMapping("/estado")
    public String mostrarEstadoEquipos(Model model) {
        java.util.Map<String, Object> stats = equipoService.getEquipoStatistics();
        model.addAllAttributes(stats);
        return "equipos/estado-equipos";
    }

    /**
     * Crear nuevo equipo
     */
    @PostMapping("/crear")
    public String crearEquipo(
            @RequestParam EquipoInformatico.TipoEquipo tipo,
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String numeroInventario,
            @RequestParam Site site,
            @RequestParam EquipoInformatico.EstadoEquipo estado,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes) {
        
        try {
            EquipoInformatico equipo = new EquipoInformatico();
            equipo.setTipo(tipo);
            equipo.setMarca(marca.toUpperCase());
            equipo.setModelo(modelo.toUpperCase());
            equipo.setNumeroSerie(numeroSerie != null ? numeroSerie.toUpperCase() : null);
            equipo.setNumeroInventario(numeroInventario != null ? numeroInventario.toUpperCase() : null);
            equipo.setSite(site);
            equipo.setFechaRegistro(LocalDate.now());
            equipo.setEstado(estado);
            // Establecer activo basado en el estado
            equipo.setActivo(estado == EquipoInformatico.EstadoEquipo.ACTIVO);
            equipo.setObservaciones(observaciones != null ? observaciones.toUpperCase() : null);
            
            equipoService.guardarEquipo(equipo);
            
            redirectAttributes.addFlashAttribute("success", 
                "Equipo creado exitosamente: " + tipo.getLabel() + " " + marca + " " + modelo);
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear el equipo: " + e.getMessage());
        }
        
        return "redirect:/equipos/gestionar";
    }

    /**
     * Editar equipo
     */
    @PostMapping("/editar/{id}")
    public String editarEquipo(
            @PathVariable Long id,
            @RequestParam EquipoInformatico.TipoEquipo tipo,
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam(required = false) String numeroSerie,
            @RequestParam(required = false) String numeroInventario,
            @RequestParam Site site,
            @RequestParam EquipoInformatico.EstadoEquipo estado,
            @RequestParam Boolean activo,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes) {
        
        try {
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(id);
            equipo.setTipo(tipo);
            equipo.setMarca(marca.toUpperCase());
            equipo.setModelo(modelo.toUpperCase());
            equipo.setNumeroSerie(numeroSerie != null ? numeroSerie.toUpperCase() : null);
            equipo.setNumeroInventario(numeroInventario != null ? numeroInventario.toUpperCase() : null);
            equipo.setSite(site);
            equipo.setEstado(estado);
            equipo.setActivo(activo);
            equipo.setObservaciones(observaciones != null ? observaciones.toUpperCase() : null);
            
            equipoService.guardarEquipo(equipo);
            
            redirectAttributes.addFlashAttribute("success", 
                "Equipo actualizado exitosamente");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al actualizar el equipo: " + e.getMessage());
        }
        
        return "redirect:/equipos/gestionar";
    }

    /**
     * Eliminar equipo
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(id);
            String descripcion = equipo.getTipo().getLabel() + " " + equipo.getMarca() + " " + equipo.getModelo();
            
            equipoService.eliminarEquipo(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Equipo eliminado exitosamente: " + descripcion);
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al eliminar el equipo: " + e.getMessage());
        }
        
        return "redirect:/equipos/gestionar";
    }

    /**
     * Ver detalles de un equipo
     */
    @GetMapping("/detalle/{id}")
    public String verDetalleEquipo(@PathVariable Long id, Model model) {
        try {
            EquipoInformatico equipo = equipoService.obtenerEquipoPorId(id);
            model.addAttribute("equipo", equipo);
            return "equipos/detalle";
        } catch (Exception e) {
            model.addAttribute("error", "Equipo no encontrado");
            return "redirect:/equipos/gestionar";
        }
    }

    /**
     * Exportar equipos a Excel
     */
    @GetMapping("/exportar/excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=dashboard_ejecutivo_" + 
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx");
        
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            
            // ===== HOJA 1: DASHBOARD EJECUTIVO AGRUPADO POR SITE =====
            org.apache.poi.xssf.usermodel.XSSFSheet resumenSheet = workbook.createSheet("Dashboard Ejecutivo");
            
            // Crear estilos
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            
            org.apache.poi.ss.usermodel.CellStyle siteHeaderStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font siteHeaderFont = workbook.createFont();
            siteHeaderFont.setBold(true);
            siteHeaderFont.setFontHeightInPoints((short) 14);
            siteHeaderFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            siteHeaderStyle.setFont(siteHeaderFont);
            siteHeaderStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_GREEN.getIndex());
            siteHeaderStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            org.apache.poi.ss.usermodel.CellStyle tableHeaderStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font tableHeaderFont = workbook.createFont();
            tableHeaderFont.setBold(true);
            tableHeaderFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            tableHeaderStyle.setFont(tableHeaderFont);
            tableHeaderStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            tableHeaderStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            // Título principal
            org.apache.poi.ss.usermodel.Row titleRow = resumenSheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Dashboard Ejecutivo de Equipos por Site");
            titleCell.setCellStyle(titleStyle);
            
            // Fecha
            org.apache.poi.ss.usermodel.Row dateRow = resumenSheet.createRow(1);
            dateRow.createCell(0).setCellValue("Generado el: " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            // Headers para las tablas por site
            String[] tableHeaders = {"Tipo de Equipo", "Total", "Activos", "Inactivos", "En Reparación", "En Mantenimiento", "Dados de Baja", "Asignados", "Disponibles", "% Activos", "% Asignados"};
            
            // Obtener equipos y agrupar por site y tipo
            List<EquipoInformatico> equipos = equipoService.listarTodos();
            java.util.Map<String, java.util.Map<String, java.util.Map<String, Object>>> siteData = new java.util.LinkedHashMap<>();
            
            // Agrupar equipos por site y tipo
            for (EquipoInformatico equipo : equipos) {
                String siteName = equipo.getSite() != null ? equipo.getSite().getLabel() : "Sin ubicación";
                String tipoEquipo = equipo.getTipo() != null ? equipo.getTipo().getLabel() : "Sin especificar";
                
                siteData.computeIfAbsent(siteName, k -> new java.util.LinkedHashMap<>())
                        .computeIfAbsent(tipoEquipo, k -> {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("total", 0);
                            data.put("activos", 0);
                            data.put("dadosDeBaja", 0);
                            data.put("enReparacion", 0);
                            data.put("enMantenimiento", 0);
                            data.put("asignados", 0);
                            data.put("disponibles", 0);
                            return data;
                        });
                
                java.util.Map<String, Object> tipoData = siteData.get(siteName).get(tipoEquipo);
                tipoData.put("total", (Integer) tipoData.get("total") + 1);
                
                if (equipo.getActivo() != null && equipo.getActivo()) {
                    tipoData.put("activos", (Integer) tipoData.get("activos") + 1);
                }
                
                if (equipo.getEstado() != null) {
                    switch (equipo.getEstado()) {
                        case DADO_DE_BAJA:
                            tipoData.put("dadosDeBaja", (Integer) tipoData.get("dadosDeBaja") + 1);
                            break;
                        case EN_REPARACION:
                            tipoData.put("enReparacion", (Integer) tipoData.get("enReparacion") + 1);
                            break;
                        case EN_MANTENIMIENTO:
                            tipoData.put("enMantenimiento", (Integer) tipoData.get("enMantenimiento") + 1);
                            break;
                        case ACTIVO:
                            // Los equipos activos ya se cuentan en la condición anterior
                            break;
                        case INACTIVO:
                            // Los equipos inactivos no se cuentan como activos
                            break;
                    }
                }
                
                if (equipo.getUsuario() != null) {
                    tipoData.put("asignados", (Integer) tipoData.get("asignados") + 1);
                } else {
                    tipoData.put("disponibles", (Integer) tipoData.get("disponibles") + 1);
                }
            }
            
            // Crear tablas separadas por site
            int currentRow = 3;
            
            for (java.util.Map.Entry<String, java.util.Map<String, java.util.Map<String, Object>>> siteEntry : siteData.entrySet()) {
                String siteName = siteEntry.getKey();
                
                // Título del site (fila de encabezado verde)
                org.apache.poi.ss.usermodel.Row siteHeaderRow = resumenSheet.createRow(currentRow++);
                org.apache.poi.ss.usermodel.Cell siteHeaderCell = siteHeaderRow.createCell(0);
                siteHeaderCell.setCellValue("SITE: " + siteName.toUpperCase());
                siteHeaderCell.setCellStyle(siteHeaderStyle);
                
                // Fusionar celdas para el encabezado del site
                resumenSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                    currentRow - 1, currentRow - 1, 0, tableHeaders.length - 1));
                
                // Headers de la tabla para este site
                org.apache.poi.ss.usermodel.Row tableHeaderRow = resumenSheet.createRow(currentRow++);
                for (int i = 0; i < tableHeaders.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = tableHeaderRow.createCell(i);
                    cell.setCellValue(tableHeaders[i]);
                    cell.setCellStyle(tableHeaderStyle);
                }
                
                // Datos de equipos para este site
                for (java.util.Map.Entry<String, java.util.Map<String, Object>> tipoEntry : siteEntry.getValue().entrySet()) {
                    String tipoEquipo = tipoEntry.getKey();
                    java.util.Map<String, Object> data = tipoEntry.getValue();
                    
                    org.apache.poi.ss.usermodel.Row dataRow = resumenSheet.createRow(currentRow++);
                    
                    // Calcular valores profesionales
                    int total = (Integer) data.get("total");
                    int activos = (Integer) data.get("activos");
                    int inactivos = total - activos;
                    int asignados = (Integer) data.get("asignados");
                    
                    // Calcular porcentajes profesionales
                    double porcentajeActivos = total > 0 ? (double) activos / total * 100 : 0;
                    double porcentajeAsignados = total > 0 ? (double) asignados / total * 100 : 0;
                    
                    // Llenar datos: {"Tipo de Equipo", "Total", "Activos", "Inactivos", "En Reparación", "En Mantenimiento", "Dados de Baja", "Asignados", "Disponibles", "% Activos", "% Asignados"}
                    dataRow.createCell(0).setCellValue(tipoEquipo);
                    dataRow.createCell(1).setCellValue(total);
                    dataRow.createCell(2).setCellValue(activos);
                    dataRow.createCell(3).setCellValue(inactivos);
                    dataRow.createCell(4).setCellValue((Integer) data.get("enReparacion"));
                    dataRow.createCell(5).setCellValue((Integer) data.get("enMantenimiento"));
                    dataRow.createCell(6).setCellValue((Integer) data.get("dadosDeBaja"));
                    dataRow.createCell(7).setCellValue(asignados);
                    dataRow.createCell(8).setCellValue((Integer) data.get("disponibles"));
                    dataRow.createCell(9).setCellValue(String.format("%.1f%%", porcentajeActivos));
                    dataRow.createCell(10).setCellValue(String.format("%.1f%%", porcentajeAsignados));
                }
                
                // Espacio entre tablas
                currentRow += 2;
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < tableHeaders.length; i++) {
                resumenSheet.autoSizeColumn(i);
            }
            
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * Exportar equipos a PDF
     */
    @GetMapping("/exportar/pdf")
    public void exportarPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=dashboard_ejecutivo_" + 
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
        
        try (com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(response.getOutputStream());
             com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
             com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf)) {
            
            // Título principal
            document.add(new com.itextpdf.layout.element.Paragraph("Dashboard Ejecutivo de Equipos por Site")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(20));
            
            // Fecha
            document.add(new com.itextpdf.layout.element.Paragraph("Generado el: " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(30));
            
            // Headers para las tablas por site
            String[] tableHeaders = {"Tipo de Equipo", "Total", "Activos", "Inactivos", "En Reparación", "En Mantenimiento", "Dados de Baja", "Asignados", "Disponibles", "% Activos", "% Asignados"};
            
            // Obtener equipos y agrupar por site y tipo (igual que en Excel)
            List<EquipoInformatico> equipos = equipoService.listarTodos();
            java.util.Map<String, java.util.Map<String, java.util.Map<String, Object>>> siteData = new java.util.LinkedHashMap<>();
            
            // Agrupar equipos por site y tipo
            for (EquipoInformatico equipo : equipos) {
                String siteName = equipo.getSite() != null ? equipo.getSite().getLabel() : "Sin ubicación";
                String tipoEquipo = equipo.getTipo() != null ? equipo.getTipo().getLabel() : "Sin especificar";
                
                siteData.computeIfAbsent(siteName, k -> new java.util.LinkedHashMap<>())
                        .computeIfAbsent(tipoEquipo, k -> {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("total", 0);
                            data.put("activos", 0);
                            data.put("dadosDeBaja", 0);
                            data.put("enReparacion", 0);
                            data.put("enMantenimiento", 0);
                            data.put("asignados", 0);
                            data.put("disponibles", 0);
                            return data;
                        });
                
                java.util.Map<String, Object> tipoData = siteData.get(siteName).get(tipoEquipo);
                tipoData.put("total", (Integer) tipoData.get("total") + 1);
                
                if (equipo.getActivo() != null && equipo.getActivo()) {
                    tipoData.put("activos", (Integer) tipoData.get("activos") + 1);
                }
                
                if (equipo.getEstado() != null) {
                    switch (equipo.getEstado()) {
                        case DADO_DE_BAJA:
                            tipoData.put("dadosDeBaja", (Integer) tipoData.get("dadosDeBaja") + 1);
                            break;
                        case EN_REPARACION:
                            tipoData.put("enReparacion", (Integer) tipoData.get("enReparacion") + 1);
                            break;
                        case EN_MANTENIMIENTO:
                            tipoData.put("enMantenimiento", (Integer) tipoData.get("enMantenimiento") + 1);
                            break;
                        case ACTIVO:
                        case INACTIVO:
                            // Los equipos activos/inactivos ya se cuentan en la condición anterior
                            break;
                    }
                }
                
                if (equipo.getUsuario() != null) {
                    tipoData.put("asignados", (Integer) tipoData.get("asignados") + 1);
                } else {
                    tipoData.put("disponibles", (Integer) tipoData.get("disponibles") + 1);
                }
            }
            
            // Crear tablas separadas por site en PDF
            for (java.util.Map.Entry<String, java.util.Map<String, java.util.Map<String, Object>>> siteEntry : siteData.entrySet()) {
                String siteName = siteEntry.getKey();
                
                // Título del site
                document.add(new com.itextpdf.layout.element.Paragraph("SITE: " + siteName.toUpperCase())
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    .setPadding(10)
                    .setMarginTop(20)
                    .setMarginBottom(10));
                
                // Crear tabla para este site
                com.itextpdf.layout.element.Table siteTable = new com.itextpdf.layout.element.Table(new float[]{2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
                siteTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                
                // Headers de la tabla para este site
                for (String header : tableHeaders) {
                    siteTable.addHeaderCell(new com.itextpdf.layout.element.Cell()
                        .add(new com.itextpdf.layout.element.Paragraph(header).setBold().setFontSize(9))
                        .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.DARK_GRAY)
                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE));
                }
                
                // Datos de equipos para este site
                for (java.util.Map.Entry<String, java.util.Map<String, Object>> tipoEntry : siteEntry.getValue().entrySet()) {
                    String tipoEquipo = tipoEntry.getKey();
                    java.util.Map<String, Object> data = tipoEntry.getValue();
                    
                    // Calcular valores profesionales
                    int total = (Integer) data.get("total");
                    int activos = (Integer) data.get("activos");
                    int inactivos = total - activos;
                    int asignados = (Integer) data.get("asignados");
                    
                    // Calcular porcentajes profesionales
                    double porcentajeActivos = total > 0 ? (double) activos / total * 100 : 0;
                    double porcentajeAsignados = total > 0 ? (double) asignados / total * 100 : 0;
                    
                    // Llenar datos: {"Tipo de Equipo", "Total", "Activos", "Inactivos", "En Reparación", "En Mantenimiento", "Dados de Baja", "Asignados", "Disponibles", "% Activos", "% Asignados"}
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(tipoEquipo).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(total)).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(activos)).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(inactivos)).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf((Integer) data.get("enReparacion"))).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf((Integer) data.get("enMantenimiento"))).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf((Integer) data.get("dadosDeBaja"))).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf(asignados)).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.valueOf((Integer) data.get("disponibles"))).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.format("%.1f%%", porcentajeActivos)).setFontSize(9)));
                    siteTable.addCell(new com.itextpdf.layout.element.Cell().add(new com.itextpdf.layout.element.Paragraph(String.format("%.1f%%", porcentajeAsignados)).setFontSize(9)));
                }
                
                // Agregar tabla del site al documento
                document.add(siteTable);
            }
        }
    }

    /**
     * Debug endpoint para verificar datos del reporte
     */
    @GetMapping("/debug/reporte")
    @ResponseBody
    public java.util.Map<String, Object> debugReporteData() {
        try {
            // Obtener equipos y agrupar por site y tipo (igual que Excel y PDF)
            List<EquipoInformatico> equipos = equipoService.listarTodos();
            java.util.Map<String, Object> debug = new java.util.HashMap<>();
            debug.put("totalEquipos", equipos.size());
            debug.put("equipos", equipos.stream().map(e -> {
                java.util.Map<String, Object> eq = new java.util.HashMap<>();
                eq.put("id", e.getId());
                eq.put("site", e.getSite() != null ? e.getSite().getLabel() : "Sin ubicación");
                eq.put("tipo", e.getTipo() != null ? e.getTipo().getLabel() : "Sin especificar");
                eq.put("activo", e.getActivo());
                eq.put("estado", e.getEstado() != null ? e.getEstado().getLabel() : "Sin estado");
                eq.put("usuario", e.getUsuario() != null ? e.getUsuario().getNombre() : "Sin asignar");
                // Agregar números de serie e inventario para la búsqueda
                eq.put("numeroSerie", e.getNumeroSerie() != null ? e.getNumeroSerie() : "");
                eq.put("numeroInventario", e.getNumeroInventario() != null ? e.getNumeroInventario() : "");
                eq.put("marca", e.getMarca() != null ? e.getMarca() : "Sin marca");
                eq.put("modelo", e.getModelo() != null ? e.getModelo() : "Sin modelo");
                return eq;
            }).collect(java.util.stream.Collectors.toList()));
            return debug;
        } catch (Exception e) {
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
    
    /**
     * Generar reporte HTML interactivo profesional
     */
    @GetMapping("/exportar/reporte")
    public String generarReporteHTML(Model model) {
        try {
            // Obtener equipos y agrupar por site y tipo (igual que Excel y PDF)
            List<EquipoInformatico> equipos = equipoService.listarTodos();
            java.util.Map<String, java.util.Map<String, java.util.Map<String, Object>>> siteData = new java.util.LinkedHashMap<>();
            
            // Agrupar equipos por site y tipo
            for (EquipoInformatico equipo : equipos) {
                String siteName = equipo.getSite() != null ? equipo.getSite().getLabel() : "Sin ubicación";
                String tipoEquipo = equipo.getTipo() != null ? equipo.getTipo().getLabel() : "Sin especificar";
                
                siteData.computeIfAbsent(siteName, k -> new java.util.LinkedHashMap<>())
                        .computeIfAbsent(tipoEquipo, k -> {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("total", 0);
                            data.put("activos", 0);
                            data.put("dadosDeBaja", 0);
                            data.put("enReparacion", 0);
                            data.put("enMantenimiento", 0);
                            data.put("asignados", 0);
                            data.put("disponibles", 0);
                            return data;
                        });
                
                java.util.Map<String, Object> tipoData = siteData.get(siteName).get(tipoEquipo);
                tipoData.put("total", (Integer) tipoData.get("total") + 1);
                
                if (equipo.getActivo() != null && equipo.getActivo()) {
                    tipoData.put("activos", (Integer) tipoData.get("activos") + 1);
                }
                
                if (equipo.getEstado() != null) {
                    switch (equipo.getEstado()) {
                        case DADO_DE_BAJA:
                            tipoData.put("dadosDeBaja", (Integer) tipoData.get("dadosDeBaja") + 1);
                            break;
                        case EN_REPARACION:
                            tipoData.put("enReparacion", (Integer) tipoData.get("enReparacion") + 1);
                            break;
                        case EN_MANTENIMIENTO:
                            tipoData.put("enMantenimiento", (Integer) tipoData.get("enMantenimiento") + 1);
                            break;
                        case ACTIVO:
                        case INACTIVO:
                            // Los equipos activos/inactivos ya se cuentan en la condición anterior
                            break;
                    }
                }
                
                if (equipo.getUsuario() != null) {
                    tipoData.put("asignados", (Integer) tipoData.get("asignados") + 1);
                } else {
                    tipoData.put("disponibles", (Integer) tipoData.get("disponibles") + 1);
                }
            }
            
            // Preparar datos profesionales para la vista
            java.util.List<java.util.Map<String, Object>> reporteData = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, java.util.Map<String, java.util.Map<String, Object>>> siteEntry : siteData.entrySet()) {
                String siteName = siteEntry.getKey();
                for (java.util.Map.Entry<String, java.util.Map<String, Object>> tipoEntry : siteEntry.getValue().entrySet()) {
                    String tipoEquipo = tipoEntry.getKey();
                    java.util.Map<String, Object> data = tipoEntry.getValue();
                    
                    // Calcular valores profesionales
                    int total = (Integer) data.get("total");
                    int activos = (Integer) data.get("activos");
                    int inactivos = total - activos;
                    int asignados = (Integer) data.get("asignados");
                    
                    // Calcular porcentajes profesionales
                    double porcentajeActivos = total > 0 ? (double) activos / total * 100 : 0;
                    double porcentajeAsignados = total > 0 ? (double) asignados / total * 100 : 0;
                    
                    java.util.Map<String, Object> fila = new java.util.HashMap<>();
                    fila.put("site", siteName);
                    fila.put("tipoEquipo", tipoEquipo);
                    fila.put("total", total);
                    fila.put("activos", activos);
                    fila.put("inactivos", inactivos);
                    fila.put("enReparacion", data.get("enReparacion"));
                    fila.put("enMantenimiento", data.get("enMantenimiento"));
                    fila.put("dadosDeBaja", data.get("dadosDeBaja"));
                    fila.put("asignados", asignados);
                    fila.put("disponibles", data.get("disponibles"));
                    fila.put("porcentajeActivos", String.format("%.1f", porcentajeActivos));
                    fila.put("porcentajeAsignados", String.format("%.1f", porcentajeAsignados));
                    
                    reporteData.add(fila);
                }
            }
            
            model.addAttribute("reporteData", reporteData);
            model.addAttribute("fechaGeneracion", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
            return "equipos/reporte-ejecutivo";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al generar el reporte: " + e.getMessage());
            return "equipos/dashboard";
        }
    }
}