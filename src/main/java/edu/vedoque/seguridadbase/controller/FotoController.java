package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.entity.Foto;
import edu.vedoque.seguridadbase.entity.LikeFoto;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.repository.FotoRepository;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/foto")
public class FotoController {

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/like/{id}")
    public String toggleLikeFoto(@PathVariable("id") Long id,
                                 @RequestHeader(value = "referer", required = false) String referer,
                                 Authentication auth) {

        // Buscamos la foto
        Foto foto = fotoRepository.findById(id).orElse(null);
        // Buscamos quién eres tú
        User usuarioLogueado = userService.findByEmail(auth.getName());

        if (foto != null && usuarioLogueado != null) {

            // Buscamos en la lista si ya existe el like
            Optional<LikeFoto> likeExistente = foto.getLikesRecibidos().stream()
                    .filter(like -> like.getUsuario().getId().equals(usuarioLogueado.getId()))
                    .findFirst();

            if (likeExistente.isPresent()) {
                // SI YA TIENE LIKE: Lo quitamos
                foto.getLikesRecibidos().remove(likeExistente.get());
                foto.setLikes(Math.max(0, foto.getLikes() - 1));
            } else {
                // SI NO TIENE LIKE: Creamos el nuevo like
                // CORRECCIÓN: Usamos el constructor con parámetros que ya tienes definido
                LikeFoto nuevoLike = new LikeFoto(usuarioLogueado, foto);

                // Lo añadimos a la lista
                foto.getLikesRecibidos().add(nuevoLike);
                foto.setLikes(foto.getLikes() + 1);
            }

            // Guardamos la foto (y por cascada se actualizan los likes)
            fotoRepository.save(foto);
        }

        return "redirect:" + (referer != null ? referer : "/");
    }
}