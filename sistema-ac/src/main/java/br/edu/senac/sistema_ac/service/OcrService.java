package br.edu.senac.sistema_ac.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OcrService {

    public String extrairDados(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo invalido para OCR");
        }
        return "Certificado Processado";
    }
}
