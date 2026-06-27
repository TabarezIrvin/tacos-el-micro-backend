package com.tacoselmicro.menu.controller;

import com.tacoselmicro.menu.entity.Producto;
import com.tacoselmicro.menu.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 🚨 Importación Obligatoria

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import com.tacoselmicro.menu.websocket.StockWebSocketHandler;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockWebSocketHandler stockWebSocketHandler;

    // Ruta física en el servidor donde se guardarán los archivos binarios
    // Nota: "uploads" estará dentro de la carpeta target/classes/static/uploads de
    // manera pública
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // Endpoint público para la app móvil (Home)
    @GetMapping("/categoria/{idCategoria}")
    public List<Producto> listarPorCategoria(
            @PathVariable Integer idCategoria,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        System.out.println("[Prueba JWT - Backend] Token recibido desde la app: " + authHeader);

        return productoRepository.findByIdCategoriaAndActivoTrue(idCategoria);
    }

    // 🚨 MODIFICADO: Método para CREAR producto con subida al FileSystem y guardado
    // en MySQL
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<?> crearProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("precioBase") java.math.BigDecimal precioBase,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("idCategoria") Integer idCategoria,
            @RequestParam(value = "imagen", required = false) MultipartFile file) {

        // Imagen por defecto por si el Administrador no sube ninguna foto
        String nombreImagenParaBaseDeDatos = "default.png";

        if (file != null && !file.isEmpty()) {
            try {
                // 1. Asegurar que el directorio en el FileSystem exista
                Path pathDir = Paths.get(UPLOAD_DIR);
                if (!Files.exists(pathDir)) {
                    Files.createDirectories(pathDir);
                }

                // 2. Crear un nombre único concatenando el timestamp para evitar duplicados
                nombreImagenParaBaseDeDatos = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path rutaCompleta = pathDir.resolve(nombreImagenParaBaseDeDatos);

                // 3. Guardar físicamente el archivo binario de la imagen en el FileSystem
                Files.copy(file.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error crítico al guardar la imagen en el FileSystem.");
            }
        }

        // 4. Instanciar el objeto Producto y mapear los campos para MySQL
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(nombre);
        nuevoProducto.setPrecioBase(precioBase);
        nuevoProducto.setDescripcion(descripcion);
        nuevoProducto.setIdCategoria(idCategoria);
        nuevoProducto.setActivo(true); // Inicializa como disponible (1)

        // 🚨 AQUÍ SE GUARDA ÚNICAMENTE LA CADENA DE TEXTO DE LA RUTA
        nuevoProducto.setUrlImagen(nombreImagenParaBaseDeDatos);

        // 5. Commit/Persistencia en la base de datos MySQL
        Producto guardado = productoRepository.save(nuevoProducto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // 1. OBTENER TODOS LOS PRODUCTOS (Para enlistarlos en la app)
    @GetMapping
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // 3. ACTUALIZAR PRODUCTO EXISTENTE (PUT)
    // 🚨 CORREGIDO: Se añade 'activo' y se asegura que el directorio de imágenes
    // exista
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @RequestParam("nombre") String nombre,
            @RequestParam("precioBase") java.math.BigDecimal precioBase,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("idCategoria") Integer idCategoria,
            @RequestParam("activo") Boolean activo,
            @RequestParam(value = "imagen", required = false) MultipartFile file) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Seteamos los datos que vienen del formulario móvil
        producto.setNombre(nombre);
        producto.setPrecioBase(precioBase);
        producto.setDescripcion(descripcion);
        producto.setIdCategoria(idCategoria);
        producto.setActivo(activo);

        // Lógica: Si el administrador subió una nueva foto en la edición
        if (file != null && !file.isEmpty()) {
            try {
                // Asegurar que el directorio exista
                Path pathDir = Paths.get(UPLOAD_DIR);
                if (!Files.exists(pathDir)) {
                    Files.createDirectories(pathDir);
                }

                String nombreImagen = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path rutaCompleta = pathDir.resolve(nombreImagen);
                Files.copy(file.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);

                producto.setUrlImagen(nombreImagen); // Actualizamos el nombre de la foto en MySQL
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        Producto actualizado = productoRepository.save(producto);
        return ResponseEntity.ok(actualizado);
    }

    // 4. ELIMINAR PRODUCTO (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        productoRepository.delete(producto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<Producto> actualizarDisponibilidad(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        Boolean disponible = body.get("disponible");
        producto.setDisponible(disponible);
        Producto actualizado = productoRepository.save(producto);

        // Notificar a todos los clientes conectados sobre el cambio de stock
        stockWebSocketHandler.notificarCambioDisponibilidad(id, disponible);

        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/imagenes/{nombreImagen:.+}")
    public ResponseEntity<Resource> obtenerImagen(@PathVariable String nombreImagen) {
        try {
            // 1. Apuntar a la ruta física del archivo
            Path rutaArchivo = Paths.get(UPLOAD_DIR).resolve(nombreImagen).normalize();
            Resource recurso = new UrlResource(rutaArchivo.toUri());

            // 2. Verificar si el archivo existe y es legible
            if (recurso.exists() || recurso.isReadable()) {

                // 3. Detectar el Content-Type de forma dinámica (png, jpg, etc.)
                String contentType = "image/jpeg"; // Fallback por defecto
                try {
                    contentType = Files.probeContentType(rutaArchivo);
                } catch (IOException e) {
                    System.out.println("No se pudo determinar el tipo de archivo, usando jpeg.");
                }

                // 4. Retornar el binario con las cabeceras HTTP correctas
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(recurso);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}