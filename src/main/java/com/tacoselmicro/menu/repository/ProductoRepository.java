package com.tacoselmicro.menu.repository;

import com.tacoselmicro.menu.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Query Method automático de JPA para filtrar por categoría y estado activo en la app
    List<Producto> findByIdCategoriaAndActivoTrue(Integer idCategoria);
}