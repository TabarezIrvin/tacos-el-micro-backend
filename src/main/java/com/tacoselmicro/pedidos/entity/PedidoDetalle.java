package com.tacoselmicro.pedidos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "pedido_detalle") // 🚨 Tip Pro: Usa minúsculas en MySQL para evitar problemas de portabilidad (Case Sensitivity en Linux)
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor // Usar anotaciones granulares es más seguro que @Data en entidades JPA para evitar bucles en el toString()
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "tamaño", length = 20)
    private String tamaño = "Normal";

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    // Columna calculada (VIRTUAL) en MySQL protegida de los inserts de Hibernate
    @Column(name = "subtotal", insertable = false, updatable = false)
    private BigDecimal subtotal;
    
    @ManyToOne(fetch = FetchType.LAZY) // 🚨 Fetch LAZY por rendimiento, el JSON no tronará gracias a @JsonBackReference
    @JoinColumn(name = "id_pedido", nullable = false) // 🚨 nullable = false asegura la integridad de la FK antes del insert
    @JsonBackReference 
    private Pedido pedido;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(Long idProducto) {
		this.idProducto = idProducto;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public String getTamaño() {
		return tamaño;
	}

	public void setTamaño(String tamaño) {
		this.tamaño = tamaño;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(BigDecimal precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
    
    
}