package com.tacoselmicro.pedidos.entity;

import jakarta.persistence.*; // o javax.persistence.* si usas Spring Boot 2.x
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "pedidos")
public class Pedido implements Serializable {
	
	@Column(name = "estatus_pedido", length = 20, nullable = false) // 🚨 AQUÍ: Le dices a Hibernate el nombre real en MySQL
	private String status = "EN COCINA";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_pedido") // 🚨 LA SOLUCIÓN: Cambia "id_pedido" por el nombre real de tu columna en MySQL
	private Long id;

    // 🚨 REVISA ESTA SECCIÓN: El nombre del atributo aquí define la clave del JSON
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario; 

    @Column(name = "total_pagar", nullable = false)
    private Double totalPagar;

    @Column(name = "instrucciones_especiales")
    private String instruccionesEspeciales;
    
    @Column(name = "estatus", length = 30)
    private String estatus; // 🚨 El nombre de esta variable debe ser idéntico al del método del repositorio
    
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
 // 🚨 'mappedBy = "pedido"' le dice a Hibernate: 
 // "La variable llamada 'pedido' dentro de PedidoDetalle ya se encarga de la columna id_pedido, no la mapees tú otra vez aquí".
 private List<PedidoDetalle> detalles;

    // Métodos Getter y Setter (¡Obligatorios para que Jackson pueda inyectar el valor!)
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getTotalPagar() {
		return totalPagar;
	}

	public void setTotalPagar(Double totalPagar) {
		this.totalPagar = totalPagar;
	}

	public String getInstruccionesEspeciales() {
		return instruccionesEspeciales;
	}

	public void setInstruccionesEspeciales(String instruccionesEspeciales) {
		this.instruccionesEspeciales = instruccionesEspeciales;
	}

	public List<PedidoDetalle> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<PedidoDetalle> detalles) {
		this.detalles = detalles;
	}
	
	public String getEstatus() {
	    return estatus;
	}

	public void setEstatus(String estatus) {
	    this.estatus = estatus;
	}
	
	public LocalDateTime getFechaPedido() {
	    return fechaPedido;
	}

	public void setFechaPedido(LocalDateTime fechaPedido) {
	    this.fechaPedido = fechaPedido;
	}

	public Double getLatitud() {
		return latitud;
	}

	public void setLatitud(Double latitud) {
		this.latitud = latitud;
	}

	public Double getLongitud() {
		return longitud;
	}

	public void setLongitud(Double longitud) {
		this.longitud = longitud;
	}

	public String getStatus() {
	    return this.status;
	}

	public void setStatus(String status) {
	    this.status = status;
	}
	
    
   
}