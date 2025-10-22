package com.gestion.hotelera.service;

import com.gestion.hotelera.model.Empleado;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.EmpleadoRepository;
import com.gestion.hotelera.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public EmpleadoService(EmpleadoRepository empleadoRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, AuditoriaService auditoriaService) {
        this.empleadoRepository = empleadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Empleado registrarRecepcionista(Empleado empleado) {
        if (empleadoRepository.findByDni(empleado.getDni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un empleado con el DNI '" + empleado.getDni() + "'.");
        }
        if (empleadoRepository.findByEmail(empleado.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un empleado con el email '" + empleado.getEmail() + "'.");
        }
        if (usuarioRepository.findByUsername(empleado.getUsuario().getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre de usuario '" + empleado.getUsuario().getUsername() + "'.");
        }

        String rawPassword = empleado.getUsuario().getPassword();
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        empleado.getUsuario().setPassword(passwordEncoder.encode(rawPassword));
        empleado.getUsuario().setRol("ROLE_RECEPCIONISTA");
        Empleado nuevoEmpleado = empleadoRepository.save(empleado);

        auditoriaService.registrarAccion("CREACION_EMPLEADO",
                "Nuevo recepcionista: " + nuevoEmpleado.getNombres() + " " + nuevoEmpleado.getApellidos() + " (DNI: " + nuevoEmpleado.getDni() + ")",
                "Empleado",
                nuevoEmpleado.getId());
        return nuevoEmpleado;
    }

    public List<Empleado> obtenerTodosLosEmpleados() {
        return empleadoRepository.findAll();
    }

    public Optional<Empleado> buscarEmpleadoPorId(Long id) {
        return empleadoRepository.findById(id);
    }

    @Transactional
    public Empleado actualizarEmpleado(Empleado empleadoActualizado) {
        return empleadoRepository.findById(empleadoActualizado.getId()).map(empleadoExistente -> {
            if (!empleadoExistente.getDni().equals(empleadoActualizado.getDni()) && empleadoRepository.findByDni(empleadoActualizado.getDni()).isPresent()) {
                throw new IllegalArgumentException("El DNI '" + empleadoActualizado.getDni() + "' ya está en uso.");
            }
            if (!empleadoExistente.getEmail().equals(empleadoActualizado.getEmail()) && empleadoRepository.findByEmail(empleadoActualizado.getEmail()).isPresent()) {
                throw new IllegalArgumentException("El Email '" + empleadoActualizado.getEmail() + "' ya está en uso.");
            }

            empleadoExistente.setNombres(empleadoActualizado.getNombres());
            empleadoExistente.setApellidos(empleadoActualizado.getApellidos());
            empleadoExistente.setDni(empleadoActualizado.getDni());
            empleadoExistente.setEmail(empleadoActualizado.getEmail());
            empleadoExistente.setTelefono(empleadoActualizado.getTelefono());

            Usuario usuarioExistente = empleadoExistente.getUsuario();
            Usuario usuarioActualizadoForm = empleadoActualizado.getUsuario();

            if (!usuarioExistente.getUsername().equals(usuarioActualizadoForm.getUsername())) {
                Optional<Usuario> existingUserWithNewUsername = usuarioRepository.findByUsername(usuarioActualizadoForm.getUsername());
                if (existingUserWithNewUsername.isPresent() && !existingUserWithNewUsername.get().getId().equals(usuarioExistente.getId())) {
                    throw new IllegalArgumentException("El nombre de usuario '" + usuarioActualizadoForm.getUsername() + "' ya está en uso.");
                }
                usuarioExistente.setUsername(usuarioActualizadoForm.getUsername());
            }

            if (usuarioActualizadoForm.getPassword() != null && !usuarioActualizadoForm.getPassword().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizadoForm.getPassword()));
            }

            Empleado empleadoGuardado = empleadoRepository.save(empleadoExistente);

            auditoriaService.registrarAccion("ACTUALIZACION_EMPLEADO",
                    "Empleado '" + empleadoGuardado.getNombres() + " " + empleadoGuardado.getApellidos() + "' (ID: " + empleadoGuardado.getId() + ") actualizado.",
                    "Empleado",
                    empleadoGuardado.getId());
            return empleadoGuardado;
        }).orElseThrow(() -> new IllegalArgumentException("Empleado con ID " + empleadoActualizado.getId() + " no encontrado."));
    }

    @Transactional
    public boolean eliminarEmpleado(Long id) {
        Optional<Empleado> empleadoOptional = empleadoRepository.findById(id);
        if (empleadoOptional.isPresent()) {
            Empleado empleado = empleadoOptional.get();
            empleadoRepository.delete(empleado);
            auditoriaService.registrarAccion("ELIMINACION_EMPLEADO",
                    "Empleado '" + empleado.getNombres() + " " + empleado.getApellidos() + "' (ID: " + empleado.getId() + ") eliminado.",
                    "Empleado",
                    empleado.getId());
            return true;
        }
        return false;
    }

    public long contarEmpleados() {
        return empleadoRepository.count();
    }
}