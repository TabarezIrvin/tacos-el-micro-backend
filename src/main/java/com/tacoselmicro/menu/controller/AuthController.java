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
        if (usuarioRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
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
        
    	Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // --- DIAGNÓSTICO PROFUNDO ---
            System.out.println("DEBUG: Email encontrado: " + usuario.getEmail());
            System.out.println("DEBUG: Password BD: '" + usuario.getPassword() + "'");
            System.out.println("DEBUG: Password recibido: '" + loginRequest.getPassword() + "'");
            System.out.println("DEBUG: ¿Son iguales?: " + usuario.getPassword().equals(loginRequest.getPassword()));
            // ----------------------------

            if (usuario.getPassword().equals(loginRequest.getPassword())) {
                 // ... tu lógica de éxito
                 return ResponseEntity.ok(Map.of("mensaje", "Autenticación exitosa"));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Credenciales incorrectas"));
    }
}