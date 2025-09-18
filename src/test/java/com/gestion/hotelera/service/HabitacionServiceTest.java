package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Habitacion;
import com.gestion.hotelera.repository.HabitacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HabitacionServiceTest {

    @Mock
    private HabitacionRepository habitacionRepository;

    @InjectMocks
    private HabitacionService habitacionService;

    private Habitacion habitacion;

    @BeforeEach
    public void setUp() {
        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setNumero(101);
        habitacion.setTipo("Suite");
        habitacion.setEstado("Disponible");
    }

    @Test
    public void testListarTodasLasHabitaciones() {
        when(habitacionRepository.findAll()).thenReturn(Collections.singletonList(habitacion));
        assertEquals(1, habitacionService.listarTodasLasHabitaciones().size());
        verify(habitacionRepository, times(1)).findAll();
    }

    @Test
    public void testBuscarHabitacionPorId() {
        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
        Optional<Habitacion> foundHabitacion = habitacionService.buscarHabitacionPorId(1L);
        assertTrue(foundHabitacion.isPresent());
        assertEquals("Suite", foundHabitacion.get().getTipo());
        verify(habitacionRepository, times(1)).findById(1L);
    }

    @Test
    public void testGuardarHabitacion() {
        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(habitacion);
        Habitacion savedHabitacion = habitacionService.guardarHabitacion(new Habitacion());
        assertNotNull(savedHabitacion);
        assertEquals(101, savedHabitacion.getNumero());
        verify(habitacionRepository, times(1)).save(any(Habitacion.class));
    }
}