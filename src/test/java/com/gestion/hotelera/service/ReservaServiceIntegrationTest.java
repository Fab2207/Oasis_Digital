package com.gestion.hotelera.service;

import static org.assertj.core.api.Assertions.*;

import com.gestion.hotelera.BaseIntegrationTest;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.model.Reserva;
import com.gestion.hotelera.repository.ClienteRepository;
import com.gestion.hotelera.repository.HabitacionRepository;
import com.gestion.hotelera.repository.ReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Tests de integración para ReservaService
 * Prueba la interacción real con la base de datos
 */
@DisplayName("ReservaService - Integration Tests")
class ReservaServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ReservaService reservaService;
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private HabitacionRepository habitacionRepository;
    

    private Cliente cliente;
    private Habitacion habitacion;

    @BeforeEach
    void setUp() {
        // Limpiar datos de prueba
        reservaRepository.deleteAll();
        clienteRepository.deleteAll();
        habitacionRepository.deleteAll();
        
        // Crear datos de prueba
        cliente = new Cliente();
        cliente.setDni("12345678");
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("987654321");
        cliente = clienteRepository.save(cliente);
        
        habitacion = new Habitacion();
        habitacion.setNumero("101");
        habitacion.setEstado("DISPONIBLE");
        habitacion.setPrecioPorNoche(150.0);
        habitacion.setTipo("Suite");
        habitacion = habitacionRepository.save(habitacion);
    }

    @Test
    @DisplayName("Debería crear reserva con persistencia real")
    void deberiaCrearReservaConPersistenciaReal() {
        // Given
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setHabitacion(habitacion);
        reserva.setFechaInicio(LocalDate.now().plusDays(1));
        reserva.setFechaFin(LocalDate.now().plusDays(3));
        reserva.setEstadoReserva("PENDIENTE");
        reserva.setTotalPagar(300.0);
        
        // When
        Reserva resultado = reservaService.crearOActualizarReserva(reserva);
        
        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(resultado.getHabitacion().getId()).isEqualTo(habitacion.getId());
        
        // Verificar persistencia
        Optional<Reserva> persistida = reservaRepository.findById(resultado.getId());
        assertThat(persistida).isPresent();
        assertThat(persistida.get().getEstadoReserva()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Debería calcular días de estadía correctamente")
    void deberiaCalcularDiasEstadiaCorrectamente() {
        // Given
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fin = LocalDate.of(2024, 1, 5);
        
        // When
        Integer dias = reservaService.calcularDiasEstadia(inicio, fin);
        
        // Then
        assertThat(dias).isEqualTo(4);
    }

    @Test
    @DisplayName("Debería calcular total a pagar correctamente")
    void deberiaCalcularTotalPagarCorrectamente() {
        // Given
        Double precioPorNoche = 200.0;
        Integer dias = 5;
        
        // When
        Double total = reservaService.calcularTotalPagar(precioPorNoche, dias);
        
        // Then
        assertThat(total).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("Debería cancelar reserva y persistir cambio")
    void deberiaCancelarReservaYPersistirCambio() {
        // Given
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setHabitacion(habitacion);
        reserva.setFechaInicio(LocalDate.now().plusDays(1));
        reserva.setFechaFin(LocalDate.now().plusDays(3));
        reserva.setEstadoReserva("PENDIENTE");
        reserva.setTotalPagar(300.0);
        reserva = reservaRepository.save(reserva);
        
        // When
        boolean resultado = reservaService.cancelarReserva(reserva.getId());
        
        // Then
        assertThat(resultado).isTrue();
        
        // Verificar persistencia
        Optional<Reserva> actualizada = reservaRepository.findById(reserva.getId());
        assertThat(actualizada).isPresent();
        assertThat(actualizada.get().getEstadoReserva()).isEqualTo("CANCELADA");
    }

    @Test
    @DisplayName("Debería calcular ingresos totales de reservas finalizadas")
    void deberiaCalcularIngresosTotalesDeReservasFinalizadas() {
        // Given
        Reserva reserva1 = new Reserva();
        reserva1.setCliente(cliente);
        reserva1.setHabitacion(habitacion);
        reserva1.setFechaInicio(LocalDate.now().minusDays(5));
        reserva1.setFechaFin(LocalDate.now().minusDays(2));
        reserva1.setEstadoReserva("FINALIZADA");
        reserva1.setTotalPagar(450.0);
        reservaRepository.save(reserva1);
        
        Reserva reserva2 = new Reserva();
        reserva2.setCliente(cliente);
        reserva2.setHabitacion(habitacion);
        reserva2.setFechaInicio(LocalDate.now().plusDays(1));
        reserva2.setFechaFin(LocalDate.now().plusDays(3));
        reserva2.setEstadoReserva("PENDIENTE");
        reserva2.setTotalPagar(300.0);
        reservaRepository.save(reserva2);
        
        Reserva reserva3 = new Reserva();
        reserva3.setCliente(cliente);
        reserva3.setHabitacion(habitacion);
        reserva3.setFechaInicio(LocalDate.now().minusDays(10));
        reserva3.setFechaFin(LocalDate.now().minusDays(7));
        reserva3.setEstadoReserva("FINALIZADA");
        reserva3.setTotalPagar(600.0);
        reservaRepository.save(reserva3);
        
        // When
        Double ingresos = reservaService.calcularIngresosTotales();
        
        // Then
        assertThat(ingresos).isEqualTo(1050.0);
    }

    @Test
    @DisplayName("Debería obtener reservas por cliente")
    void deberiaObtenerReservasPorCliente() {
        // Given
        Reserva reserva1 = new Reserva();
        reserva1.setCliente(cliente);
        reserva1.setHabitacion(habitacion);
        reserva1.setFechaInicio(LocalDate.now().plusDays(1));
        reserva1.setFechaFin(LocalDate.now().plusDays(3));
        reserva1.setEstadoReserva("PENDIENTE");
        reserva1.setTotalPagar(300.0);
        reservaRepository.save(reserva1);
        
        Reserva reserva2 = new Reserva();
        reserva2.setCliente(cliente);
        reserva2.setHabitacion(habitacion);
        reserva2.setFechaInicio(LocalDate.now().plusDays(5));
        reserva2.setFechaFin(LocalDate.now().plusDays(7));
        reserva2.setEstadoReserva("PENDIENTE");
        reserva2.setTotalPagar(300.0);
        reservaRepository.save(reserva2);
        
        // When
        List<Reserva> reservas = reservaService.obtenerReservasPorCliente(cliente);
        
        // Then
        assertThat(reservas).hasSize(2);
        assertThat(reservas).allMatch(r -> r.getCliente().getId().equals(cliente.getId()));
    }
}