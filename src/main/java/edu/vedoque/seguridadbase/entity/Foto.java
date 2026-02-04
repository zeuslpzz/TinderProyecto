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

    // Relación de muchos a uno: Muchas fotos pertenecen a un solo usuario (el dueño)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    // Relación de muchos a muchos: Una foto puede gustarle a muchos usuarios, y un usuario puede dar like a muchas fotos
    // Uso FetchType.EAGER para que al cargar la foto se carguen también los likes inmediatamente y no me de error en la vista
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "foto_likes",
            joinColumns = @JoinColumn(name = "foto_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    // Uso un Set conjunto en vez de una List porque no quiero permitir duplicados, un usuario solo puede dar like una vez
    private Set<User> usuariosLikes = new HashSet<>();

    public boolean isLikedBy(User u) {
        if (u == null) return false;
        // Comparo por ID para asegurarme de que es el mismo usuario aunque sean objetos diferentes en memoria
        return usuariosLikes.stream().anyMatch(user -> user.getId().equals(u.getId()));
    }

    public Set<User> getUsuariosLikes() {
        return usuariosLikes;
    }

    public void setUsuariosLikes(Set<User> usuariosLikes) {
        this.usuariosLikes = usuariosLikes;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}