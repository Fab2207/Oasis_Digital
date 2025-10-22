package com.gestion.hotelera.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import  com.gestion.hotelera.model.Cliente;
import  com.gestion.hotelera.repository.ClienteRepository;

public class ClienteServiceTest {

    private ClienteRepository clienteRepository;
    private AuditoriaService auditoriaService;
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteRepository = Mockito.mock(ClienteRepository.class);
        auditoriaService = Mockito.mock(AuditoriaService.class);
        clienteService = new ClienteService(clienteRepository, auditoriaService);
    }

    @Test
    void testRegistrarCliente() {
        Cliente cliente = new Cliente();
        cliente.setDni("87654321");
        cliente.setNombres("Maria");
        cliente.setApellidos("Lopez");
        cliente.setNacionalidad("Peruana");
        cliente.setEmail("maria@test.com");
        cliente.setTelefono("987654321");

        when(clienteRepository.findByDni(any())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente guardado = clienteService.crearCliente(cliente);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getNombres()).isEqualTo("Maria");
    }

    @Test
    void testBuscarPorDni() {
        Cliente cliente = new Cliente();
        cliente.setDni("11223344");
        cliente.setNombres("Carlos");

        when(clienteRepository.findByDni("11223344")).thenReturn(Optional.of(cliente));

        Optional<Cliente> encontrado = clienteService.buscarClientePorDni("11223344");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombres()).isEqualTo("Carlos");
    }
}