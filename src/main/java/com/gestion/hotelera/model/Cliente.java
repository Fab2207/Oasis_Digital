package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clientes")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank
    @Size(min = 8, max = 8)
    @Pattern(regexp = "^[0-9]{8}$")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s-]*$")
    @Column(length = 50)
    private String nacionalidad;

    @Email
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Transient
    private boolean hasActiveReservations;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
}
