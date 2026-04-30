package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
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
    private final EmailService emailService;
    private final LogService logService;

    @Transactional
    public Submissao criar(SubmissaoRequest request, MultipartFile arquivo) {
        Usuario aluno = usuarioRepository.findById(request.studentId())
            .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        Curso curso = cursoService.buscarPorId(request.cursoId());

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo do comprovante e obrigatorio");
        }

        AreaAtividade areaEnum;
        if (request.areaId() instanceof String) {
            String areaStr = (String) request.areaId();
            if (areaStr.matches("\\d+")) {
                areaEnum = AreaAtividade.values()[Integer.parseInt(areaStr) - 1]; // Assume id 1-based, ou ajuste se for 0-based
            } else {
                areaEnum = AreaAtividade.valueOf(areaStr.toUpperCase());
            }
        } else if (request.areaId() instanceof Number) {
            int id = ((Number) request.areaId()).intValue();
            areaEnum = AreaAtividade.values()[id - 1];
        } else {
            throw new IllegalArgumentException("Formato de areaId invalido");
        }

        validacaoHorasService.validarLimiteHorasPorArea(
            aluno.getId(),
            curso.getId(),
            areaEnum,
            request.workload()
        );

        AtividadeComplementar atividade = AtividadeComplementar.builder()
            .aluno(aluno)
            .curso(curso)
            .titulo(request.title())
            .descricao(request.descricao())
            .area(areaEnum)
            .horasDeclaradas(request.workload())
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

        Submissao salvo = submissaoRepository.save(submissao);
        
        // Enviar email para coordenadores
        try {
            Long countCoords = usuarioRepository.countByPerfil(br.edu.senac.sistema_ac.domain.enums.PerfilUsuario.COORDENADOR);
            if (countCoords > 0) {
                // Simplificação: notifica coordenador genérico ou lista de coordenadores
                // Aqui pegamos o primeiro coordenador como exemplo (ajuste conforme regra de negócio)
                // Idealmente buscaríamos "findByPerfil", para este código vamos enviar para todos com perfil COORDENADOR
                List<Usuario> coords = usuarioRepository.findAll().stream()
                    .filter(u -> u.getPerfil() == br.edu.senac.sistema_ac.domain.enums.PerfilUsuario.COORDENADOR)
                    .toList();
                    
                for (Usuario coord : coords) {
                    String assunto = "Nova submissão recebida!";
                    String msg = String.format("O aluno %s submeteu uma nova atividade (%s). Acesse o painel para avaliar.", 
                        aluno.getNome(), request.title());
                    emailService.enviarEmail(coord.getEmail(), assunto, msg);
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao enviar email para coordenação: " + e.getMessage());
        }

        return salvo;
    }

    @Transactional(readOnly = true)
    public List<Submissao> listarTodas() {
        String emailLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailLogado)
            .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado"));

        if (usuarioLogado.getPerfil() == br.edu.senac.sistema_ac.domain.enums.PerfilUsuario.SUPER_ADMIN) {
            return submissaoRepository.findAllByOrderByDataSubmissaoDesc();
        } else if (usuarioLogado.getPerfil() == br.edu.senac.sistema_ac.domain.enums.PerfilUsuario.COORDENADOR) {
            List<Long> cursoIds = usuarioLogado.getCursos().stream().map(Curso::getId).toList();
            if (cursoIds.isEmpty()) return List.of();
            return submissaoRepository.findAllByAtividadeComplementarCursoIdInOrderByDataSubmissaoDesc(cursoIds);
        } else {
            // Se for ALUNO, retorna apenas as dele
            return submissaoRepository.findAllByAlunoIdOrderByDataSubmissaoDesc(usuarioLogado.getId());
        }
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

        // capturar status anterior para detectar mudança
        var statusAnterior = submissao.getStatus();

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

        Submissao salvo = submissaoRepository.save(submissao);

        // disparar email se houve mudanca de status para APROVADA ou REPROVADA
        if (statusAnterior != salvo.getStatus()) {
            Usuario aluno = salvo.getAluno();
            String email = aluno.getEmail();
            String nomeAluno = aluno.getNome();
            String tituloAtividade = salvo.getAtividadeComplementar().getTitulo();
            String nomeCurso = salvo.getAtividadeComplementar().getCurso().getNome();

            if (salvo.getStatus() == StatusSubmissao.APROVADA) {
                String assunto = "SGAC - Horas Complementares Aprovadas!";
                String mensagem = String.format("Olá %s, suas horas referentes à atividade '%s' do curso '%s' foram aprovadas!",
                    nomeAluno, tituloAtividade, nomeCurso);
                emailService.enviarEmail(email, assunto, mensagem);

                String logMsg = String.format("Submissão ID %d aprovada. Aluno: %s. Curso: %s", salvo.getId(), nomeAluno, nomeCurso);
                String emailLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
                logService.registrarLog(logMsg, emailLogado);
            } else if (salvo.getStatus() == StatusSubmissao.REPROVADA) {
                String assunto = "SGAC - Horas Complementares Reprovadas";
                String motivo = request.observacaoCoordenacao() != null ? request.observacaoCoordenacao() : "Motivo nao informado";
                String mensagem = String.format("Olá %s, infelizmente sua submissão foi reprovada. Motivo: %s",
                    nomeAluno, motivo);
                emailService.enviarEmail(email, assunto, mensagem);

                String logMsg = String.format("Submissão ID %d reprovada. Aluno: %s. Motivo: %s", salvo.getId(), nomeAluno, motivo);
                String emailLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
                logService.registrarLog(logMsg, emailLogado);
            }
        }

        return salvo;
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
