package com.example.autopartsapi;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cinco endpoints que consume la app AutoParts (guia 9_2). Sin autenticacion:
 * el login se agrega en la guia 9_3.
 */
@RestController
@RequestMapping("/api/repuestos-servicios")
public class RepuestoServicioController {

    private final RepuestoServicioRepository repositorio;
    private final ImagenService imagenes;

    public RepuestoServicioController(RepuestoServicioRepository repositorio, ImagenService imagenes) {
        this.repositorio = repositorio;
        this.imagenes = imagenes;
    }

    // GET /api/repuestos-servicios  -> lista con foto, precio y tipo
    @GetMapping
    public List<RepuestoServicioDTO> obtenerTodos() {
        return repositorio.findAll().stream().map(RepuestoServicioDTO::desde).toList();
    }

    // GET /api/repuestos-servicios/{id}  -> detalle
    @GetMapping("/{id}")
    public RepuestoServicioDTO obtenerPorId(@PathVariable Integer id) {
        return RepuestoServicioDTO.desde(buscar(id));
    }

    // POST multipart: parte "dto" (JSON) + parte "imagen" (opcional)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> crear(
            @RequestPart("dto") @Valid RepuestoServicioDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        RepuestoServicio e = new RepuestoServicio();
        copiarCampos(dto, e);
        if (imagen != null && !imagen.isEmpty()) e.setFoto(imagenes.guardar(imagen));
        RepuestoServicio guardado = repositorio.save(e);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(respuesta("Registrado correctamente", guardado));
    }

    // PUT multipart: igual que POST; si no llega imagen se conserva la anterior
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> actualizar(
            @PathVariable Integer id,
            @RequestPart("dto") @Valid RepuestoServicioDTO dto,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen) {

        RepuestoServicio e = buscar(id);
        copiarCampos(dto, e);
        if (imagen != null && !imagen.isEmpty()) {
            String anterior = e.getFoto();
            e.setFoto(imagenes.guardar(imagen));
            imagenes.borrar(anterior);
        }
        return respuesta("Actualizado correctamente", repositorio.save(e));
    }

    // DELETE /api/repuestos-servicios/{id}
    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Integer id) {
        RepuestoServicio e = buscar(id);
        repositorio.delete(e);
        imagenes.borrar(e.getFoto());
        return Map.of("message", "Eliminado correctamente");
    }

    private RepuestoServicio buscar(Integer id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "El repuesto o servicio no existe"));
    }

    /** La foto no se copia del DTO: solo cambia cuando llega una imagen nueva. */
    private void copiarCampos(RepuestoServicioDTO dto, RepuestoServicio e) {
        e.setNombre(dto.nombre().trim());
        e.setDescripcion(dto.descripcion() == null || dto.descripcion().isBlank() ? null : dto.descripcion().trim());
        e.setPrecio(dto.precio());
        e.setTipo(dto.tipo());
        e.setStock(dto.tipo() == TipoItem.SERVICIO ? 0 : dto.stock());
    }

    private Map<String, Object> respuesta(String mensaje, RepuestoServicio e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("message", mensaje);
        cuerpo.put("repuestoServicio", RepuestoServicioDTO.desde(e));
        return cuerpo;
    }
}
