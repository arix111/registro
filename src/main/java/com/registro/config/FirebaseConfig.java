// src/main/java/com/registro/config/FirebaseConfig.java
package com.registro.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        // Carga las credenciales desde el JSON en resources
        ClassPathResource serviceAccountResource = new ClassPathResource("registro.json");
        try (InputStream serviceAccount = serviceAccountResource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                // Reemplaza con el nombre real de tu bucket (lo ves en la consola de Firebase Storage)
                .setStorageBucket("registro-c9912.firebasestorage.app")
                .build();

            // Solo inicializa si no hay otra app ya inicializada
            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            } else {
                return FirebaseApp.getInstance();
            }
        }
    }
}
