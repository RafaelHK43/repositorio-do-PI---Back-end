package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.domain.enums.TipoArquivoComprovante;
import br.edu.senac.sistema_ac.dto.SubmissaoRequest;
import br.edu.senac.sistema_ac.dto.SubmissaoUpdateRequest;
import br.edu.senac.sistema_ac.repository.AtividadeComplementarRepository;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<Submissao> listarTodas() {
        return submissaoRepository.findAllByOrderByDataSubmissaoDesc();
    }

    @Transactional(readOnly = true)
    public List<Submissao> listarPorAluno(Long alunoId) {
        return submissaoRepository.findAllByAlunoIdOrderByDataSubmissaoDesc(alunoId);
    }

    @Transactional(readOnly = true)
    public Submissao buscarPorId(Long id) {
        return submissaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Submissao nao encontrada"));
    }

    @Transactional
    public Submissao atualizar(Long id, SubmissaoUpdateRequest request) {
        Submissao submissao = buscarPorId(id);

        if (request.status() != null) {
            submissao.setStatus(request.status());
        }

        if (submissao.getStatus() == StatusSubmissao.APROVADA) {
            BigDecimal horasAprovadas = request.horasAprovadas() != null
                ? request.horasAprovadas()
                : submissao.getAtividadeComplementar().getHorasDeclaradas();

            if (horasAprovadas.compareTo(submissao.getAtividadeComplementar().getHorasDeclaradas()) > 0) {
                throw new IllegalArgumentException("Horas aprovadas nao podem exceder as horas declaradas");
            }

            submissao.setHorasAprovadas(horasAprovadas);
        } else {
            submissao.setHorasAprovadas(null);
        }

        if (request.observacaoCoordenacao() != null) {
            submissao.setObservacaoCoordenacao(request.observacaoCoordenacao());
        }

        return submissaoRepository.save(submissao);
    }

    @Transactional
    public void excluir(Long id) {
        Submissao submissao = buscarPorId(id);
        AtividadeComplementar atividade = submissao.getAtividadeComplementar();
        String certificadoUrl = submissao.getCertificadoUrl();

        submissaoRepository.delete(submissao);
        atividadeComplementarRepository.delete(atividade);
        fileStorageService.removerArquivo(certificadoUrl);
    }
}
