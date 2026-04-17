package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.domain.entity.RegraAtividade;
import br.edu.senac.sistema_ac.dto.RegraAtividadeRequest;
import br.edu.senac.sistema_ac.service.RegraAtividadeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/regras")
@RequiredArgsConstructor
public class RegraAtividadeController {

    private final RegraAtividadeService regraAtividadeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegraAtividade criarOuAtualizar(@Valid @RequestBody RegraAtividadeRequest request) {
        return regraAtividadeService.criarOuAtualizar(
            request.cursoId(),
            request.area(),
            request.limiteHoras()
        );
    }

    @GetMapping("/curso/{cursoId}")
    public List<RegraAtividade> listarPorCurso(@PathVariable Long cursoId) {
        return regraAtividadeService.listarPorCurso(cursoId);
    }
}
