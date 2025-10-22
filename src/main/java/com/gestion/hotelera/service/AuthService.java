package com.gestion.hotelera.service;

import com.gestion.hotelera.dto.AuthResponse;
import com.gestion.hotelera.dto.LoginRequest;
import com.gestion.hotelera.dto.RegisterRequest;
import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ClienteService clienteService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Usuario user = usuarioRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String token = jwtService.getToken(user);

        return AuthResponse.builder()
            .token(token)
            .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }

        Usuario user = new Usuario();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRol("ROLE_CLIENTE"); 

        Usuario userGuardado = usuarioRepository.save(user);

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setDni(request.getDni());
        nuevoCliente.setNombres(request.getNombres());
        nuevoCliente.setApellidos(request.getApellidos());
        nuevoCliente.setNacionalidad(request.getNacionalidad());
        nuevoCliente.setEmail(request.getEmail());
        nuevoCliente.setTelefono(request.getTelefono());
        nuevoCliente.setUsuario(userGuardado);

        clienteService.guardar(nuevoCliente);

        String token = jwtService.getToken(userGuardado);

        System.out.println("✅ Cliente registrado: " + userGuardado.getUsername());

        return AuthResponse.builder()
            .token(token)
            .build();
    }
}