package br.edu.senac.sistema_ac.service;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OcrService {

    public String extrairDados(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo invalido para OCR");
        }

        String contentType = arquivo.getContentType();
        if (contentType != null && contentType.equals("application/pdf")) {
            return extrairTextoPdf(arquivo);
        }

        return "Arquivo recebido: " + arquivo.getOriginalFilename();
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
}
