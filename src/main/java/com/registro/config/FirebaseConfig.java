// src/main/java/com/registro/config/FirebaseConfig.java
package com.registro.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials}")
    private String firebaseCredentials;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        InputStream serviceAccount;

        if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
            // Producción: Usa las credenciales de la variable de entorno
            serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));
        } else {
            // Desarrollo: Usa el archivo local
            ClassPathResource serviceAccountResource = new ClassPathResource("registro.json");
            serviceAccount = serviceAccountResource.getInputStream();
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setStorageBucket(storageBucket)
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }
}
