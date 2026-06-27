package com.tacoselmicro.pedidos.repository;

import com.tacoselmicro.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    List<Pedido> findByEstatusInOrderByIdDesc(List<String> estatusList);
    
    List<Pedido> findByEstatus(String estatus);
    
    List<Pedido> findByIdUsuarioOrderByFechaPedidoDesc(Long idUsuario);
    
    List<Pedido> findByIdUsuarioOrderByIdDesc(Integer idUsuario);

    @Query("SELECT SUM(p.totalPagar) FROM Pedido p WHERE p.estatus != 'CANCELADO'")
    Double sumTotalRevenue();

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.estatus != 'CANCELADO'")
    Long countActiveOrders();

    @Query("SELECT FUNCTION('DATE', p.fechaPedido) as fecha, SUM(p.totalPagar) as total " +
           "FROM Pedido p WHERE p.estatus != 'CANCELADO' " +
           "GROUP BY FUNCTION('DATE', p.fechaPedido) ORDER BY fecha DESC")
    List<Object[]> getWeeklyRevenue();
}