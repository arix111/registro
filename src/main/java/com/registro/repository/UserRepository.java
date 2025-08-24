package com.registro.repository;

import com.registro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Buscar usuario por username (usado para autenticación)
     */
    Optional<User> findByUsername(String username);

    /**
     * Buscar usuario por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Verificar si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);

    /**
     * Verificar si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Buscar usuarios por rol
     */
    List<User> findByRole(User.Role role);

    /**
     * Buscar usuarios habilitados
     */
    List<User> findByEnabledTrue();

    /**
     * Buscar usuarios por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findBySearchTerm(@Param("searchTerm") String searchTerm);

    /**
     * Actualizar último login del usuario
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Contar usuarios por rol
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * Buscar usuarios creados después de una fecha específica
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Buscar usuarios que no han iniciado sesión en un período específico
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :date")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
}
