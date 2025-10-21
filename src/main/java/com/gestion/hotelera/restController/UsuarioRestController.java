package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.ClienteRepository;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.getUsernameFromToken(token);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getCliente() == null) {
            return ResponseEntity.status(403).body("El usuario no tiene perfil de cliente");
        }

        return ResponseEntity.ok(usuario.getCliente());
    }

    @PostMapping("/registrarCliente")
    public ResponseEntity<?> registrarCliente(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody Cliente nuevoCliente) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.getUsernameFromToken(token);
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        if (!(usuario.getRol().equals("ROLE_ADMIN") || usuario.getRol().equals("ROLE_RECEPCIONISTA"))) {
            return ResponseEntity.status(403).body("No tienes permiso para registrar clientes.");
        }

        if (clienteRepository.findByDni(nuevoCliente.getDni()).isPresent()) {
            return ResponseEntity.badRequest().body("El cliente con ese DNI ya existe.");
        }

        Cliente guardado = clienteRepository.save(nuevoCliente);
        return ResponseEntity.ok(guardado);
    }

    @GetMapping("/clientes")
    public ResponseEntity<?> listarClientes(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.getUsernameFromToken(token);
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        if (!(usuario.getRol().equals("ROLE_ADMIN") || usuario.getRol().equals("ROLE_RECEPCIONISTA"))) {
            return ResponseEntity.status(403).body("No tienes permiso para ver la lista de clientes.");
        }

        return ResponseEntity.ok(clienteRepository.findAll());
    }
}