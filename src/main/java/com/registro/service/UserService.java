package com.registro.service;

import com.registro.model.User;
import com.registro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Implementación de UserDetailsService para Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });
        
        log.debug("User found: {} with role: {}", username, user.getRole());
        return user;
    }

    /**
     * Crear un nuevo usuario
     */
    public User createUser(String username, String password, String email, 
                          String firstName, String lastName, User.Role role) {
        log.info("Creating new user with username: {}", username);
        
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya existe: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role != null ? role : User.Role.USER);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return savedUser;
    }

    /**
     * Buscar usuario por username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Buscar usuario por email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Obtener todos los usuarios
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Obtener usuarios por rol
     */
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    /**
     * Buscar usuarios por término de búsqueda
     */
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.findBySearchTerm(searchTerm.trim());
    }

    /**
     * Actualizar último login del usuario
     */
    public void updateLastLogin(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.updateLastLogin();
            userRepository.save(user);
            log.debug("Updated last login for user: {}", username);
        }
    }

    /**
     * Cambiar contraseña del usuario
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
    }

    /**
     * Cambiar contraseña de usuario por administrador (sin validar contraseña anterior)
     */
    public void changePasswordByAdmin(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed by admin for user: {}", user.getUsername());
    }

    /**
     * Habilitar/deshabilitar usuario
     */
    public void toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("User {} status changed to: {}", user.getUsername(), 
                user.isEnabled() ? "enabled" : "disabled");
    }

    /**
     * Actualizar rol del usuario
     */
    public void updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        User.Role oldRole = user.getRole();
        user.setRole(newRole);
        userRepository.save(user);
        log.info("User {} role changed from {} to {}", 
                user.getUsername(), oldRole, newRole);
    }

    /**
     * Eliminar usuario
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        userRepository.delete(user);
        log.info("User deleted: {}", user.getUsername());
    }

    /**
     * Verificar si un usuario existe
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Verificar si un email existe
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Obtener estadísticas de usuarios
     */
    public UserStats getUserStats() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long enabledUsers = allUsers.stream().filter(User::isEnabled).count();
        long adminUsers = allUsers.stream().filter(u -> u.getRole() == User.Role.ADMIN).count();
        long managerUsers = allUsers.stream().filter(u -> u.getRole() == User.Role.MANAGER).count();
        long regularUsers = allUsers.stream().filter(u -> u.getRole() == User.Role.USER).count();

        return new UserStats(totalUsers, enabledUsers, adminUsers, managerUsers, regularUsers);
    }

    /**
     * Actualizar perfil personal del usuario
     */
    @Transactional
    public void updateProfile(String username, String fullName, String email) {
        User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar que el email no esté en uso por otro usuario
        if (!user.getEmail().equals(email)) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                throw new RuntimeException("El email ya está en uso por otro usuario");
            }
        }
        
        // Actualizar nombre completo y email
        String[] names = fullName.trim().split(" ", 2);
        user.setFirstName(names[0]);
        if (names.length > 1) {
            user.setLastName(names[1]);
        }
        user.setEmail(email);
        userRepository.save(user);
        
        log.info("Perfil actualizado para usuario: {}", username);
    }

    /**
     * Clase para estadísticas de usuarios
     */
    public static class UserStats {
        private final long totalUsers;
        private final long activeUsers;
        private final long adminUsers;
        private final long managerUsers;
        private final long regularUsers;

        public UserStats(long totalUsers, long activeUsers, long adminUsers, long managerUsers, long regularUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminUsers = adminUsers;
            this.managerUsers = managerUsers;
            this.regularUsers = regularUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getAdminUsers() { return adminUsers; }
        public long getManagerUsers() { return managerUsers; }
        public long getRegularUsers() { return regularUsers; }
        public long getDisabledUsers() { return totalUsers - activeUsers; }
    }
}
