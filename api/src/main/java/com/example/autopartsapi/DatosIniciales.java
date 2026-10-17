package com.example.autopartsapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Si la tabla esta vacia, carga los 8 registros de ejemplo (5 repuestos con foto y 3 servicios). */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final RepuestoServicioRepository repositorio;
    private final ImagenService imagenes;

    public DatosIniciales(RepuestoServicioRepository repositorio, ImagenService imagenes) {
        this.repositorio = repositorio;
        this.imagenes = imagenes;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repositorio.count() > 0) return;

        repuesto("Pastillas de freno delanteras", "Juego de pastillas cerámicas para eje delantero, bajo ruido y alta duración.",
                "35.50", 24, "08605714-820e-415b-9e31-fea1bdb00321.jpg");
        repuesto("Disco de frenos", "Disco ventilado delantero, díametro 266mm, compatible con la mayoría de sedanes.",
                "62.00", 10, "5d1c8f2a-3b7e-4c11-9a52-7f0e6d4b1a90.jpg");
        repuesto("Bujías de iridio", "Juego de 4 bujías de iridio, mejor encendido y menor consumo de combustible.",
                "28.75", 40, "a3f9e6b1-52c8-4d07-8e34-1b9c7d2f5a68.jpg");
        repuesto("Filtro de aceite", "Filtro de aceite de alta eficiencia para motores 1.6 a 2.0 litros.",
                "9.90", 60, "c7e24d90-8a15-4f3b-b6d1-0e5a93c8f742.jpg");
        repuesto("Batería 12V 60Ah", "Batería libre de mantenimiento, 12 voltios y 60 amperios hora, garantía de 12 meses.",
                "95.00", 8, "e19b5a73-64d2-4c8f-a07e-3d6f8b1c2a45.jpg");

        servicio("Mantenimiento de frenos", "Revisión completa del sistema de frenos: pastillas, discos, líquido y ajuste.", "45.00");
        servicio("Limpieza de inyectores", "Limpieza ultrasónica de inyectores para recuperar potencia y reducir consumo.", "60.00");
        servicio("Cambio de aceite y filtro", "Cambio de aceite de motor con filtro nuevo e inspección de niveles.", "30.00");
    }

    private void repuesto(String nombre, String descripcion, String precio, int stock, String foto) throws Exception {
        try (InputStream in = new ClassPathResource("seed/" + foto).getInputStream()) {
            Files.copy(in, imagenes.getCarpeta().resolve(foto), StandardCopyOption.REPLACE_EXISTING);
        }
        guardar(nombre, descripcion, precio, stock, foto, TipoItem.REPUESTO);
    }

    private void servicio(String nombre, String descripcion, String precio) {
        guardar(nombre, descripcion, precio, 0, null, TipoItem.SERVICIO);
    }

    private void guardar(String nombre, String descripcion, String precio, int stock, String foto, TipoItem tipo) {
        RepuestoServicio e = new RepuestoServicio();
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        e.setPrecio(new BigDecimal(precio));
        e.setStock(stock);
        e.setFoto(foto);
        e.setTipo(tipo);
        repositorio.save(e);
    }
}
