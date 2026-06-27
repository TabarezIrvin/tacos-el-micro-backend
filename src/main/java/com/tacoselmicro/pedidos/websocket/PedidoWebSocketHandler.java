package com.tacoselmicro.pedidos.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tacoselmicro.pedidos.entity.Pedido;
import com.tacoselmicro.pedidos.repository.PedidoRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class PedidoWebSocketHandler extends TextWebSocketHandler {

    // Almacena las sesiones activas vinculadas por el ID del pedido (Múltiples sesiones por pedido)
    private final Map<String, Set<WebSocketSession>> sesiones = new ConcurrentHashMap<>();
    
    @Autowired
    private PedidoRepository pedidoRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        String idPedido = uri.substring(uri.lastIndexOf('/') + 1);
        sesiones.computeIfAbsent(idPedido, k -> new CopyOnWriteArraySet<>()).add(session);
        System.out.println("[WebSocket] Cliente conectado al tracker para el pedido #" + idPedido + ". Total sesiones: " + sesiones.get(idPedido).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String uri = session.getUri().toString();
        String idPedido = uri.substring(uri.lastIndexOf('/') + 1);
        Set<WebSocketSession> sessions = sesiones.get(idPedido);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sesiones.remove(idPedido);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            if ("LOCATION_UPDATE".equals(data.get("type"))) {
                String idPedido = (String) data.get("idPedido");
                Double lat = ((Number) data.get("lat")).doubleValue();
                Double lng = ((Number) data.get("lng")).doubleValue();
                
                // 1. Guardar en DB
                pedidoRepository.findById(Long.parseLong(idPedido)).ifPresent(pedido -> {
                    pedido.setLatitud(lat);
                    pedido.setLongitud(lng);
                    pedidoRepository.save(pedido);
                });
                
                // 2. Broadcast a todos los interesados en este pedido
                broadcast(idPedido, payload);
            }
        } catch (Exception e) {
            System.err.println("Error procesando mensaje WS: " + e.getMessage());
        }
    }

    public void notificarCambioEstatus(Long idPedido, String nuevoEstatus) {
        String payload = String.format("{\"estatus\": \"%s\"}", nuevoEstatus);
        broadcast(idPedido.toString(), payload);
    }

    /**
     * Permite enviar mensajes arbitrarios desde servicios externos al tracker
     */
    public void broadcastManual(String idPedido, String payload) {
        broadcast(idPedido, payload);
    }
    
    private void broadcast(String idPedido, String payload) {
        Set<WebSocketSession> sessions = sesiones.get(idPedido);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
