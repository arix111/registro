// src/main/java/com/registro/repository/IArchivoRepository.java
package com.registro.repository;

import com.registro.model.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IArchivoRepository extends JpaRepository<Archivo, Long> {
    // si necesitas buscar por legajo: List<Archivo> findByUsuario_Legajo(String legajo);
}
