package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.entity.Foto;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.repository.FotoRepository;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Importante
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

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

            // Compruebo si ya le dio like
            if (foto.isLikedBy(usuarioLogueado)) {
                // Si ya tiene like quito el like
                // Buscamos el objeto usuario dentro del set para borrarlo correctamente
                foto.getUsuariosLikes().removeIf(u -> u.getId().equals(usuarioLogueado.getId()));
                foto.setLikes(foto.getLikes() - 1); // Bajamos contador
            } else {
                foto.getUsuariosLikes().add(usuarioLogueado);
                foto.setLikes(foto.getLikes() + 1); // Subimos contador
            }

            // Guardamos cambios
            fotoRepository.save(foto);
        }

        return "redirect:" + (referer != null ? referer : "/");
    }
}