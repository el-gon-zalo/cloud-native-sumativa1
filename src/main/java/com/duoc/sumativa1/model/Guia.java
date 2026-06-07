package com.duoc.sumativa1.model;

import java.time.LocalDateTime;

public class Guia {

    private String id;
    private String transportista;
    private String fecha;
    private String destinatario;
    private String rutaEFS;
    private String rutaS3;
    private EstadoGuia estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public enum EstadoGuia {
        CREADA,
        SUBIDA_EFS,
        SUBIDA_S3,
        DESCARGADA,
        MODIFICADA,
        ELIMINADA
    }

    // Constructor vacío
    public Guia() {}

    // Constructor lleno
    public Guia(String id, String transportista, String fecha, String destinatario,
                String rutaEFS, String rutaS3, EstadoGuia estado,
                LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.transportista = transportista;
        this.fecha = fecha;
        this.destinatario = destinatario;
        this.rutaEFS = rutaEFS;
        this.rutaS3 = rutaS3;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

   
    public String getId() { return id; }
    public String getTransportista() { return transportista; }
    public String getFecha() { return fecha; }
    public String getDestinatario() { return destinatario; }
    public String getRutaEFS() { return rutaEFS; }
    public String getRutaS3() { return rutaS3; }
    public EstadoGuia getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }

    public void setId(String id) { this.id = id; }
    public void setTransportista(String transportista) { this.transportista = transportista; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public void setRutaEFS(String rutaEFS) { this.rutaEFS = rutaEFS; }
    public void setRutaS3(String rutaS3) { this.rutaS3 = rutaS3; }
    public void setEstado(EstadoGuia estado) { this.estado = estado; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    // toString
    @Override
    public String toString() {
        return "Guia{" +
                "id='" + id + '\'' +
                ", transportista='" + transportista + '\'' +
                ", fecha='" + fecha + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", rutaEFS='" + rutaEFS + '\'' +
                ", rutaS3='" + rutaS3 + '\'' +
                ", estado=" + estado +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }
}