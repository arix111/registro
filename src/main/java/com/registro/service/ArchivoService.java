// src/main/java/com/registro/service/ArchivoService.java
package com.registro.service;

import com.google.firebase.cloud.StorageClient;
import com.registro.model.Archivo;
import com.registro.model.Usuario;
import com.registro.repository.IArchivoRepository;
import com.registro.repository.IUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArchivoService {

    private final IUsuarioRepository usuarioRepository;
    private final IArchivoRepository archivoRepository;

    public ArchivoService(IUsuarioRepository usuarioRepository,
                          IArchivoRepository archivoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.archivoRepository = archivoRepository;
    }

    @Transactional
    public List<Archivo> subirArchivos(String legajo, MultipartFile[] archivos) throws IOException {
        Usuario usuario = usuarioRepository.findByLegajo(legajo)
            .orElseThrow(() -> new RuntimeException("Usuario con legajo " + legajo + " no encontrado"));

        String bucketName = StorageClient.getInstance().bucket().getName();
        List<Archivo> guardados = new ArrayList<>();

        for (MultipartFile file : archivos) {
            String nombreOriginal = file.getOriginalFilename();
            String rutaEnBucket   = "usuarios/" + legajo + "/"
                                 + System.currentTimeMillis()
                                 + "_" + nombreOriginal;

            // Subir al bucket
            StorageClient.getInstance()
                         .bucket()
                         .create(rutaEnBucket,
                                 file.getInputStream(),
                                 file.getContentType());

            // Construir URL pública
            String urlPublica = String.format(
                "https://storage.googleapis.com/%s/%s",
                bucketName, rutaEnBucket
            );

            Archivo ent = new Archivo(nombreOriginal, urlPublica, rutaEnBucket, usuario);
            guardados.add(archivoRepository.save(ent));
        }

        return guardados;
    }

    @Transactional
    public void eliminarArchivo(Long archivoId) {
        Archivo a = archivoRepository.findById(archivoId)
            .orElseThrow(() -> new RuntimeException("Archivo no encontrado"));

        // Determinar nombre de blob: preferir 'path', si es null extraer de URL
        String blobName;
        if (a.getPath() != null && !a.getPath().isEmpty()) {
            blobName = a.getPath();
        } else {
            String bucket = StorageClient.getInstance().bucket().getName();
            String prefix = "https://storage.googleapis.com/" + bucket + "/";
            blobName = a.getUrl().substring(prefix.length());
        }

        // Borrar del bucket si existe
        com.google.cloud.storage.Blob blob =
            StorageClient.getInstance().bucket().get(blobName);
        if (blob != null) {
            blob.delete();
        }

        // Borrar registro en BD
        archivoRepository.delete(a);
    }

    public Archivo obtenerArchivoPorId(Long archivoId) {
        return archivoRepository.findById(archivoId)
            .orElseThrow(() -> new RuntimeException("Archivo no encontrado con ID: " + archivoId));
    }

    @Transactional(readOnly = true)
    public byte[] descargarArchivoBytes(String path) throws IOException {
        com.google.cloud.storage.Blob blob = StorageClient.getInstance().bucket().get(path);
        if (blob == null) {
            throw new IOException("No se encontró el archivo en Firebase Storage con la ruta: " + path);
        }
        return blob.getContent();
    }
}
