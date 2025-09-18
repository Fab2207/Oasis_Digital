package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionRestController {

    @Autowired
    private HabitacionService habitacionService;

    @GetMapping
    public List<Habitacion> listarTodasLasHabitaciones() {
        return habitacionService.listarTodasLasHabitaciones();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habitacion> buscarHabitacionPorId(@PathVariable Long id) {
        return habitacionService.buscarHabitacionPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Habitacion guardarHabitacion(@RequestBody Habitacion habitacion) {
        return habitacionService.guardarHabitacion(habitacion);
    }

    // Endpoint para eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabitacion(@PathVariable Long id) {
        habitacionService.eliminarHabitacion(id);
        return ResponseEntity.noContent().build();
    }
}