package com.gestion.hotelera.restController;

import org.springframework.web.bind.annotation.*;
import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.service.EmpleadoService;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoRestController {

    private final EmpleadoService empleadoService;

    public EmpleadoRestController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public Empleado registrar(@RequestBody Empleado empleado) {
        return empleadoService.registrarEmpleado(empleado);
    }

    @GetMapping
    public List<Empleado> listar() {
        return empleadoService.listarEmpleados();
    }
}