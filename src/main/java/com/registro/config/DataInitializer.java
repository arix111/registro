package com.registro.config;

import com.registro.model.User;
import com.registro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando inicialización de datos...");
        
        // Verificar si ya existen usuarios
        if (userRepository.count() > 0) {
            log.info("Los usuarios ya existen en la base de datos. Total: {}", userRepository.count());
            return;
        }

        log.info("Creando usuarios por defecto...");
        
        // Crear usuario administrador
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@registro.com");
            admin.setFirstName("Administrador");
            admin.setLastName("Sistema");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            admin.setAccountNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setCredentialsNonExpired(true);
            
            userRepository.save(admin);
            log.info("Usuario administrador creado: admin");
        }

        // Crear usuario gerente
        if (!userRepository.existsByUsername("manager")) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEmail("manager@registro.com");
            manager.setFirstName("Gerente");
            manager.setLastName("General");
            manager.setRole(User.Role.MANAGER);
            manager.setEnabled(true);
            manager.setAccountNonExpired(true);
            manager.setAccountNonLocked(true);
            manager.setCredentialsNonExpired(true);
            
            userRepository.save(manager);
            log.info("Usuario gerente creado: manager");
        }

        // Crear usuario normal
        if (!userRepository.existsByUsername("user1")) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(passwordEncoder.encode("user123"));
            user1.setEmail("user1@registro.com");
            user1.setFirstName("Juan");
            user1.setLastName("Pérez");
            user1.setRole(User.Role.USER);
            user1.setEnabled(true);
            user1.setAccountNonExpired(true);
            user1.setAccountNonLocked(true);
            user1.setCredentialsNonExpired(true);
            
            userRepository.save(user1);
            log.info("Usuario normal creado: user1");
        }

        log.info("Inicialización de datos completada. Total usuarios: {}", userRepository.count());
        
        // Mostrar información de usuarios creados
        userRepository.findAll().forEach(user -> {
            log.info("Usuario: {} - Rol: {} - Habilitado: {}", 
                    user.getUsername(), user.getRole(), user.isEnabled());
        });
    }
}
