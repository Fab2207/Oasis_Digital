package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {}