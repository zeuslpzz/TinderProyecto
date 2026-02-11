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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/chat")
public class ChatController {

    // Necesito estos servicios y repositorios para gestionar usuarios y guardar los mensajes en la base de datos
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MensajeRepository mensajeRepository;
    @Autowired
    private MatchActionRepository matchActionRepository;

    //Ver el historial de mensajes (chat) con la persona
    @GetMapping("/{id}")
    public String verChat(@PathVariable("id") Long idUsuarioDestino, Model model, Authentication auth) {
        // Saco mi usuario de la sesión y busco al usuario con el que quiero hablar por su id
        User miUsuario = userService.findByEmail(auth.getName());
        User otroUsuario = userRepository.findById(idUsuarioDestino).orElse(null);

        // Si el usuario no existe me vuelvo a la lista de matches para evitar errores
        if (otroUsuario == null) {
            return "redirect:/matches/mis-matches";
        }

        // Uso una consulta del repositorio que me devuelve los mensajes de los dos ordenados por fecha
        List<Mensaje> mensajes = mensajeRepository.findByRemitenteAndDestinatario(miUsuario, otroUsuario);

        // Paso los datos a la vista para poder pintar los globos de chat a la izquierda o derecha
        model.addAttribute("miUsuario", miUsuario);
        model.addAttribute("otroUsuario", otroUsuario);
        model.addAttribute("mensajes", mensajes);

        return "chat";
    }

    // Aquí recojo el texto que envía el usuario y lo guardo
    @PostMapping("/enviar")
    public String enviarMensaje(@RequestParam("idDestino") Long idDestino,
                                @RequestParam("texto") String texto,
                                Authentication auth) {
        User miUsuario = userService.findByEmail(auth.getName());
        User otroUsuario = userRepository.findById(idDestino).orElse(null);

        // Solo guardo el mensaje si el texto no está vacío para no llenar la base de datos de mensajes en blanco
        if (otroUsuario != null && !texto.trim().isEmpty()) {
            Mensaje mensaje = new Mensaje();
            mensaje.setRemitente(miUsuario);
            mensaje.setDestinatario(otroUsuario);

            // Asigno el contenido y la fecha actual antes de guardar
            mensaje.setContenido(texto);

            mensaje.setFechaHora(LocalDateTime.now());
            mensajeRepository.save(mensaje);
        }

        // Recargo la misma página para que aparezca el mensaje nuevo que acabo de enviar
        return "redirect:/chat/" + idDestino;
    }

    //Deshacer match con alguien desde el chat
    @PostMapping("/unmatch/{id}")
    public String deshacerMatch(@PathVariable("id") Long idOtroUsuario, Authentication auth) {
        User yo = userService.findByEmail(auth.getName());
        User elOtro = userRepository.findById(idOtroUsuario).orElse(null);

        if (elOtro != null) {
            // Borro los likes de los dos lados para romper la relación
            Optional<MatchAction> miLike = matchActionRepository.findByEmisorAndReceptor(yo, elOtro);
            miLike.ifPresent(matchActionRepository::delete);

            Optional<MatchAction> suLike = matchActionRepository.findByEmisorAndReceptor(elOtro, yo);
            suLike.ifPresent(matchActionRepository::delete);

            // Importante borrar también los mensajes para limpiar la base de datos y que no queden huerfanos
            List<Mensaje> historial = mensajeRepository.findByRemitenteAndDestinatario(yo, elOtro);
            mensajeRepository.deleteAll(historial);
        }
        return "redirect:/matches/mis-matches";
    }
}