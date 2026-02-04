package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.entity.MatchAction;
import edu.vedoque.seguridadbase.entity.Mensaje;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.repository.MatchActionRepository;
import edu.vedoque.seguridadbase.repository.MensajeRepository;
import edu.vedoque.seguridadbase.repository.UserRepository;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Este controlador gestiona toda la lógica principal de la app como son los likes, dislikes y el cálculo de matches
@Controller
@RequestMapping("/matches")
public class MatchesController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchActionRepository matchActionRepository;
    @Autowired
    private MensajeRepository mensajeRepository;

    // Este método se ejecuta cuando le damos al botón del corazón verde
    @PostMapping("/like/{id}")
    public String darLike(@PathVariable("id") Long id, Authentication auth) {
        User logueado = userService.findByEmail(auth.getName());
        User objetivo = userRepository.findById(id).orElse(null);

        // Me aseguro de que el usuario existe y de no darme like a mí mismo para evitar errores
        if (objetivo != null && !logueado.getId().equals(objetivo.getId())) {
            Optional<MatchAction> existente = matchActionRepository.findByEmisorAndReceptor(logueado, objetivo);

            // Si es la primera vez que interactúo creo un registro nuevo pero si ya existía lo actualizo a true
            if (existente.isEmpty()) {
                MatchAction accion = new MatchAction();
                accion.setEmisor(logueado);
                accion.setReceptor(objetivo);
                accion.setLeGusta(true);
                matchActionRepository.save(accion);
            } else {
                MatchAction accion = existente.get();
                accion.setLeGusta(true);
                matchActionRepository.save(accion);
            }

            // Compruebo si el otro usuario también me ha dado like a mí para saber si hay match y mostrar la alerta
            boolean esMatch = matchActionRepository.countLikesRecibidos(logueado, objetivo) > 0;
            if (esMatch) {
                return "redirect:/?match=true";
            }
        }
        return "redirect:/";
    }

    // Funciona igual que el like pero guardando false en la base de datos para indicar que no me interesa
    @PostMapping("/dislike/{id}")
    public String darDislike(@PathVariable("id") Long id, Authentication auth) {
        User logueado = userService.findByEmail(auth.getName());
        User objetivo = userRepository.findById(id).orElse(null);

        if (objetivo != null) {
            Optional<MatchAction> existente = matchActionRepository.findByEmisorAndReceptor(logueado, objetivo);

            if (existente.isEmpty()) {
                MatchAction accion = new MatchAction();
                accion.setEmisor(logueado);
                accion.setReceptor(objetivo);
                accion.setLeGusta(false);
                matchActionRepository.save(accion);
            } else {
                MatchAction accion = existente.get();
                accion.setLeGusta(false);
                matchActionRepository.save(accion);
            }
        }
        return "redirect:/";
    }

    // Deshacer los match y cualquier relacion
    @PostMapping("/unmatch/{id}")
    public String deshacerMatch(@PathVariable("id") Long idOtroUsuario, Authentication auth) {
        User yo = userService.findByEmail(auth.getName());
        User elOtro = userRepository.findById(idOtroUsuario).orElse(null);

        if (elOtro != null) {
            // Borro mi like hacia él
            Optional<MatchAction> miLike = matchActionRepository.findByEmisorAndReceptor(yo, elOtro);
            miLike.ifPresent(matchActionRepository::delete);

            // Borro su like hacia mí para que la ruptura sea total
            Optional<MatchAction> suLike = matchActionRepository.findByEmisorAndReceptor(elOtro, yo);
            suLike.ifPresent(matchActionRepository::delete);

            // Elimino todo el historial de chat para no dejar basura en la base de datos ya que no se van a poder hablar más
            List<Mensaje> historial = mensajeRepository.findByRemitenteAndDestinatario(yo, elOtro);
            mensajeRepository.deleteAll(historial);
        }
        return "redirect:/matches/mis-matches";
    }

    // Muestra la lista de gente con la que he conectado verificando que el like sea mutuo
    @GetMapping("/mis-matches")
    public String verMisMatches(Model model, Authentication auth) {
        User logueado = userService.findByEmail(auth.getName());
        List<User> todosLosUsuarios = userRepository.findAll();
        List<User> misMatches = new ArrayList<>();

        // Recorro todos los usuarios y compruebo si hay like en las dos direcciones
        for (User u : todosLosUsuarios) {
            if (u.getId().equals(logueado.getId())) continue;

            long leGusto = matchActionRepository.countLikesRecibidos(logueado, u);
            long meGusta = matchActionRepository.countLikesRecibidos(u, logueado);

            // Si los dos contadores son positivos significa que es un match real y lo añado a la lista
            if (leGusto > 0 && meGusta > 0) {
                misMatches.add(u);
            }
        }

        model.addAttribute("listaMatches", misMatches);
        return "matches";
    }
}