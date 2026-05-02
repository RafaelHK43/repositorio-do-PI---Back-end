package br.edu.senac.sistema_ac.service;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OcrService {

    private static final int TAMANHO_MAXIMO_RESULTADO_OCR = 5000;

    public String extrairDados(MultipartFile arquivo) {
        try {
            if (arquivo == null || arquivo.isEmpty()) {
                return "OCR não processado";
            }

            String contentType = arquivo.getContentType();
            if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
                return limitarResultado(extrairTextoPdf(arquivo));
            }

            return limitarResultado("Certificado processado: " + arquivo.getOriginalFilename());
        } catch (Exception e) {
            return "OCR não processado";
        }
    }

    private String extrairTextoPdf(MultipartFile arquivo) {
        try (PDDocument document = Loader.loadPDF(arquivo.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);
            return texto == null || texto.isBlank()
                ? "PDF sem texto extraivel"
                : texto.trim();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar PDF: " + e.getMessage(), e);
        }
    }

    private String limitarResultado(String texto) {
        if (texto == null || texto.isBlank()) {
            return "OCR não processado";
        }

        String textoLimpo = texto.trim();
        if (textoLimpo.length() <= TAMANHO_MAXIMO_RESULTADO_OCR) {
            return textoLimpo;
        }

        return textoLimpo.substring(0, TAMANHO_MAXIMO_RESULTADO_OCR);
    }
}
