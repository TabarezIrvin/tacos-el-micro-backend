package com.tacoselmicro.menu.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.tacoselmicro.menu.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByNombre(String nombre);
}
