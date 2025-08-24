// src/main/java/com/registro/config/FirebaseConfig.java
package com.registro.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.json.path:#{null}}")
    private String firebaseJsonPath;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        InputStream serviceAccount;

        if (firebaseJsonPath != null && !firebaseJsonPath.isEmpty()) {
            // Producción: Lee el archivo desde la ruta especificada por la variable de entorno
            serviceAccount = new FileInputStream(firebaseJsonPath);
        } else {
            // Desarrollo: Usa el archivo local del classpath
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
