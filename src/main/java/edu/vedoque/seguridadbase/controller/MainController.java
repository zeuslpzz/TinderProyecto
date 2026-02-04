package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.entity.Foto;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.repository.MatchActionRepository;
import edu.vedoque.seguridadbase.repository.UserRepository;
import edu.vedoque.seguridadbase.service.FileProcessingService;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import edu.vedoque.seguridadbase.entity.Role;
import edu.vedoque.seguridadbase.entity.UserRole;
import edu.vedoque.seguridadbase.repository.RoleRepository;
import edu.vedoque.seguridadbase.repository.UserRoleRepository;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

// Este es el controlador principal que maneja la navegación pública y el perfil del usuario
@Controller
public class MainController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchActionRepository matchActionRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FileProcessingService fileProcessingService;

    // Defino esta lista fija aquí para usarla luego en los desplegables de filtros
    private final List<String> LISTA_INTERESES = Arrays.asList(
            "Deportista", "Cinéfilo", "Gamer", "Viajero", "Músico",
            "Lector", "Aventurero", "Tranquilo"
    );

    // Este método carga la página de inicio y se encarga de buscar un candidato adecuado para mostrar
    @GetMapping("/")
    public String index(@RequestParam(value="filtro", required=false) String filtro, Model model, Authentication auth) {

        User logueado = null;
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            logueado = userService.findByEmail(auth.getName());
        }

        List<User> todos = userRepository.findAll();

        // Busco a los administradores para excluirlos de la lista de gente que se puede conocer
        Role rolAdmin = roleRepository.findByName("ROLE_ADMIN");
        List<UserRole> relacionesAdmin = userRoleRepository.findByRole(rolAdmin);
        List<Long> idsAdmins = relacionesAdmin.stream()
                .map(r -> r.getUser().getId())
                .collect(Collectors.toList());

        User candidato = null;

        // Si estoy logueado filtro la lista para no mostrarme a mi mismo ni a los admins ni a la gente con la que ya he interactuado
        if (logueado != null) {
            User finalLogueado = logueado;
            candidato = todos.stream()
                    .filter(u -> !u.getId().equals(finalLogueado.getId()))
                    .filter(u -> !idsAdmins.contains(u.getId()))
                    .filter(u -> !u.getEmail().equals("admin@admin.com"))
                    .filter(u -> matchActionRepository.findByEmisorAndReceptor(finalLogueado, u).isEmpty())
                    .filter(u -> filtro == null || filtro.isEmpty() || (u.getInteres() != null && u.getInteres().equals(filtro)))
                    .findFirst()
                    .orElse(null);
        } else {
            // Si no estoy logueado muestro un usuario aleatorio que no sea admin para invitar al registro
            Collections.shuffle(todos);
            candidato = todos.stream()
                    .filter(u -> !idsAdmins.contains(u.getId()))
                    .filter(u -> !u.getEmail().equals("admin@admin.com"))
                    .filter(u -> filtro == null || filtro.isEmpty() || (u.getInteres() != null && u.getInteres().equals(filtro)))
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("usuarioCandidato", candidato);
        model.addAttribute("listaIntereses", LISTA_INTERESES);
        model.addAttribute("filtroActivo", filtro);

        return "index";
    }

    // Muestro la página de edición de perfil cargando los datos del usuario actual
    @GetMapping("/perfil")
    public String perfil(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("listaIntereses", LISTA_INTERESES);
        return "perfil";
    }

    // Recibo los datos del formulario de perfil incluyendo la foto de avatar y las fotos de la galería
    @PostMapping("/subir/fotos")
    public String subirFotos(@RequestParam("avatarFile") MultipartFile avatarFile,
                             @RequestParam("galeriaFiles") MultipartFile[] galeriaFiles,
                             @RequestParam("biografia") String biografia,
                             @RequestParam("interes") String interes,
                             @RequestParam(value = "edad", required = false) Integer edad,
                             @RequestParam(value = "ubicacion", required = false) String ubicacion,
                             Authentication auth) {

        User user = userService.findByEmail(auth.getName());

        user.setBiografia(biografia);
        user.setInteres(interes);
        user.setEdad(edad);
        user.setUbicacion(ubicacion);

        // Si han subido un avatar nuevo lo guardo usando el servicio de ficheros y actualizo el nombre en el usuario
        if (!avatarFile.isEmpty()) {
            String imgName = "avatar-" + user.getId() + "-" + System.currentTimeMillis() + ".png";
            fileProcessingService.uploadFile(avatarFile, imgName);
            user.setAvatar(imgName);
        }

        // Recorro todas las fotos de la galería que hayan subido y creo un objeto Foto para cada una
        if (galeriaFiles != null && galeriaFiles.length > 0) {
            for (MultipartFile fichero : galeriaFiles) {
                if (!fichero.isEmpty()) {
                    String fileName = "galeria-" + user.getId() + "-" + System.currentTimeMillis() + ".png";
                    fileProcessingService.uploadFile(fichero, fileName);

                    // Creo la entidad foto y la relaciono con el usuario
                    Foto nuevaFoto = new Foto();
                    nuevaFoto.setNombreArchivo(fileName);
                    nuevaFoto.setUsuario(user);
                    nuevaFoto.setLikes(0);

                    // La añado a la lista de fotos del usuario
                    user.getFotosGaleria().add(nuevaFoto);
                }
            }
        }

        userService.save(user);
        return "redirect:/perfil?success";
    }

    // Elimino una foto concreta de la galería del usuario buscando por su id
    @PostMapping("/perfil/eliminar-foto")
    public String eliminarFoto(@RequestParam("idFoto") Long idFoto, Authentication auth) {
        User user = userService.findByEmail(auth.getName());

        // Uso una expresión lambda para borrar la foto de la lista si coincide el id
        user.getFotosGaleria().removeIf(f -> f.getId().equals(idFoto));

        userService.save(user);
        return "redirect:/perfil";
    }

    // Muestro el perfil público de otro usuario para que se pueda ver su detalle antes de dar like o dislike
    @GetMapping("/usuario/{id}")
    public String verUsuarioPublico(@PathVariable("id") Long id, Model model, Authentication auth) {
        User logueado = userService.findByEmail(auth.getName());
        User usuario = userService.findById(id);

        // Si intento ver mi propio perfil público me redirijo a mi edición de perfil
        if (logueado.getId().equals(usuario.getId())) {
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario);
        // Paso mi usuario a la vista para poder comprobar si ya he dado like a alguna foto
        model.addAttribute("miUsuario", logueado);

        // Compruebo si hay match mutuo o si ya le he dado like para pintar los botones de forma diferente
        boolean hayMatch = matchActionRepository.countLikesRecibidos(logueado, usuario) > 0 &&
                matchActionRepository.countLikesRecibidos(usuario, logueado) > 0;

        boolean yaLeDiLike = matchActionRepository.findByEmisorAndReceptor(logueado, usuario)
                .map(match -> match.isLeGusta())
                .orElse(false);

        String estado = "NADA";
        if (hayMatch) {
            estado = "MATCH";
        } else if (yaLeDiLike) {
            estado = "LIKED";
        }

        model.addAttribute("estado", estado);
        return "usuario";
    }
}