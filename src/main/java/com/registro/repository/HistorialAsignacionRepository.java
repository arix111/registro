package com.registro.repository;

import com.registro.model.EquipoInformatico;
import com.registro.model.HistorialAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HistorialAsignacionRepository extends JpaRepository<HistorialAsignacion, Long> {

    List<HistorialAsignacion> findByEquipoInformaticoOrderByFechaAsignacionDesc(EquipoInformatico equipoInformatico);

    @Query("SELECT h FROM HistorialAsignacion h WHERE h.equipoInformatico = :equipo AND h.fechaDevolucion IS NULL")
    Optional<HistorialAsignacion> findOpenByEquipoInformatico(EquipoInformatico equipo);

}
