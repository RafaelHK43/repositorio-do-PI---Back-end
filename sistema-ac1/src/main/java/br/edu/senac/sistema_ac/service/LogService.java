package br.edu.senac.sistema_ac.service;

import br.edu.senac.sistema_ac.domain.entity.LogAcao;
import br.edu.senac.sistema_ac.repository.LogAcaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogAcaoRepository logAcaoRepository;

    @Transactional
    public void registrarLog(String acao, String usuarioEmail) {
        LogAcao logAcao = LogAcao.builder()
            .acao(acao)
            .usuarioEmail(usuarioEmail)
            .build();
            
        logAcaoRepository.save(logAcao);
        
        System.out.println("[AUDIT LOG] " + usuarioEmail + " -> " + acao);
    }
}
