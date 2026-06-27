package com.tacoselmicro.pedidos.controller;


import com.tacoselmicro.pedidos.entity.Pedido;
import com.tacoselmicro.pedidos.service.PedidoService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        return ResponseEntity.ok(pedidoService.getStats());
    }

    @PostMapping
    public ResponseEntity<Pedido> enviarPedidoACocina(@RequestBody Pedido nuevoPedido) {
        Pedido procesado = pedidoService.procesarPedido(nuevoPedido);
        return new ResponseEntity<>(procesado, HttpStatus.CREATED);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Pedido>> obtenerHistorial(@PathVariable Integer idUsuario) {
        List<Pedido> historial = pedidoService.obtenerPedidosPorUsuario(idUsuario);

        if (historial.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(historial, HttpStatus.OK);
    }

    @GetMapping("/cocina")
    public ResponseEntity<List<Pedido>> obtenerPedidosCocina() {
        List<Pedido> pedidos = pedidoService.obtenerPedidosActivos();
        return ResponseEntity.ok(pedidos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build(); // Retorna un estatus 204
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> actualizarEstatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String nuevoEstatus = body.get("status");
        Pedido actualizado = pedidoService.actualizarEstatus(id, nuevoEstatus);
        return ResponseEntity.ok(actualizado);
    }

    @PatchMapping("/{id}/ubicacion")
    public ResponseEntity<Pedido> actualizarUbicacion(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        Double lat = body.get("lat");
        Double lng = body.get("lng");
        Pedido actualizado = pedidoService.actualizarUbicacion(id, lat, lng);
        return ResponseEntity.ok(actualizado);
    }
}