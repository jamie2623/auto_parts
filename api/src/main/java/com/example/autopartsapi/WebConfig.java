package com.example.autopartsapi;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las fotos en http://localhost:8080/images/{foto} (fuera de /api). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImagenService imagenes;

    public WebConfig(ImagenService imagenes) {
        this.imagenes = imagenes;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagenes.getCarpeta().toUri().toString());
    }
}
