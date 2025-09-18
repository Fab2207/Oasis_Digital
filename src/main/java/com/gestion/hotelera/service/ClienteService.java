package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodosLosClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarClientePorDni(String dni) {
        return Optional.ofNullable(clienteRepository.findByDni(dni));
    }

    public Cliente guardarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
    
    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}