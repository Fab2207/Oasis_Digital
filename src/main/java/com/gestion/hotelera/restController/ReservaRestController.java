package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaRestController {

    private final ReservaService reservaService;

    public ReservaRestController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PostMapping
    public ResponseEntity<?> crearReserva(@Valid @RequestBody Reserva reserva) {
        if (reserva.getCliente() == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Debe asociar un cliente a la reserva"));
        }
        Reserva nuevaReserva = reservaService.crearOActualizarReserva(reserva);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @GetMapping
    public ResponseEntity<List<Reserva>> listarReservas() {
        return ResponseEntity.ok(reservaService.obtenerTodasLasReservas());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA','ROLE_CLIENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return reservaService.buscarReservaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarReserva(@PathVariable Long id, @Valid @RequestBody Reserva reserva) {
        reserva.setId(id);
        Reserva reservaActualizada = reservaService.crearOActualizarReserva(reserva);
        return ResponseEntity.ok(reservaActualizada);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizarReserva(@PathVariable Long id) {
        boolean ok = reservaService.finalizarReserva(id);
        if (ok) return ResponseEntity.ok(Collections.singletonMap("message", "Reserva finalizada correctamente"));
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No se pudo finalizar la reserva"));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_RECEPCIONISTA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarReserva(@PathVariable Long id) {
        boolean ok = reservaService.eliminarReservaFisica(id);
        if (ok) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Reserva no encontrada"));
    }
}