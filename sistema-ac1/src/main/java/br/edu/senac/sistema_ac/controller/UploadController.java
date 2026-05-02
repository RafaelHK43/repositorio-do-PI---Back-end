package br.edu.senac.sistema_ac.controller;

import br.edu.senac.sistema_ac.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{nomeArquivo:.+}")
    public ResponseEntity<Resource> abrirComprovante(@PathVariable String nomeArquivo) throws IOException {
        Path arquivo = fileStorageService.buscarComprovante(nomeArquivo);

        if (!Files.exists(arquivo) || !Files.isRegularFile(arquivo)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(arquivo);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        Resource resource = new FileSystemResource(arquivo);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.getFileName() + "\"")
            .body(resource);
    }
}
