// src/main/java/com/registro/controller/ArchivoController.java
package com.registro.controller;

import com.registro.model.Archivo;
import com.registro.service.ArchivoService;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ArchivoController {

    private final ArchivoService archivoService;

    public ArchivoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @GetMapping("/upload")
    public String mostrarFormulario() {
        return "formulario";
    }

    @PostMapping("/upload")
    public String manejarUpload(
            @RequestParam("legajo") String legajo,
            @RequestParam("archivos") MultipartFile[] archivos,
            org.springframework.ui.Model model
    ) throws IOException {
        List<Archivo> guardados = archivoService.subirArchivos(legajo, archivos);
        model.addAttribute("archivos", guardados);
        model.addAttribute("mensaje", "Archivos subidos correctamente");
        return "resultado";
    }

    /** Borra vía GET para evitar multipart en la petición */
    @GetMapping("/archivos/eliminar")
    public String eliminar(
            @RequestParam("archivoId") Long id,
            @RequestParam("legajo") String legajo
    ) {
        archivoService.eliminarArchivo(id);
        return "redirect:/usuarios/editar?legajo=" + legajo;
    }
}
