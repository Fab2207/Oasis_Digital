package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final AuditoriaService auditoriaService;

    public ReservaService(ReservaRepository reservaRepository, AuditoriaService auditoriaService) {
        this.reservaRepository = reservaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Reserva crearOActualizarReserva(Reserva reserva) {
        Reserva guardada = reservaRepository.save(reserva);
        auditoriaService.registrarAccion("CREACION_O_ACTUALIZACION_RESERVA",
                "Reserva creada o actualizada (ID: " + guardada.getId() + ") para cliente " + guardada.getCliente().getNombres(),
                "Reserva", guardada.getId());
        return guardada;
    }

    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> buscarReservaPorId(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> obtenerReservasPorCliente(Cliente cliente) {
        return reservaRepository.findByCliente(cliente);
    }

    public List<Reserva> obtenerReservasPorClienteId(Long clienteId) {
        List<Reserva> todas = reservaRepository.findAll();
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : todas) {
            if (r.getCliente() != null && Objects.equals(r.getCliente().getId(), clienteId)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public long contarReservas() {
        return reservaRepository.count();
    }

    @Transactional
    public boolean cancelarReserva(Long id) {
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            reserva.setEstadoReserva("CANCELADA");
            reservaRepository.save(reserva);
            auditoriaService.registrarAccion("CANCELACION_RESERVA",
                    "Reserva (ID: " + reserva.getId() + ") cancelada.", "Reserva", reserva.getId());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean eliminarReservaFisica(Long id) {
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            reservaRepository.deleteById(id);
            auditoriaService.registrarAccion("ELIMINACION_RESERVA",
                    "Reserva (ID: " + id + ") eliminada físicamente.", "Reserva", id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean finalizarReserva(Long id) {
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isPresent()) {
            Reserva reserva = opt.get();
            reserva.setEstadoReserva("FINALIZADA");
            reservaRepository.save(reserva);
            auditoriaService.registrarAccion("FINALIZACION_RESERVA",
                    "Reserva (ID: " + reserva.getId() + ") finalizada.", "Reserva", reserva.getId());
            return true;
        }
        return false;
    }

    public Integer calcularDiasEstadia(LocalDate inicio, LocalDate fin) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(inicio, fin);
    }

    public Double calcularTotalPagar(Double precioPorNoche, Integer dias) {
        return precioPorNoche * dias;
    }

    public double calcularIngresosTotales() {
        List<Reserva> reservas = reservaRepository.findAll();
        return reservas.stream()
                .filter(r -> "FINALIZADA".equalsIgnoreCase(r.getEstadoReserva()))
                .mapToDouble(Reserva::getTotalPagar)
                .sum();
    }

    public List<Map<String, Object>> getIngresosPorPeriodo(LocalDate inicio, LocalDate fin) {
        List<Reserva> reservas = reservaRepository.findAll();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Reserva r : reservas) {
            if (!r.getFechaInicio().isBefore(inicio) && !r.getFechaFin().isAfter(fin)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("cliente", r.getCliente().getNombres());
                map.put("total", r.getTotalPagar());
                lista.add(map);
            }
        }
        return lista;
    }

    public List<Map<String, Object>> getMovimientoPorPeriodo(LocalDate inicio, LocalDate fin) {
        List<Reserva> reservas = reservaRepository.findAll();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Reserva r : reservas) {
            if (!r.getFechaInicio().isBefore(inicio) && !r.getFechaFin().isAfter(fin)) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("estado", r.getEstadoReserva());
                map.put("fechaInicio", r.getFechaInicio());
                map.put("fechaFin", r.getFechaFin());
                lista.add(map);
            }
        }
        return lista;
    }

    public long contarReservasPorEstado(String estado) {
        return reservaRepository.countByEstadoReservaIgnoreCase(estado);
    }

    public long contarCheckInsHoy() {
        return reservaRepository.countByFechaInicio(LocalDate.now());
    }

    public long contarCheckOutsHoy() {
        return reservaRepository.countByFechaFin(LocalDate.now());
    }
}