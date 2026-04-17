package br.edu.senac.sistema_ac.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${app.storage.upload-dir:uploads/comprovantes}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel criar diretorio de upload", ex);
        }
    }

    public String salvarComprovante(MultipartFile arquivo, Long alunoId) {
        String nomeOriginal = Objects.requireNonNullElse(arquivo.getOriginalFilename(), "arquivo");
        String nomeSanitizado = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = alunoId + "_" + UUID.randomUUID() + "_" + nomeSanitizado;

        try {
            Path destino = uploadPath.resolve(nomeFinal);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return destino.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao salvar arquivo do comprovante", ex);
        }
    }
}
