package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.repository.ClienteRepository;
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
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    public void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setDni("12345678");
        cliente.setNombre("Juan");
    }

    @Test
    public void testListarTodosLosClientes() {
        when(clienteRepository.findAll()).thenReturn(Collections.singletonList(cliente));
        assertEquals(1, clienteService.listarTodosLosClientes().size());
        verify(clienteRepository, times(1)).findAll();
    }
    
    @Test
    public void testGuardarCliente() {
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        Cliente savedCliente = clienteService.guardarCliente(new Cliente());
        assertNotNull(savedCliente);
        assertEquals("Juan", savedCliente.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}