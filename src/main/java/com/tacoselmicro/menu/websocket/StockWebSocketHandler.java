package com.tacoselmicro.menu.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StockWebSocketHandler extends TextWebSocketHandler {

    // Almacena todas las sesiones activas (clientes interesados en actualizaciones de stock)
    private final Set<WebSocketSession> sesiones = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sesiones.add(session);
        System.out.println("[WebSocket Stock] Cliente conectado para actualizaciones de inventario");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sesiones.remove(session);
    }

    public void notificarCambioDisponibilidad(Long idProducto, boolean disponible) {
        String payload = String.format("{\"idProducto\": %d, \"disponible\": %b}", idProducto, disponible);
        TextMessage message = new TextMessage(payload);

        for (WebSocketSession session : sesiones) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
