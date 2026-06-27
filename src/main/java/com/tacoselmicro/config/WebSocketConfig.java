package com.tacoselmicro.config;

import com.tacoselmicro.pedidos.websocket.PedidoWebSocketHandler;
import com.tacoselmicro.menu.websocket.StockWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private PedidoWebSocketHandler pedidoWebSocketHandler;

    @Autowired
    private StockWebSocketHandler stockWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Tracker de Pedido Individual
        registry.addHandler(pedidoWebSocketHandler, "/ws/pedidos/{id}")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");

        // Actualizaciones de Stock en Tiempo Real
        registry.addHandler(stockWebSocketHandler, "/ws/stock")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
