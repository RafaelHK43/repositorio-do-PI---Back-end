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

    public Path getUploadPath() {
        return uploadPath;
    }

    /**
     * Saves the file and returns only the generated filename (not the absolute path),
     * so it can be used to construct a public URL.
     */
    public String salvarComprovante(MultipartFile arquivo, Long alunoId) {
        String nomeOriginal = Objects.requireNonNullElse(arquivo.getOriginalFilename(), "arquivo");
        String nomeSanitizado = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = alunoId + "_" + UUID.randomUUID() + "_" + nomeSanitizado;

        try {
            Path destino = uploadPath.resolve(nomeFinal);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nomeFinal;
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao salvar arquivo do comprovante", ex);
        }
    }

    /**
     * Accepts either a bare filename or an absolute path.
     * Bare filenames are resolved against the configured upload directory.
     */
    public void removerArquivo(String nomeOuCaminho) {
        if (nomeOuCaminho == null || nomeOuCaminho.isBlank()) {
            return;
        }

        try {
            Path alvo = Paths.get(nomeOuCaminho);
            if (!alvo.isAbsolute()) {
                alvo = uploadPath.resolve(nomeOuCaminho);
            }
            Files.deleteIfExists(alvo);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao remover arquivo do comprovante", ex);
        }
    }
}
