package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HabitacionService {
    private final HabitacionRepository habitacionRepository;
    private final AuditoriaService auditoriaService;
    private final ReservaRepository reservaRepository;

    @Autowired
    public HabitacionService(HabitacionRepository habitacionRepository, AuditoriaService auditoriaService, ReservaRepository reservaRepository) {
        this.habitacionRepository = habitacionRepository;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = reservaRepository;
    }

    // Constructor de compatibilidad para tests existentes
    public HabitacionService(HabitacionRepository habitacionRepository, AuditoriaService auditoriaService) {
        this.habitacionRepository = habitacionRepository;
        this.auditoriaService = auditoriaService;
        this.reservaRepository = null;
    }

    public long contarHabitaciones() {
        return habitacionRepository.count();
    }

    public long contarDisponibles() {
        return habitacionRepository.countByEstado("DISPONIBLE");
    }

    public long contarOcupadas() {
        return habitacionRepository.countByEstado("OCUPADA");
    }

    public long contarEnMantenimiento() {
        return habitacionRepository.countByEstado("MANTENIMIENTO");
    }

    public List<Habitacion> obtenerTodasLasHabitaciones() {
        return habitacionRepository.findAll();
    }

    public Optional<Habitacion> buscarHabitacionPorId(Long id) {
        return habitacionRepository.findById(id);
    }

    public Optional<Habitacion> buscarHabitacionPorNumero(String numero) {
        return habitacionRepository.findByNumero(numero);
    }

    public List<Habitacion> obtenerHabitacionesDisponibles() {
        return habitacionRepository.findByEstado("DISPONIBLE");
    }

    public long contarTotalHabitaciones() {
        return habitacionRepository.count();
    }

    public List<Habitacion> obtenerHabitacionesOcupadas() {
        return habitacionRepository.findByEstado("OCUPADA");
    }

    public List<Habitacion> obtenerHabitacionesEnMantenimiento() {
        return habitacionRepository.findByEstado("MANTENIMIENTO");
    }

    @Transactional
    public Habitacion crearHabitacion(Habitacion habitacion) {
        if (habitacionRepository.findByNumero(habitacion.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una habitación con el número '" + habitacion.getNumero() + "'.");
        }
        Habitacion nuevaHabitacion = habitacionRepository.save(habitacion);
        auditoriaService.registrarAccion(
                "CREACION_HABITACION",
                "Nueva habitación registrada: #" + nuevaHabitacion.getNumero() + " (" + nuevaHabitacion.getTipo() + ", $" + nuevaHabitacion.getPrecioPorNoche() + ")",
                "Habitacion",
                nuevaHabitacion.getId()
        );
        return nuevaHabitacion;
    }

    @Transactional
    public Habitacion actualizarHabitacion(Habitacion habitacionActualizada) {
        return habitacionRepository.findById(habitacionActualizada.getId())
                .map(habitacionExistente -> {
                    if (!habitacionExistente.getNumero().equals(habitacionActualizada.getNumero()) &&
                            habitacionRepository.findByNumero(habitacionActualizada.getNumero()).isPresent()) {
                        throw new IllegalArgumentException("El número de habitación '" + habitacionActualizada.getNumero() + "' ya está en uso por otra habitación.");
                    }

                    habitacionExistente.setNumero(habitacionActualizada.getNumero());
                    habitacionExistente.setTipo(habitacionActualizada.getTipo());
                    habitacionExistente.setPrecioPorNoche(habitacionActualizada.getPrecioPorNoche());
                    habitacionExistente.setEstado(habitacionActualizada.getEstado());

                    Habitacion habitacionGuardada = habitacionRepository.save(habitacionExistente);
                    auditoriaService.registrarAccion(
                            "ACTUALIZACION_HABITACION",
                            "Habitación #" + habitacionGuardada.getNumero() + " (ID: " + habitacionGuardada.getId() + ") actualizada. Nuevo estado: " + habitacionGuardada.getEstado(),
                            "Habitacion",
                            habitacionGuardada.getId()
                    );
                    return habitacionGuardada;
                })
                .orElseThrow(() -> new IllegalArgumentException("Habitación con ID " + habitacionActualizada.getId() + " no encontrada para actualizar."));
    }

    @Transactional
    public void actualizarEstadoHabitacion(Long id, String nuevoEstado) {
        habitacionRepository.findById(id).ifPresent(habitacion -> {
            String estadoAnterior = habitacion.getEstado();
            habitacion.setEstado(nuevoEstado);
            habitacionRepository.save(habitacion);
            auditoriaService.registrarAccion(
                    "CAMBIO_ESTADO_HABITACION",
                    "Estado de habitación #" + habitacion.getNumero() + " (ID: " + habitacion.getId() + ") cambiado de '" + estadoAnterior + "' a '" + nuevoEstado + "'.",
                    "Habitacion",
                    habitacion.getId()
            );
        });
    }

    public void inicializarHabitacionesSiNoExisten() {
        if (habitacionRepository.count() == 0) {
            crearHabitacion(new Habitacion("101", "Simple", 50.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("102", "Doble", 80.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("103", "Suite", 150.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("201", "Simple", 55.0, "DISPONIBLE"));
            crearHabitacion(new Habitacion("202", "Doble", 85.0, "OCUPADA"));
            crearHabitacion(new Habitacion("203", "Suite", 160.0, "MANTENIMIENTO"));
            System.out.println("Habitaciones inicializadas en la base de datos.");
        }
    }

    @Transactional
    public void eliminarHabitacion(Long id) {
        Optional<Habitacion> habitacionOptional = habitacionRepository.findById(id);
        if (habitacionOptional.isPresent()) {
            Habitacion habitacion = habitacionOptional.get();
            // eliminar reservas asociadas primero (si el repo está disponible)
            if (reservaRepository != null) {
                var reservas = reservaRepository.findByHabitacion(habitacion);
                if (reservas != null && !reservas.isEmpty()) {
                    reservaRepository.deleteAll(reservas);
                }
            }
            habitacionRepository.deleteById(id);
            auditoriaService.registrarAccion(
                    "ELIMINACION_HABITACION",
                    "Habitación #" + habitacion.getNumero() + " (ID: " + habitacion.getId() + ") eliminada.",
                    "Habitacion",
                    habitacion.getId()
            );
        } else {
            throw new IllegalArgumentException("Habitación con ID " + id + " no encontrada para eliminar.");
        }
    }
}