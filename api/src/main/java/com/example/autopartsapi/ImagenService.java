package com.example.autopartsapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Guarda y borra las fotos en la carpeta app.images-dir. */
@Service
public class ImagenService {

    private final Path carpeta;

    public ImagenService(@Value("${app.images-dir}") String dir) throws IOException {
        this.carpeta = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    public Path getCarpeta() { return carpeta; }

    /** Guarda la imagen subida con un nombre UUID y devuelve ese nombre. */
    public String guardar(MultipartFile archivo) {
        String tipo = archivo.getContentType();
        String ext;
        if ("image/png".equals(tipo)) ext = "png";
        else if ("image/jpeg".equals(tipo) || "image/jpg".equals(tipo)) ext = "jpg";
        else if ("image/webp".equals(tipo)) ext = "webp";
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen debe ser JPG, PNG o WEBP");
        String nombre = UUID.randomUUID() + "." + ext;
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
        }
        return nombre;
    }

    public void borrar(String nombre) {
        if (nombre == null || nombre.isBlank()) return;
        try {
            Path destino = carpeta.resolve(nombre).normalize();
            if (destino.startsWith(carpeta)) Files.deleteIfExists(destino);
        } catch (IOException ignorado) {
            // Si no se puede borrar el archivo, el registro igual se elimina.
        }
    }
}
