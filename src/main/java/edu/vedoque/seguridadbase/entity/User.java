package edu.vedoque.seguridadbase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String avatar;

    private Integer edad;

    private String ubicacion;

    @Column(columnDefinition = "TEXT")
    private String biografia;

    private String interes;

    // Relación con sus propias fotos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Foto> fotosGaleria = new ArrayList<>();

    // Al usar orphanRemoval se borran sus likes automáticamente
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LikeFoto> likesDados = new HashSet<>();

    // Relación con MatchActions
    @OneToMany(mappedBy = "emisor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchAction> accionesEnviadas = new ArrayList<>();

    @OneToMany(mappedBy = "receptor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchAction> accionesRecibidas = new ArrayList<>();

    // Relación con Mensajes
    @OneToMany(mappedBy = "remitente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajesEnviados = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajesRecibidos = new ArrayList<>();

    // Relación con Roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public String getInteres() { return interes; }
    public void setInteres(String interes) { this.interes = interes; }

    public List<Foto> getFotosGaleria() { return fotosGaleria; }
    public void setFotosGaleria(List<Foto> fotosGaleria) { this.fotosGaleria = fotosGaleria; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public List<Mensaje> getMensajesEnviados() { return mensajesEnviados; }
    public void setMensajesEnviados(List<Mensaje> mensajesEnviados) { this.mensajesEnviados = mensajesEnviados; }

    public List<Mensaje> getMensajesRecibidos() { return mensajesRecibidos; }
    public void setMensajesRecibidos(List<Mensaje> mensajesRecibidos) { this.mensajesRecibidos = mensajesRecibidos; }

    public Set<LikeFoto> getLikesDados() { return likesDados; }
    public void setLikesDados(Set<LikeFoto> likesDados) { this.likesDados = likesDados; }
}