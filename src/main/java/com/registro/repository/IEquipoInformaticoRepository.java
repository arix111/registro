// src/main/java/com/registro/repository/IEquipoInformaticoRepository.java
package com.registro.repository;

import com.registro.model.EquipoInformatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEquipoInformaticoRepository extends JpaRepository<EquipoInformatico, Long> {
    
    List<EquipoInformatico> findByUsuarioLegajo(String legajo);
    
    void deleteByUsuarioLegajo(String legajo);

    @Query(value = "SELECT e FROM EquipoInformatico e LEFT JOIN e.usuario u WHERE " +
            "(LOWER(e.marca) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.modelo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.numeroSerie) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.numeroInventario) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.legajo) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
            "(:filtroTipo IS NULL OR e.tipo = :filtroTipo)",
            countQuery = "SELECT count(e) FROM EquipoInformatico e LEFT JOIN e.usuario u WHERE " +
            "(LOWER(e.marca) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.modelo) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.numeroSerie) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(e.numeroInventario) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.legajo) LIKE LOWER(CONCAT('%', :term, '%'))) AND " +
            "(:filtroTipo IS NULL OR e.tipo = :filtroTipo)")
    Page<EquipoInformatico> findBySearchTerm(@Param("term") String term, @Param("filtroTipo") EquipoInformatico.TipoEquipo filtroTipo, Pageable pageable);
}