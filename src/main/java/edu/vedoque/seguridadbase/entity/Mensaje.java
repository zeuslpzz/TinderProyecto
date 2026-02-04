package edu.vedoque.seguridadbase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenido;

    private LocalDateTime fechaHora;

    // Relación con el que envía
    @ManyToOne
    @JoinColumn(name = "remitente_id")
    private User remitente;

    // Relación con el que recibe
    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private User destinatario;

    private boolean leido = false;

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public User getRemitente() { return remitente; }
    public void setRemitente(User remitente) { this.remitente = remitente; }

    public User getDestinatario() { return destinatario; }
    public void setDestinatario(User destinatario) { this.destinatario = destinatario; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}