package edu.vedoque.seguridadbase.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class UserDto {
    private Long id;
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String name;

    @NotEmpty(message = "El email no puede estar vacío")
    @Email
    private String email;

    private String password;
    private String biografia;
    private String genero;
    private String preferencia;
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPreferencia() {
        return preferencia;
    }

    public void setPreferencia(String preferencia) {
        this.preferencia = preferencia;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public @NotEmpty(message = "El email no puede estar vacío") @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotEmpty(message = "El email no puede estar vacío") @Email String email) {
        this.email = email;
    }

    public @NotEmpty(message = "El nombre no puede estar vacío") String getName() {
        return name;
    }

    public void setName(@NotEmpty(message = "El nombre no puede estar vacío") String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}