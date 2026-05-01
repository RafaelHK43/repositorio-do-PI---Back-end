package br.edu.senac.sistema_ac.config;

import br.edu.senac.sistema_ac.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = "file:" + fileStorageService.getUploadPath().toString() + "/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
