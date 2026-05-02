package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.dto.UsuarioRequestDTO;
import br.edu.senac.sistema_ac.dto.UsuarioResponseDTO;
import br.edu.senac.sistema_ac.service.UsuarioService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) br.edu.senac.sistema_ac.domain.enums.PerfilUsuario perfil,
            @RequestParam(required = false) Long cursoId,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.listarTodos(perfil, cursoId, authentication));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioLogado(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioLogado(authentication));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO criado = usuarioService.salvar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO atualizado = usuarioService.atualizar(id, request);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
