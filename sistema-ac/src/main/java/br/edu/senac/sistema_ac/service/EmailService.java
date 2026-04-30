package br.edu.senac.sistema_ac.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(destinatario);
            mail.setSubject(assunto);
            mail.setText(mensagem);

            mailSender.send(mail);
        } catch (Exception e) {
            // não propagar exceção para não quebrar fluxo principal
            System.err.println("Falha ao enviar email: " + e.getMessage());
        }
    }
}
