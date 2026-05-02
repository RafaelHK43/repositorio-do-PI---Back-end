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

    private static final long TAMANHO_MAXIMO_COMPROVANTE = 20L * 1024 * 1024;

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

    public Path buscarComprovante(String nomeArquivo) {
        String nomeSanitizado = extrairNomeArquivo(nomeArquivo);
        Path arquivo = uploadPath.resolve(nomeSanitizado).normalize();

        if (!arquivo.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Nome de arquivo invalido");
        }

        return arquivo;
    }

    /**
     * Saves the file and returns the relative URL used by the API.
     */
    public String salvarComprovante(MultipartFile arquivo, Long alunoId) {
        validarComprovante(arquivo);

        String nomeOriginal = Objects.requireNonNullElse(arquivo.getOriginalFilename(), "arquivo");
        String nomeSanitizado = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        String nomeFinal = alunoId + "_" + UUID.randomUUID() + "_" + nomeSanitizado;

        try {
            Path destino = uploadPath.resolve(nomeFinal);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return "/api/uploads/" + nomeFinal;
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao salvar arquivo do comprovante", ex);
        }
    }

    private void validarComprovante(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Comprovante é obrigatório.");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO_COMPROVANTE) {
            throw new IllegalArgumentException("O arquivo enviado é muito grande. Envie um arquivo de até 20MB.");
        }

        String contentType = arquivo.getContentType();
        if (!ehTipoPermitido(contentType)) {
            throw new IllegalArgumentException("Tipo de arquivo não suportado. Envie PDF, PNG, JPG ou JPEG.");
        }
    }

    private boolean ehTipoPermitido(String contentType) {
        if (contentType == null) {
            return false;
        }

        return "application/pdf".equalsIgnoreCase(contentType)
            || "image/png".equalsIgnoreCase(contentType)
            || "image/jpeg".equalsIgnoreCase(contentType)
            || "image/jpg".equalsIgnoreCase(contentType);
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
            Path alvo = Paths.get(extrairNomeArquivo(nomeOuCaminho));
            if (!alvo.isAbsolute()) {
                alvo = uploadPath.resolve(alvo);
            }
            Files.deleteIfExists(alvo);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao remover arquivo do comprovante", ex);
        }
    }

    private String extrairNomeArquivo(String nomeOuCaminho) {
        String normalizado = nomeOuCaminho.replace("\\", "/");
        int lastSep = normalizado.lastIndexOf('/');
        return lastSep >= 0 ? normalizado.substring(lastSep + 1) : normalizado;
    }
}
