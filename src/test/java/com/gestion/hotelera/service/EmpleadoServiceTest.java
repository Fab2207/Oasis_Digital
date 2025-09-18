package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.repository.EmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmpleadoServiceTest {

    @Mock
    private EmpleadoRepository empleadoRepository;

    @InjectMocks
    private EmpleadoService empleadoService;

    private Empleado empleado;

    @BeforeEach
    public void setUp() {
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setDni("87654321");
        empleado.setNombre("Ana");
        empleado.setApellido("Perez");
        empleado.setCargo("Recepcionista");
    }

    @Test
    public void testListarTodosLosEmpleados() {
        when(empleadoRepository.findAll()).thenReturn(Collections.singletonList(empleado));
        assertEquals(1, empleadoService.listarTodosLosEmpleados().size());
        verify(empleadoRepository, times(1)).findAll();
    }

    @Test
    public void testGuardarEmpleado() {
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(empleado);
        Empleado savedEmpleado = empleadoService.guardarEmpleado(new Empleado());
        assertNotNull(savedEmpleado);
        assertEquals("Ana", savedEmpleado.getNombre());
        verify(empleadoRepository, times(1)).save(any(Empleado.class));
    }
}