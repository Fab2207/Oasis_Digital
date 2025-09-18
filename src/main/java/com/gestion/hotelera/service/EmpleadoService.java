package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public List<Empleado> listarTodosLosEmpleados() {
        return empleadoRepository.findAll();
    }

    public Optional<Empleado> buscarEmpleadoPorId(Long id) {
        return empleadoRepository.findById(id);
    }
    
    public Empleado guardarEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }
    
    public void eliminarEmpleado(Long id) {
        empleadoRepository.deleteById(id);
    }
}