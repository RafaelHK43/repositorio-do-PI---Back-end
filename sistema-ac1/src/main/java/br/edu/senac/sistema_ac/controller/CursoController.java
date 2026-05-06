package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.dto.CursoRequest;
import br.edu.senac.sistema_ac.service.CursoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Curso criar(@Valid @RequestBody CursoRequest request) {
        return cursoService.criar(request);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public List<Curso> listar(org.springframework.security.core.Authentication authentication) {
        return cursoService.listar(authentication);
    }

    @GetMapping("/{id}")
    public Curso buscarPorId(@PathVariable Long id) {
        return cursoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Curso atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
        return cursoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        cursoService.excluir(id);
    }
}
