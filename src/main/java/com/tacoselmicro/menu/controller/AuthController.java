package com.tacoselmicro.menu.controller;

import com.tacoselmicro.menu.dto.LoginRequest;
import com.tacoselmicro.menu.dto.RegisterRequest;
import com.tacoselmicro.menu.entity.Role;
import com.tacoselmicro.menu.entity.Usuario;
import com.tacoselmicro.menu.repository.RoleRepository;
import com.tacoselmicro.menu.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegisterRequest registerRequest) {
        if (usuarioRepository.findByEmailAndActivoTrue(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El correo ya está registrado"));
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(registerRequest.getUsername() != null ? registerRequest.getUsername() : registerRequest.getEmail());
        nuevoUsuario.setNombre(registerRequest.getNombre());
        nuevoUsuario.setEmail(registerRequest.getEmail());
        nuevoUsuario.setPassword(registerRequest.getPassword()); // Nota: En producción usar Hashing
        nuevoUsuario.setTelefono(registerRequest.getTelefono());
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());
        nuevoUsuario.setActivo(true);

        Role rolCliente = roleRepository.findByNombre("CLIENTE").orElseThrow(() -> new RuntimeException("Error: Rol no encontrado"));
        nuevoUsuario.setRol(rolCliente);

        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.ok(Map.of("mensaje", "Usuario registrado exitosamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> autenticarUsuario(@RequestBody LoginRequest loginRequest) {
        
        // 1. Buscar en la base de datos por el correo electrónico recibido
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndActivoTrue(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // 2. Validar la contraseña en texto plano
            if (usuario.getPassword().equals(loginRequest.getPassword())) {
                
                // 3. Respuesta estructurada si coincide el password
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Autenticación exitosa");
                respuesta.put("idUsuario", usuario.getId());
                respuesta.put("username", usuario.getUsername());
                respuesta.put("email", usuario.getEmail());
                respuesta.put("role", usuario.getRol().getNombre());
                respuesta.put("token", "JWT_TOKEN_GENERADO_DE_FORMA_REAL_MYSQL");

                return ResponseEntity.ok(respuesta);
            }
        }

        // 4. Retorno de error unificado si el correo no existe o el password es incorrecto
        Map<String, String> errorRespuesta = new HashMap<>();
        errorRespuesta.put("error", "Unauthorized");
        errorRespuesta.put("mensaje", "El correo electrónico o la contraseña son incorrectos.");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorRespuesta);
    }
}