package com.registro.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import com.registro.model.EquipoInformatico;
import com.registro.model.Site;
import com.registro.model.Usuario;
import com.registro.repository.IEquipoInformaticoRepository;
import com.registro.repository.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private IEquipoInformaticoRepository equipoRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    public byte[] generateExcelReport() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // Crear reporte ejecutivo general
        createExecutiveSummarySheet(workbook);
        
        // Crear hojas profesionales detalladas por cada site
        createDetailedSiteReports(workbook);
        
        // Crear consolidado de equipos por marca/modelo
        createEquipmentSummarySheet(workbook);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    private void createExecutiveSummarySheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("📊 RESUMEN EJECUTIVO");
        
        CellStyle titleStyle = createExecutiveTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle highlightStyle = createHighlightStyle(workbook);
        
        int rowNum = 0;
        
        // Título principal del reporte
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE EJECUTIVO DEL SISTEMA DE EQUIPOS INFORMÁTICOS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        
        rowNum++;
        
        // Fecha y hora del reporte
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Generado el: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm 'hs'")));
        dateCell.setCellStyle(dataStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 6));
        
        rowNum += 2;
        
        // Resumen general del sistema
        Row generalHeaderRow = sheet.createRow(rowNum++);
        Cell generalHeaderCell = generalHeaderRow.createCell(0);
        generalHeaderCell.setCellValue("📈 RESUMEN GENERAL DEL SISTEMA");
        generalHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));
        
        rowNum++;
        
        // Obtener totales generales
        long totalEquipos = equipoRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long usuariosConEquipos = equipoRepository.findAll().stream()
            .map(EquipoInformatico::getUsuario)
            .filter(Objects::nonNull)
            .map(Usuario::getId)
            .distinct()
            .count();
        
        // Mostrar totales
        String[] generalData = {
            "Total de Equipos en el Sistema: " + totalEquipos,
            "Total de Usuarios Registrados: " + totalUsuarios,
            "Usuarios con Equipos Asignados: " + usuariosConEquipos,
            "Usuarios sin Equipos: " + (totalUsuarios - usuariosConEquipos)
        };
        
        for (String data : generalData) {
            Row dataRow = sheet.createRow(rowNum++);
            Cell dataCell = dataRow.createCell(1);
            dataCell.setCellValue(data);
            dataCell.setCellStyle(highlightStyle);
        }
        
        rowNum += 2;
        
        // Resumen por sites
        Row siteHeaderRow = sheet.createRow(rowNum++);
        Cell siteHeaderCell = siteHeaderRow.createCell(0);
        siteHeaderCell.setCellValue("🏢 DISTRIBUCIÓN POR SITES");
        siteHeaderCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));
        
        rowNum++;
        
        // Encabezados de la tabla
        Row tableHeaderRow = sheet.createRow(rowNum++);
        String[] tableHeaders = {"Site", "Usuarios Totales", "Usuarios con Equipos", "Total Equipos", "Equipos por Usuario", "Estado"};
        for (int i = 0; i < tableHeaders.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(tableHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Datos por site
        Map<Site, List<Usuario>> usuariosPorSite = usuarioRepository.findAll()
            .stream()
            .filter(usuario -> usuario.getSite() != null)
            .collect(Collectors.groupingBy(Usuario::getSite));
        
        Map<Site, List<EquipoInformatico>> equiposPorSite = equipoRepository.findAll()
            .stream()
            .filter(equipo -> equipo.getUsuario() != null && equipo.getUsuario().getSite() != null)
            .collect(Collectors.groupingBy(equipo -> equipo.getUsuario().getSite()));
        
        for (Site site : Site.values()) {
            List<Usuario> usuarios = usuariosPorSite.getOrDefault(site, new ArrayList<>());
            List<EquipoInformatico> equipos = equiposPorSite.getOrDefault(site, new ArrayList<>());
            Set<Usuario> usuariosConEquiposSite = equipos.stream()
                .map(EquipoInformatico::getUsuario)
                .collect(Collectors.toSet());
            
            Row siteRow = sheet.createRow(rowNum++);
            
            // Site
            Cell siteCell = siteRow.createCell(0);
            siteCell.setCellValue(site.toString());
            siteCell.setCellStyle(dataStyle);
            
            // Usuarios totales
            Cell totalUsersCell = siteRow.createCell(1);
            totalUsersCell.setCellValue(usuarios.size());
            totalUsersCell.setCellStyle(dataStyle);
            
            // Usuarios con equipos
            Cell usersWithEquipCell = siteRow.createCell(2);
            usersWithEquipCell.setCellValue(usuariosConEquiposSite.size());
            usersWithEquipCell.setCellStyle(dataStyle);
            
            // Total equipos
            Cell totalEquipCell = siteRow.createCell(3);
            totalEquipCell.setCellValue(equipos.size());
            totalEquipCell.setCellStyle(dataStyle);
            
            // Equipos por usuario promedio
            Cell avgEquipCell = siteRow.createCell(4);
            double promedio = usuariosConEquiposSite.size() > 0 ? (double) equipos.size() / usuariosConEquiposSite.size() : 0;
            avgEquipCell.setCellValue(String.format("%.1f", promedio));
            avgEquipCell.setCellStyle(dataStyle);
            
            // Estado
            Cell statusCell = siteRow.createCell(5);
            String estado = usuarios.size() > 0 ? "✅ Activo" : "⚠️ Sin usuarios";
            statusCell.setCellValue(estado);
            statusCell.setCellStyle(dataStyle);
        }
        
        // Ajustar ancho de columnas
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createDetailedSiteReports(Workbook workbook) {
        // Obtener datos agrupados por site
        Map<Site, List<Usuario>> usuariosPorSite = usuarioRepository.findAll()
            .stream()
            .filter(usuario -> usuario.getSite() != null)
            .collect(Collectors.groupingBy(Usuario::getSite));
        
        Map<Site, List<EquipoInformatico>> equiposPorSite = equipoRepository.findAll()
            .stream()
            .filter(equipo -> equipo.getUsuario() != null && equipo.getUsuario().getSite() != null)
            .collect(Collectors.groupingBy(equipo -> equipo.getUsuario().getSite()));
        
        for (Site site : Site.values()) {
            List<Usuario> usuarios = usuariosPorSite.getOrDefault(site, new ArrayList<>());
            List<EquipoInformatico> equipos = equiposPorSite.getOrDefault(site, new ArrayList<>());
            
            if (usuarios.isEmpty()) {
                continue; // Saltar sites sin usuarios
            }
            
            Sheet sheet = workbook.createSheet("🏢 " + site.toString());
            CellStyle titleStyle = createExecutiveTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle highlightStyle = createHighlightStyle(workbook);
            CellStyle sectionStyle = createSectionHeaderStyle(workbook);
            
            int rowNum = 0;
            
            // Título del site
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DETALLADO - " + site.toString());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
            
            rowNum++;
            
            // Resumen del site - EXACTAMENTE LO QUE PEDISTE
            Row summaryRow = sheet.createRow(rowNum++);
            Cell summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue("📍 El site de " + site.toString() + " tiene " + usuarios.size() + " usuarios registrados");
            summaryCell.setCellStyle(highlightStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 8));
            
            // Estadísticas adicionales del site
            Row statsRow = sheet.createRow(rowNum++);
            Cell statsCell = statsRow.createCell(0);
            Set<Usuario> usuariosConEquipos = equipos.stream()
                .map(EquipoInformatico::getUsuario)
                .collect(Collectors.toSet());
            statsCell.setCellValue("📊 " + usuariosConEquipos.size() + " usuarios tienen equipos asignados (" + equipos.size() + " equipos en total)");
            statsCell.setCellStyle(dataStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 8));
            
            rowNum += 2;
            
            // Sección de usuarios y equipos
            Row sectionRow = sheet.createRow(rowNum++);
            Cell sectionCell = sectionRow.createCell(0);
            sectionCell.setCellValue("👥 LISTADO DETALLADO DE USUARIOS Y SUS EQUIPOS");
            sectionCell.setCellStyle(sectionStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 8));
            
            rowNum++;
            
            // Encabezados de la tabla
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Legajo", "Nombre Completo", "Email", "Teléfono", "Tipo Equipo", "Marca", "Modelo", "N° Serie", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos de usuarios y equipos - FORMATO PROFESIONAL
            for (Usuario usuario : usuarios.stream()
                    .sorted(Comparator.comparing(Usuario::getLegajo, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList())) {
                
                // Obtener equipos del usuario
                List<EquipoInformatico> equiposUsuario = equipos.stream()
                    .filter(equipo -> equipo.getUsuario() != null && equipo.getUsuario().getId().equals(usuario.getId()))
                    .sorted(Comparator.comparing(e -> e.getTipo() != null ? e.getTipo().toString() : ""))
                    .collect(Collectors.toList());
                
                if (equiposUsuario.isEmpty()) {
                    // Usuario sin equipos
                    Row row = sheet.createRow(rowNum++);
                    fillProfessionalUserData(row, usuario, null, dataStyle);
                } else {
                    // Usuario con equipos - una fila por equipo
                    for (EquipoInformatico equipo : equiposUsuario) {
                        Row row = sheet.createRow(rowNum++);
                        fillProfessionalUserData(row, usuario, equipo, dataStyle);
                    }
                }
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Agregar resumen al final
            rowNum += 2;
            Row finalSummaryRow = sheet.createRow(rowNum++);
            Cell finalSummaryCell = finalSummaryRow.createCell(0);
            finalSummaryCell.setCellValue("📋 Resumen: " + usuarios.size() + " usuarios, " + equipos.size() + " equipos, " + 
                equipos.stream().map(EquipoInformatico::getMarca).filter(Objects::nonNull).collect(Collectors.toSet()).size() + " marcas diferentes");
            finalSummaryCell.setCellStyle(highlightStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 8));
        }
    }
    
    private void fillProfessionalUserData(Row row, Usuario usuario, EquipoInformatico equipo, CellStyle dataStyle) {
        // Legajo
        Cell legajoCell = row.createCell(0);
        legajoCell.setCellValue(usuario.getLegajo() != null ? usuario.getLegajo() : "");
        legajoCell.setCellStyle(dataStyle);
        
        // Nombre completo
        Cell nombreCell = row.createCell(1);
        nombreCell.setCellValue(usuario.getNombre() + " " + usuario.getApellido());
        nombreCell.setCellStyle(dataStyle);
        
        // Email
        Cell emailCell = row.createCell(2);
        emailCell.setCellValue(usuario.getCorreoElectronico() != null ? usuario.getCorreoElectronico() : "");
        emailCell.setCellStyle(dataStyle);
        
        // Teléfono
        Cell telefonoCell = row.createCell(3);
        telefonoCell.setCellValue(usuario.getTelefono() != null ? usuario.getTelefono() : "");
        telefonoCell.setCellStyle(dataStyle);
        
        if (equipo != null) {
            // Tipo Equipo
            Cell tipoCell = row.createCell(4);
            tipoCell.setCellValue(equipo.getTipo() != null ? equipo.getTipo().toString() : "");
            tipoCell.setCellStyle(dataStyle);
            
            // Marca
            Cell marcaCell = row.createCell(5);
            marcaCell.setCellValue(equipo.getMarca() != null ? equipo.getMarca() : "");
            marcaCell.setCellStyle(dataStyle);
            
            // Modelo
            Cell modeloCell = row.createCell(6);
            modeloCell.setCellValue(equipo.getModelo() != null ? equipo.getModelo() : "");
            modeloCell.setCellStyle(dataStyle);
            
            // Número de serie
            Cell serieCell = row.createCell(7);
            serieCell.setCellValue(equipo.getNumeroSerie() != null ? equipo.getNumeroSerie() : "");
            serieCell.setCellStyle(dataStyle);
            
            // Estado
            Cell estadoCell = row.createCell(8);
            estadoCell.setCellValue(equipo.getEstado() != null ? equipo.getEstado().toString() : "");
            estadoCell.setCellStyle(dataStyle);
        } else {
            // Usuario sin equipos
            for (int i = 4; i <= 8; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(i == 4 ? "🚫 Sin equipos asignados" : "");
                cell.setCellStyle(dataStyle);
            }
        }
    }
    
    private void createEquipmentSummarySheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("📦 RESUMEN DE EQUIPOS");
        
        CellStyle titleStyle = createExecutiveTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RESUMEN CONSOLIDADO DE EQUIPOS POR MARCA Y MODELO");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        
        rowNum += 2;
        
        // Encabezados
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Marca", "Modelo", "Cantidad", "Sites", "Usuarios", "Estado Predominante"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Agrupar equipos por marca y modelo
        Map<String, Map<String, List<EquipoInformatico>>> equiposPorMarcaModelo = equipoRepository.findAll()
            .stream()
            .filter(equipo -> equipo.getMarca() != null && equipo.getModelo() != null)
            .collect(Collectors.groupingBy(
                EquipoInformatico::getMarca,
                Collectors.groupingBy(EquipoInformatico::getModelo)
            ));
        
        for (Map.Entry<String, Map<String, List<EquipoInformatico>>> marcaEntry : equiposPorMarcaModelo.entrySet()) {
            String marca = marcaEntry.getKey();
            
            for (Map.Entry<String, List<EquipoInformatico>> modeloEntry : marcaEntry.getValue().entrySet()) {
                String modelo = modeloEntry.getKey();
                List<EquipoInformatico> equipos = modeloEntry.getValue();
                
                Row row = sheet.createRow(rowNum++);
                
                // Marca
                Cell marcaCell = row.createCell(0);
                marcaCell.setCellValue(marca);
                marcaCell.setCellStyle(dataStyle);
                
                // Modelo
                Cell modeloCell = row.createCell(1);
                modeloCell.setCellValue(modelo);
                modeloCell.setCellStyle(dataStyle);
                
                // Cantidad
                Cell cantidadCell = row.createCell(2);
                cantidadCell.setCellValue(equipos.size());
                cantidadCell.setCellStyle(dataStyle);
                
                // Sites
                Set<String> sites = equipos.stream()
                    .filter(equipo -> equipo.getUsuario() != null && equipo.getUsuario().getSite() != null)
                    .map(equipo -> equipo.getUsuario().getSite().toString())
                    .collect(Collectors.toSet());
                Cell sitesCell = row.createCell(3);
                sitesCell.setCellValue(String.join(", ", sites));
                sitesCell.setCellStyle(dataStyle);
                
                // Usuarios únicos
                Set<String> usuarios = equipos.stream()
                    .filter(equipo -> equipo.getUsuario() != null)
                    .map(equipo -> equipo.getUsuario().getLegajo())
                    .collect(Collectors.toSet());
                Cell usuariosCell = row.createCell(4);
                usuariosCell.setCellValue(usuarios.size());
                usuariosCell.setCellStyle(dataStyle);
                
                // Estado predominante
                String estadoPredominante = equipos.stream()
                    .collect(Collectors.groupingBy(
                        equipo -> equipo.getEstado() != null ? equipo.getEstado().toString() : "Sin estado",
                        Collectors.counting()
                    ))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
                Cell estadoCell = row.createCell(5);
                estadoCell.setCellValue(estadoPredominante);
                estadoCell.setCellStyle(dataStyle);
            }
        }
        
        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    // Estilos profesionales
    private CellStyle createExecutiveTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createSectionHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createHighlightStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    public byte[] generatePdfReport() throws IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Crear documento PDF
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(outputStream);
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
            
            // Configurar márgenes
            document.setMargins(50, 50, 50, 50);
            
            // Crear estilos
            com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            com.itextpdf.kernel.font.PdfFont boldFont = com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            
            // Título principal
            com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph("REPORTE EJECUTIVO DEL SISTEMA DE EQUIPOS INFORMÁTICOS")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(title);
            
            // Fecha y hora
            com.itextpdf.layout.element.Paragraph date = new com.itextpdf.layout.element.Paragraph(
                "Generado el: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm 'hs'")))
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                .setMarginBottom(30);
            document.add(date);
            
            // Resumen general
            addPdfSection(document, "📈 RESUMEN GENERAL DEL SISTEMA", boldFont, font);
            
            long totalEquipos = equipoRepository.count();
            long totalUsuarios = usuarioRepository.count();
            long usuariosConEquipos = equipoRepository.findAll().stream()
                .map(EquipoInformatico::getUsuario)
                .filter(Objects::nonNull)
                .map(Usuario::getId)
                .distinct()
                .count();
            
            String[] generalStats = {
                "• Total de Equipos en el Sistema: " + totalEquipos,
                "• Total de Usuarios Registrados: " + totalUsuarios,
                "• Usuarios con Equipos Asignados: " + usuariosConEquipos,
                "• Usuarios sin Equipos: " + (totalUsuarios - usuariosConEquipos)
            };
            
            for (String stat : generalStats) {
                document.add(new com.itextpdf.layout.element.Paragraph(stat)
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20));
            }
            
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));
            
            // Reportes por site
            addPdfSection(document, "🏢 DISTRIBUCIÓN DETALLADA POR SITES", boldFont, font);
            
            // Obtener datos por site
            Map<Site, List<Usuario>> usuariosPorSite = usuarioRepository.findAll()
                .stream()
                .filter(usuario -> usuario.getSite() != null)
                .collect(Collectors.groupingBy(Usuario::getSite));
            
            Map<Site, List<EquipoInformatico>> equiposPorSite = equipoRepository.findAll()
                .stream()
                .filter(equipo -> equipo.getUsuario() != null && equipo.getUsuario().getSite() != null)
                .collect(Collectors.groupingBy(equipo -> equipo.getUsuario().getSite()));
            
            for (Site site : Site.values()) {
                List<Usuario> usuarios = usuariosPorSite.getOrDefault(site, new ArrayList<>());
                List<EquipoInformatico> equipos = equiposPorSite.getOrDefault(site, new ArrayList<>());
                
                if (usuarios.isEmpty()) {
                    continue;
                }
                
                // Título del site - EXACTAMENTE LO QUE PEDISTE
                document.add(new com.itextpdf.layout.element.Paragraph("\n📍 El site de " + site.toString() + " tiene " + usuarios.size() + " usuarios registrados")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
                    .setPadding(10)
                    .setMarginBottom(10));
                
                // Estadísticas del site
                Set<Usuario> usuariosConEquiposSite = equipos.stream()
                    .map(EquipoInformatico::getUsuario)
                    .collect(Collectors.toSet());
                
                document.add(new com.itextpdf.layout.element.Paragraph(
                    "📊 " + usuariosConEquiposSite.size() + " usuarios tienen equipos asignados (" + equipos.size() + " equipos en total)")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginLeft(20)
                    .setMarginBottom(15));
                
                // Crear tabla de usuarios y equipos
                createPdfUserEquipmentTable(document, usuarios, equipos, font, boldFont);
                
                // Salto de página para el siguiente site (excepto el último)
                if (!site.equals(Site.values()[Site.values().length - 1])) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
            }
            
            // Cerrar documento
            document.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new IOException("Error generando reporte PDF: " + e.getMessage(), e);
        }
    }
    
    private void addPdfSection(com.itextpdf.layout.Document document, String title, 
                              com.itextpdf.kernel.font.PdfFont boldFont, com.itextpdf.kernel.font.PdfFont font) {
        document.add(new com.itextpdf.layout.element.Paragraph(title)
            .setFont(boldFont)
            .setFontSize(14)
            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
            .setPadding(8)
            .setMarginBottom(15));
    }
    
    private void createPdfUserEquipmentTable(com.itextpdf.layout.Document document, 
                                           List<Usuario> usuarios, 
                                           List<EquipoInformatico> equipos,
                                           com.itextpdf.kernel.font.PdfFont font,
                                           com.itextpdf.kernel.font.PdfFont boldFont) {
        
        // Crear tabla con 6 columnas
        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(new float[]{1, 2, 2, 1.5f, 1.5f, 1})
            .setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100))
            .setMarginBottom(20);
        
        // Encabezados
        String[] headers = {"Legajo", "Nombre Completo", "Email", "Tipo Equipo", "Marca/Modelo", "Estado"};
        for (String header : headers) {
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(header)
                    .setFont(boldFont)
                    .setFontSize(10))
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.BLUE)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                .setPadding(8)
                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
        }
        
        // Datos de usuarios y equipos
        for (Usuario usuario : usuarios.stream()
                .sorted(Comparator.comparing(Usuario::getLegajo, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList())) {
            
            List<EquipoInformatico> equiposUsuario = equipos.stream()
                .filter(equipo -> equipo.getUsuario() != null && equipo.getUsuario().getId().equals(usuario.getId()))
                .sorted(Comparator.comparing(e -> e.getTipo() != null ? e.getTipo().toString() : ""))
                .collect(Collectors.toList());
            
            if (equiposUsuario.isEmpty()) {
                // Usuario sin equipos
                addPdfTableRow(table, usuario, null, font);
            } else {
                // Usuario con equipos - una fila por equipo
                for (EquipoInformatico equipo : equiposUsuario) {
                    addPdfTableRow(table, usuario, equipo, font);
                }
            }
        }
        
        document.add(table);
    }
    
    private void addPdfTableRow(com.itextpdf.layout.element.Table table, Usuario usuario, 
                               EquipoInformatico equipo, com.itextpdf.kernel.font.PdfFont font) {
        
        // Legajo
        table.addCell(new com.itextpdf.layout.element.Cell()
            .add(new com.itextpdf.layout.element.Paragraph(usuario.getLegajo() != null ? usuario.getLegajo() : "")
                .setFont(font).setFontSize(9))
            .setPadding(5));
        
        // Nombre completo
        table.addCell(new com.itextpdf.layout.element.Cell()
            .add(new com.itextpdf.layout.element.Paragraph(usuario.getNombre() + " " + usuario.getApellido())
                .setFont(font).setFontSize(9))
            .setPadding(5));
        
        // Email
        table.addCell(new com.itextpdf.layout.element.Cell()
            .add(new com.itextpdf.layout.element.Paragraph(usuario.getCorreoElectronico() != null ? usuario.getCorreoElectronico() : "")
                .setFont(font).setFontSize(9))
            .setPadding(5));
        
        if (equipo != null) {
            // Tipo Equipo
            table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(equipo.getTipo() != null ? equipo.getTipo().toString() : "")
                    .setFont(font).setFontSize(9))
                .setPadding(5));
            
            // Marca/Modelo
            String marcaModelo = "";
            if (equipo.getMarca() != null && equipo.getModelo() != null) {
                marcaModelo = equipo.getMarca() + " / " + equipo.getModelo();
            } else if (equipo.getMarca() != null) {
                marcaModelo = equipo.getMarca();
            } else if (equipo.getModelo() != null) {
                marcaModelo = equipo.getModelo();
            }
            table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(marcaModelo)
                    .setFont(font).setFontSize(9))
                .setPadding(5));
            
            // Estado
            table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph(equipo.getEstado() != null ? equipo.getEstado().toString() : "")
                    .setFont(font).setFontSize(9))
                .setPadding(5));
        } else {
            // Usuario sin equipos
            table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("🚫 Sin equipos")
                    .setFont(font).setFontSize(9))
                .setPadding(5));
            table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("")
                    .setFont(font).setFontSize(9))
                .setPadding(5));
            table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new com.itextpdf.layout.element.Paragraph("")
                    .setFont(font).setFontSize(9))
                .setPadding(5));
        }
    }
}
