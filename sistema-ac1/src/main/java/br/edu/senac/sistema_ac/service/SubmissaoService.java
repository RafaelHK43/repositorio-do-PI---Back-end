package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.AtividadeComplementar;
import br.edu.senac.sistema_ac.domain.entity.Curso;
import br.edu.senac.sistema_ac.domain.entity.Submissao;
import br.edu.senac.sistema_ac.domain.entity.Usuario;
import br.edu.senac.sistema_ac.domain.enums.AreaAtividade;
import br.edu.senac.sistema_ac.domain.enums.PerfilUsuario;
import br.edu.senac.sistema_ac.domain.enums.StatusSubmissao;
import br.edu.senac.sistema_ac.domain.enums.TipoArquivoComprovante;
import br.edu.senac.sistema_ac.dto.SubmissaoRequest;
import br.edu.senac.sistema_ac.dto.SubmissaoUpdateRequest;
import br.edu.senac.sistema_ac.repository.AtividadeComplementarRepository;
import br.edu.senac.sistema_ac.repository.SubmissaoRepository;
import br.edu.senac.sistema_ac.repository.UsuarioRepository;
import br.edu.senac.sistema_ac.exception.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.text.Normalizer;
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
        Long alunoId = validarAlunoId(request);
        Long cursoId = validarCursoId(request);
        Object area = validarArea(request);
        BigDecimal cargaHoraria = validarCargaHoraria(request);
        String titulo = validarTitulo(request);
        String descricao = request.getDescricao();
        var dataAtividade = validarDataAtividade(request);

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Comprovante é obrigatório.");
        }

        Usuario aluno = usuarioRepository.findById(alunoId)
            .orElseThrow(() -> new IllegalArgumentException("Aluno nao encontrado"));
        Curso curso = cursoService.buscarPorId(cursoId);

        AreaAtividade areaEnum = resolverAreaAtividade(area);

        validacaoHorasService.validarLimiteHorasPorArea(
            aluno.getId(),
            curso.getId(),
            areaEnum,
            cargaHoraria
        );

        AtividadeComplementar atividade = AtividadeComplementar.builder()
            .aluno(aluno)
            .curso(curso)
            .titulo(titulo)
            .descricao(descricao)
            .area(areaEnum)
            .horasDeclaradas(cargaHoraria)
            .dataAtividade(dataAtividade)
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
            .resultadoOcr(resultadoOcr)
            .build();

        Submissao salvo = submissaoRepository.save(submissao);
        
        // Enviar email para coordenadores
        try {
            List<Usuario> coords = usuarioRepository.findAllByFiltrosECursos(
                PerfilUsuario.COORDENADOR,
                List.of(curso.getId())
            );
            if (!coords.isEmpty()) {
                for (Usuario coord : coords) {
                    String assunto = "Nova submissão recebida!";
                    String msg = String.format("O aluno %s submeteu uma nova atividade (%s). Acesse o painel para avaliar.", 
                        aluno.getNome(), titulo);
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
    public List<Submissao> listarPorAluno(Long alunoId, StatusSubmissao status) {
        String emailLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailLogado)
            .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado"));

        if (usuarioLogado.getPerfil() == br.edu.senac.sistema_ac.domain.enums.PerfilUsuario.ALUNO && !usuarioLogado.getId().equals(alunoId)) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }

        if (status != null) {
            return submissaoRepository.findAllByAlunoIdAndStatusOrderByDataSubmissaoDesc(alunoId, status);
        }
        return submissaoRepository.findAllByAlunoIdOrderByDataSubmissaoDesc(alunoId);
    }

    @Transactional(readOnly = true)
    public Submissao buscarPorId(Long id) {
        String emailLogado = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailLogado)
            .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado"));

        Submissao submissao = submissaoRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Submissao nao encontrada"));

        if (usuarioLogado.getPerfil() == br.edu.senac.sistema_ac.domain.enums.PerfilUsuario.ALUNO && !submissao.getAluno().getId().equals(usuarioLogado.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado");
        }

        return submissao;
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

    private Long validarAlunoId(SubmissaoRequest request) {
        Long alunoId = request.getAlunoId();
        if (alunoId == null) {
            throw new IllegalArgumentException("Aluno é obrigatório.");
        }
        return alunoId;
    }

    private Long validarCursoId(SubmissaoRequest request) {
        Long cursoId = request.cursoId();
        if (cursoId == null) {
            throw new IllegalArgumentException("Curso é obrigatório.");
        }
        return cursoId;
    }

    private Object validarArea(SubmissaoRequest request) {
        Object area = request.getArea();
        if (area == null || area instanceof String areaStr && areaStr.isBlank()) {
            throw new IllegalArgumentException("Área é obrigatória.");
        }
        return area;
    }

    private BigDecimal validarCargaHoraria(SubmissaoRequest request) {
        BigDecimal cargaHoraria = request.getCargaHoraria();
        if (cargaHoraria == null || cargaHoraria.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Carga horária deve ser maior que zero.");
        }
        return cargaHoraria;
    }

    private String validarTitulo(SubmissaoRequest request) {
        String titulo = request.getTitulo();
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório.");
        }
        return titulo;
    }

    private java.time.LocalDate validarDataAtividade(SubmissaoRequest request) {
        java.time.LocalDate dataAtividade = request.getDataAtividade();
        if (dataAtividade == null) {
            throw new IllegalArgumentException("Data da atividade é obrigatória.");
        }
        return dataAtividade;
    }

    private AreaAtividade resolverAreaAtividade(Object areaId) {
        AreaAtividade[] values = AreaAtividade.values();
        try {
            if (areaId instanceof AreaAtividade areaEnum) {
                return areaEnum;
            }
            if (areaId instanceof String areaStr) {
                String trimmed = areaStr.trim();
                if (trimmed.matches("\\d+")) {
                    return resolverPorIndice(Integer.parseInt(trimmed), values);
                }
                return AreaAtividade.valueOf(normalizarArea(trimmed));
            }
            if (areaId instanceof Number num) {
                return resolverPorIndice(num.intValue(), values);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Área de atividade inválida: '" + areaId + "'. Valores aceitos: ENSINO, PESQUISA, EXTENSAO, CULTURA, EVENTOS");
        }
        throw new IllegalArgumentException("Formato de área inválido: " + areaId);
    }

    private String normalizarArea(String area) {
        return Normalizer.normalize(area, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase()
            .replace('Ç', 'C');
    }

    private AreaAtividade resolverPorIndice(int idx, AreaAtividade[] values) {
        if (idx >= 1 && idx <= values.length) {
            return values[idx - 1]; // 1-based
        }
        if (idx >= 0 && idx < values.length) {
            return values[idx]; // 0-based fallback
        }
        throw new IllegalArgumentException("Indice de area fora do intervalo valido: " + idx);
    }
}
