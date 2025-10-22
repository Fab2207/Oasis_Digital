package com.gestion.hotelera.config;

import com.gestion.hotelera.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import java.io.IOException;
import jakarta.servlet.ServletException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .disable())
            .authorizeHttpRequests(auth -> auth
                // RUTAS PÚBLICAS
                .requestMatchers("/", "/index", "/home", "/login",
                                 "/css/**", "/js/**", "/images/**",
                                 "/h2-console/**", "/api/auth/**",
                                 "/habitaciones/publico")
                .permitAll()

                // ADMIN
                .requestMatchers("/empleados/**", "/admin/**", "/auditoria/logs")
                .hasAuthority("ROLE_ADMIN")

                // CLIENTE: permitir editar su propio perfil y reservar
                .requestMatchers("/clientes/editar/**").hasAuthority("ROLE_CLIENTE")

                // RECEPCIONISTA o ADMIN
                .requestMatchers("/clientes/**", "/reservas/crear", "/habitaciones/**")
                .hasAnyAuthority("ROLE_ADMIN", "ROLE_RECEPCIONISTA")

                // CLIENTE
                .requestMatchers("/cliente/**", "/cliente/reservas/**")
                .hasAuthority("ROLE_CLIENTE")

                // DASHBOARD (requiere login)
                .requestMatchers("/dashboard")
                .authenticated()

                // Cualquier otra ruta requiere autenticación
                .anyRequest().permitAll())
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll())
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()))
            .build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request,
                                                jakarta.servlet.http.HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                boolean isStaff = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> "ROLE_ADMIN".equals(a) || "ROLE_RECEPCIONISTA".equals(a));
                if (isStaff) {
                    response.sendRedirect("/dashboard");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }
}
