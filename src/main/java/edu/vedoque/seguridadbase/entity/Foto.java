package edu.vedoque.seguridadbase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fotos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreArchivo;

    private int likes = 0;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @OneToMany(mappedBy = "foto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LikeFoto> likesRecibidos = new HashSet<>();

    public boolean isLikedBy(User u) {
        if (u == null) return false;
        return likesRecibidos.stream()
                .anyMatch(like -> like.getUsuario().getId().equals(u.getId()));
    }

    public Set<LikeFoto> getLikesRecibidos() { return likesRecibidos; }
    public void setLikesRecibidos(Set<LikeFoto> likesRecibidos) { this.likesRecibidos = likesRecibidos; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public User getUsuario() { return usuario; }
    public void setUsuario(User usuario) { this.usuario = usuario; }

}