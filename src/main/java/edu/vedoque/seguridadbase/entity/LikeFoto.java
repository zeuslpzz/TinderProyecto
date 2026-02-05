package edu.vedoque.seguridadbase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "likes_fotos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "foto_id")
    private Foto foto;

    public LikeFoto(User usuario, Foto foto) {
        this.usuario = usuario;
        this.foto = foto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public Foto getFoto() {
        return foto;
    }

    public void setFoto(Foto foto) {
        this.foto = foto;
    }

    public LikeFoto() {
    }
}