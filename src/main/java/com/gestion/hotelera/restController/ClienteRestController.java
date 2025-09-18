package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<Cliente> listarTodosLosClientes() {
        return clienteService.listarTodosLosClientes();
    }

    @GetMapping("/{dni}")
    public ResponseEntity<Cliente> buscarClientePorDni(@PathVariable String dni) {
        return clienteService.buscarClientePorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cliente guardarCliente(@RequestBody Cliente cliente) {
        return clienteService.guardarCliente(cliente);
    }

    // Endpoint para eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}