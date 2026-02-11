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
                // Quitar like
                foto.getLikesRecibidos().remove(likeExistente.get());
                foto.setLikes(Math.max(0, foto.getLikes() - 1));
            } else {
                //Dar like
                LikeFoto nuevoLike = new LikeFoto(usuarioLogueado, foto);
                foto.getLikesRecibidos().add(nuevoLike);
                foto.setLikes(foto.getLikes() + 1);
            }
            fotoRepository.save(foto);
        }
        //Vuelves a la misma pagina donde estas
        return "redirect:" + (referer != null ? referer : "/");
    }
}