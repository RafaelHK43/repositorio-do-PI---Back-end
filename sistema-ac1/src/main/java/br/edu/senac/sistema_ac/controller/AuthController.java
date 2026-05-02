package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.dto.AuthLoginRequest;
import br.edu.senac.sistema_ac.dto.AuthResponse;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));

        if (!usuario.getPerfil().equals(request.perfil())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Perfil informado nao corresponde ao cadastrado");
        }

        return new AuthResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil()
        );
    }
}
