package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.dto.SubmissaoRequest;
import br.edu.senac.sistema_ac.dto.SubmissaoUpdateRequest;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;
import br.edu.senac.sistema_ac.dto.SubmissaoResponse;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/submissoes")
@RequiredArgsConstructor
public class SubmissaoController {

    private final SubmissaoService submissaoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissaoResponse criar(
        @Valid @RequestPart("dados") SubmissaoRequest request,
        @RequestPart("arquivo") MultipartFile arquivo
    ) {
        return SubmissaoResponse.fromEntity(submissaoService.criar(request, arquivo));
    }

    @GetMapping
    public List<SubmissaoResponse> listarTodas() {
        return submissaoService.listarTodas().stream().map(SubmissaoResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public SubmissaoResponse buscarPorId(@PathVariable Long id) {
        return SubmissaoResponse.fromEntity(submissaoService.buscarPorId(id));
    }

    @GetMapping("/aluno/{alunoId}")
    public List<SubmissaoResponse> listarPorAluno(
            @PathVariable Long alunoId,
            @RequestParam(required = false) StatusSubmissao status) {
        return submissaoService.listarPorAluno(alunoId, status).stream().map(SubmissaoResponse::fromEntity).toList();
    }

    @PutMapping("/{id}")
    public SubmissaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody SubmissaoUpdateRequest request) {
        return SubmissaoResponse.fromEntity(submissaoService.atualizar(id, request));
    }

    @PutMapping("/{id}/aprovar")
    public SubmissaoResponse aprovar(@PathVariable Long id) {
        SubmissaoUpdateRequest req = new SubmissaoUpdateRequest(StatusSubmissao.APROVADA, null, null);
        return SubmissaoResponse.fromEntity(submissaoService.atualizar(id, req));
    }

    @PutMapping("/{id}/reprovar")
    public SubmissaoResponse reprovar(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String motivo = body == null ? null : body.get("motivo");
        SubmissaoUpdateRequest req = new SubmissaoUpdateRequest(StatusSubmissao.REPROVADA, null, motivo);
        return SubmissaoResponse.fromEntity(submissaoService.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        submissaoService.excluir(id);
    }
}
