package edu.vedoque.seguridadbase.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Esta es la entidad clave para la lógica de la app porque aquí es donde guardo cada vez que alguien le da al corazón o a la cruz
@Entity
@Table(name = "match_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardo quién es el usuario que está realizando la acción de dar like o dislike
    @ManyToOne
    @JoinColumn(name = "emisor_id")
    private User emisor;

    // Guardo a quién va dirigida esa acción para saber si luego me devuelve el like
    @ManyToOne
    @JoinColumn(name = "receptor_id")
    private User receptor;

    private boolean leGusta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmisor() {
        return emisor;
    }

    public void setEmisor(User emisor) {
        this.emisor = emisor;
    }

    public User getReceptor() {
        return receptor;
    }

    public void setReceptor(User receptor) {
        this.receptor = receptor;
    }

    public boolean isLeGusta() {
        return leGusta;
    }

    public void setLeGusta(boolean leGusta) {
        this.leGusta = leGusta;
    }
}