package com.tacoselmicro.menu.repository;

import com.tacoselmicro.menu.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Ahora busca en MySQL usando la columna email y valida que el usuario esté activo
    Optional<Usuario> findByEmailAndActivoTrue(String email);
}