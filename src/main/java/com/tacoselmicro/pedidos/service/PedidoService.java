package com.tacoselmicro.pedidos.service;

import com.tacoselmicro.pedidos.entity.Pedido;
import com.tacoselmicro.pedidos.entity.PedidoDetalle;
import com.tacoselmicro.pedidos.repository.PedidoRepository;
import com.tacoselmicro.pedidos.websocket.PedidoWebSocketHandler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired(required = false)
    private PedidoWebSocketHandler pedidoWebSocketHandler;

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        Double totalSales = pedidoRepository.sumTotalRevenue();
        stats.put("totalSales", totalSales != null ? totalSales : 0.0);

        Long ordersCount = pedidoRepository.countActiveOrders();
        stats.put("ordersCount", ordersCount != null ? ordersCount : 0);

        List<Object[]> weeklyData = pedidoRepository.getWeeklyRevenue();
        List<Double> revenueList = new ArrayList<>();
        // Tomamos los últimos 7 días o rellenamos con 0
        for (int i = 0; i < 7; i++) {
            if (i < weeklyData.size()) {
                revenueList.add(((Number) weeklyData.get(i)[1]).doubleValue());
            } else {
                revenueList.add(0.0);
            }
        }
        stats.put("weeklyRevenue", revenueList);

        // Datos estáticos por ahora para evitar complejidad excesiva de JOINS en este
        // paso
        stats.put("topProduct", "Tacos al Pastor");
        stats.put("peakHour", "20:00 PM");

        return stats;
    }

    @Transactional
    public Pedido procesarPedido(Pedido nuevoPedido) {
        // 🚨 CRÍTICO: Recorremos los detalles que llegaron del JSON y les asignamos su
        // Pedido padre
        if (nuevoPedido.getDetalles() != null) {
            for (PedidoDetalle detalle : nuevoPedido.getDetalles()) {
                detalle.setPedido(nuevoPedido); // Vinculación bidireccional en memoria
            }
        }

        // Hibernate guardará el Pedido, obtendrá el ID de MySQL y se lo heredará a los
        // detalles automáticamente
        return pedidoRepository.save(nuevoPedido);
    }

    @Transactional(readOnly = true) // 🚨 Mantiene la sesión abierta de forma eficiente para la lectura
    public List<Pedido> obtenerPedidosPorUsuario(Integer idUsuario) {
        return pedidoRepository.findByIdUsuarioOrderByIdDesc(idUsuario);
    }

    @Transactional
    public void eliminarPedido(Long id) {
        // Al tener CascadeType.ALL o ORPHAN_REMOVAL, borrar el padre limpia
        // automáticamente los detalles en MySQL
        pedidoRepository.deleteById(id);
    }

    @Transactional
    public Pedido actualizarNotas(Long id, Pedido dtoConNuevasNotas) { // 🚨 Corregido: tipo y nombre de variable
                                                                       // limpios
        Pedido pedidoExistente = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        // Solo sobreescribimos la columna de texto de las instrucciones
        pedidoExistente.setInstruccionesEspeciales(dtoConNuevasNotas.getInstruccionesEspeciales());

        return pedidoRepository.save(pedidoExistente);
    }

    @Transactional
    public void cancelarPedido(Long id) {
        // 1. Buscamos la comanda por su ID en la base de datos
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la comanda con el ID: " + id));

        // 2. Modificamos el atributo de estatus para que coincida con el frontend
        // (PedidoDTO)
        pedido.setEstatus("CANCELADO");

        // 3. Sincronizamos el cambio ejecutando un UPDATE implícito a través de
        // Hibernate
        pedidoRepository.save(pedido);

        // 4. Notificamos a la aplicación móvil sobre el cambio
        if (pedidoWebSocketHandler != null) {
            pedidoWebSocketHandler.notificarCambioEstatus(id, "CANCELADO");
        }
    }

    @Transactional
    public Pedido actualizarEstatus(Long id, String nuevoEstatus) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstatus(nuevoEstatus);
        pedido.setStatus(nuevoEstatus); // Sincronización de campos redundantes
        Pedido actualizado = pedidoRepository.save(pedido);

        // Notificamos a la aplicación móvil sobre el cambio en el Tracker
        if (pedidoWebSocketHandler != null) {
            pedidoWebSocketHandler.notificarCambioEstatus(id, nuevoEstatus);
        }
        return actualizado;
    }

    @Transactional
    public Pedido actualizarUbicacion(Long id, Double lat, Double lng) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setLatitud(lat);
        pedido.setLongitud(lng);
        Pedido actualizado = pedidoRepository.save(pedido);

        // Notificamos por WebSocket a los interesados (ej: el cliente que rastrea)
        if (pedidoWebSocketHandler != null) {
            String payload = String.format(
                "{\"type\": \"LOCATION_UPDATE\", \"idPedido\": \"%d\", \"lat\": %f, \"lng\": %f}",
                id, lat, lng
            );
            broadcastLocation(id.toString(), payload);
        }

        return actualizado;
    }

    private void broadcastLocation(String idPedido, String payload) {
        // Podríamos llamar a un método en el handler que haga el broadcast
        try {
            // Usamos un método bridge en el handler si es necesario, o lo hacemos público
            pedidoWebSocketHandler.broadcastManual(idPedido, payload);
        } catch (Exception e) {
            System.err.println("Error en broadcast de ubicación: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerPedidosActivos() {
        return pedidoRepository.findByEstatusInOrderByIdDesc(Arrays.asList("RECIBIDO", "EN COCINA", "EN CAMINO"));
    }
}
