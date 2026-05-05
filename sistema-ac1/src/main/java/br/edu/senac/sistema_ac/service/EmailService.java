package br.edu.senac.sistema_ac.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${sgac.mail.enabled:false}")
    private boolean mailEnabled;

    @Async
    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        if (!mailEnabled) {
            logger.info("[EMAIL SIMULADO] Para: {} | Assunto: {} | Mensagem: {}",
                    destinatario, assunto, mensagem);
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(remetente);
            mail.setTo(destinatario);
            mail.setSubject(assunto);
            mail.setText(mensagem);
            mailSender.send(mail);
        } catch (Exception e) {
            logger.error("Falha ao enviar email para '{}': {}", destinatario, e.getMessage(), e);
        }
    }
}
