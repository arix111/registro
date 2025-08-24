// src/main/java/com/registro/repository/IUsuarioRepository.java
package com.registro.repository;

import com.registro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Busca un usuario por su legajo (clave de negocio).
     */
    Optional<Usuario> findByLegajo(String legajo);
}
