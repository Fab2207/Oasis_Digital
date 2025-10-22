package com.gestion.hotelera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RegisterRequest {

    private String username;
    private String password;
    private String nombres;
    private String apellidos;
    private String dni;
    private String nacionalidad;
    private String email;
    private String telefono;

}