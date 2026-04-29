package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.dto.SubmissaoRequest;
import br.edu.senac.sistema_ac.dto.SubmissaoUpdateRequest;
import br.edu.senac.sistema_ac.service.SubmissaoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/submissoes")
@RequiredArgsConstructor
public class SubmissaoController {

    private final SubmissaoService submissaoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Submissao criar(
        @Valid @RequestPart("dados") SubmissaoRequest request,
        @RequestPart("arquivo") MultipartFile arquivo
    ) {
        return submissaoService.criar(request, arquivo);
    }

    @GetMapping
    public List<Submissao> listarTodas() {
        return submissaoService.listarTodas();
    }

    @GetMapping("/{id}")
    public Submissao buscarPorId(@PathVariable Long id) {
        return submissaoService.buscarPorId(id);
    }

    @GetMapping("/aluno/{alunoId}")
    public List<Submissao> listarPorAluno(@PathVariable Long alunoId) {
        return submissaoService.listarPorAluno(alunoId);
    }

    @PutMapping("/{id}")
    public Submissao atualizar(@PathVariable Long id, @Valid @RequestBody SubmissaoUpdateRequest request) {
        return submissaoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        submissaoService.excluir(id);
    }
}
