package org.example.notificaciones.modelo;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "notificaciones")
public class Notificacion {

    @Id
    private String id;
    private String usuario;
    private String mensaje;
    private String tipo; // INFO, ALERTA, URGENTE
    private Date fecha = new Date();
    private boolean leido = false;

    // Constructor vacío
    public Notificacion() {}

    // Constructor para creación
    public Notificacion(String usuario, String mensaje, String tipo) {
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.tipo = tipo;
    }

    // --- Getters y Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
