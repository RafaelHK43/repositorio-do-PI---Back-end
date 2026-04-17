package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.domain.enums.TipoArquivoComprovante;
import br.edu.senac.sistema_ac.dto.SubmissaoRequest;
import br.edu.senac.sistema_ac.repository.AtividadeComplementarRepository;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SubmissaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AtividadeComplementarRepository atividadeComplementarRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoService cursoService;
    private final ValidacaoHorasService validacaoHorasService;
    private final FileStorageService fileStorageService;
    private final OcrService ocrService;

    @Transactional
    public Submissao criar(SubmissaoRequest request, MultipartFile arquivo) {
        Usuario aluno = usuarioRepository.findById(request.alunoId())
            .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        Curso curso = cursoService.buscarPorId(request.cursoId());

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo do comprovante e obrigatorio");
        }

        validacaoHorasService.validarLimiteHorasPorArea(
            aluno.getId(),
            curso.getId(),
            request.area(),
            request.horasDeclaradas()
        );

        AtividadeComplementar atividade = AtividadeComplementar.builder()
            .aluno(aluno)
            .curso(curso)
            .titulo(request.titulo())
            .descricao(request.descricao())
            .area(request.area())
            .horasDeclaradas(request.horasDeclaradas())
            .dataAtividade(request.dataAtividade())
            .build();

        atividade = atividadeComplementarRepository.save(atividade);

        String caminhoArquivo = fileStorageService.salvarComprovante(arquivo, aluno.getId());
        String resultadoOcr = ocrService.extrairDados(arquivo);

        Submissao submissao = Submissao.builder()
            .aluno(aluno)
            .atividadeComplementar(atividade)
            .status(StatusSubmissao.PENDENTE)
            .certificadoUrl(caminhoArquivo)
            .nomeArquivoComprovante(arquivo.getOriginalFilename())
            .tipoArquivoComprovante(TipoArquivoComprovante.fromContentType(arquivo.getContentType()))
            .observacaoCoordenacao(resultadoOcr)
            .build();

        return submissaoRepository.save(submissao);
    }
}
