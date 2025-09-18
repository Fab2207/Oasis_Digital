package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.repository.HabitacionRepository;


import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class HabitacionServiceTest {

    private HabitacionRepository habitacionRepository;
    private HabitacionService habitacionService;

    @BeforeEach
    void setUp() {
        habitacionRepository = Mockito.mock(HabitacionRepository.class);
        habitacionService = new HabitacionService(habitacionRepository);
    }

    @Test
    void testRegistrarHabitacion() {
        Habitacion habitacion = new Habitacion();
        habitacion.setNumero("201");
        habitacion.setEstado("Disponible");
        habitacion.setPrecio(BigDecimal.valueOf(150));

        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(habitacion);

        Habitacion guardada = habitacionService.registrarHabitacion(habitacion);

        assertThat(guardada).isNotNull();
        assertThat(guardada.getNumero()).isEqualTo("201");
    }

    @Test
    void testListarHabitaciones() {
        Habitacion h1 = new Habitacion();
        h1.setNumero("101");
        h1.setEstado("Disponible");

        Habitacion h2 = new Habitacion();
        h2.setNumero("102");
        h2.setEstado("Ocupado");

        when(habitacionRepository.findAll()).thenReturn(List.of(h1, h2));

        List<Habitacion> habitaciones = habitacionService.listarHabitaciones();

        assertThat(habitaciones).hasSize(2);
        assertThat(habitaciones.get(0).getNumero()).isEqualTo("101");
    }

    @Test
    void testListarPorEstado() {
        Habitacion h1 = new Habitacion();
        h1.setNumero("150");
        h1.setEstado("Mantenimiento");

        when(habitacionRepository.findByEstado("Mantenimiento")).thenReturn(List.of(h1));

        List<Habitacion> mantenimiento = habitacionService.listarPorEstado("Mantenimiento");

        assertThat(mantenimiento).isNotEmpty();
        assertThat(mantenimiento.get(0).getNumero()).isEqualTo("301");
    }
}