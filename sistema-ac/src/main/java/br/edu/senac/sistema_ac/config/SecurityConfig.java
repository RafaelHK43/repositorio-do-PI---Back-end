package br.edu.senac.sistema_ac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/cursos/**", "/api/regras/**")
                    .hasAnyRole("SUPER_ADMIN", "COORDENADOR")
                .requestMatchers(HttpMethod.GET, "/api/cursos/**", "/api/regras/**")
                    .hasAnyRole("SUPER_ADMIN", "COORDENADOR", "ALUNO")
                .requestMatchers(HttpMethod.POST, "/api/submissoes/**").hasRole("ALUNO")
                .requestMatchers("/api/submissoes/**").hasAnyRole("SUPER_ADMIN", "COORDENADOR", "ALUNO")
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
