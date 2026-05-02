package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.dto.RegraAtividadeRequest;
import br.edu.senac.sistema_ac.dto.RegraAtividadeResponse;
import br.edu.senac.sistema_ac.service.RegraAtividadeService;
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
@RequestMapping("/api/regras")
@RequiredArgsConstructor
public class RegraAtividadeController {

    private final RegraAtividadeService regraAtividadeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegraAtividadeResponse criarOuAtualizar(@Valid @RequestBody RegraAtividadeRequest request) {
        return RegraAtividadeResponse.fromEntity(regraAtividadeService.criarOuAtualizar(
            request.cursoId(),
            request.area(),
            request.limiteHoras()
        ));
    }

    @GetMapping("/curso/{cursoId}")
    public List<RegraAtividadeResponse> listarPorCurso(@PathVariable Long cursoId) {
        return regraAtividadeService.listarPorCurso(cursoId)
            .stream()
            .map(RegraAtividadeResponse::fromEntity)
            .toList();
    }

    @GetMapping
    public List<RegraAtividadeResponse> listarTodas() {
        return regraAtividadeService.listarTodas()
            .stream()
            .map(RegraAtividadeResponse::fromEntity)
            .toList();
    }

    @GetMapping("/{id}")
    public RegraAtividadeResponse buscarPorId(@PathVariable Long id) {
        return RegraAtividadeResponse.fromEntity(regraAtividadeService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public RegraAtividadeResponse atualizar(@PathVariable Long id, @Valid @RequestBody RegraAtividadeRequest request) {
        return RegraAtividadeResponse.fromEntity(regraAtividadeService.atualizar(
            id,
            request.cursoId(),
            request.area(),
            request.limiteHoras()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        regraAtividadeService.excluir(id);
    }
}
