package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.entity.Foto;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.repository.MatchActionRepository;
import edu.vedoque.seguridadbase.repository.UserRepository;
import edu.vedoque.seguridadbase.service.FileProcessingService;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

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
    private JavaMailSender emailSender;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FileProcessingService fileProcessingService;


    // Lista base por defecto
    private final List<String> INTERESES_BASE = Arrays.asList(
            "Deportista", "Cinéfilo", "Gamer", "Viajero", "Músico",
            "Lector", "Aventurero", "Tranquilo"
    );

    //Metodo para obtener todas las etiquetas de la bd actualizada
    private List<String> obtenerInteresesCombinados() {
        // Obtener los intereses que están usando los usuarios en la BD
        List<String> interesesEnUso = userRepository.findAllIntereses();
        //Crear un set combinando los base y los de la BD set evita duplicados automáticamente
        Set<String> conjuntoTotal = new HashSet<>(INTERESES_BASE);
        if (interesesEnUso != null) {
            conjuntoTotal.addAll(interesesEnUso);
        }
        //Convertir a lista y ordenar alfabéticamente para que se vea bien
        List<String> listaFinal = new ArrayList<>(conjuntoTotal);
        Collections.sort(listaFinal);
        return listaFinal;
    }
    // Página Principal el feed
    @GetMapping("/")
    public String index(@RequestParam(value="filtro", required=false) String filtro, Model model, Authentication auth) {

        User logueado = null;
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            logueado = userService.findByEmail(auth.getName());
        }

        List<User> todos = userRepository.findAll();
        // Identificamos a los Admins para no mostrarlos
        Role rolAdmin = roleRepository.findByName("ROLE_ADMIN");
        List<UserRole> relacionesAdmin = userRoleRepository.findByRole(rolAdmin);
        List<Long> idsAdmins = relacionesAdmin.stream()
                .map(r -> r.getUser().getId())
                .collect(Collectors.toList());

        User candidato = null;

        if (logueado != null) {
            User finalLogueado = logueado;
            // ALGORITMO DE SELECCIÓN DE CANDIDATO
            candidato = todos.stream()
                    .filter(u -> !u.getId().equals(finalLogueado.getId()))
                    .filter(u -> !idsAdmins.contains(u.getId()))
                    .filter(u -> !u.getEmail().equals("admin@admin.com"))
                    // Que no le haya dado like o dislike ya
                    .filter(u -> matchActionRepository.findByEmisorAndReceptor(finalLogueado, u).isEmpty())
                    // Filtro por interés si está activo
                    .filter(u -> filtro == null || filtro.isEmpty() || (u.getInteres() != null && u.getInteres().equals(filtro)))
                    .findFirst()
                    .orElse(null);
        } else {
            Collections.shuffle(todos);
            candidato = todos.stream()
                    .filter(u -> !idsAdmins.contains(u.getId()))
                    .filter(u -> !u.getEmail().equals("admin@admin.com"))
                    .filter(u -> filtro == null || filtro.isEmpty() || (u.getInteres() != null && u.getInteres().equals(filtro)))
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("usuarioCandidato", candidato);

        // MODIFICADO: Ahora usamos la lista dinámica
        model.addAttribute("listaIntereses", obtenerInteresesCombinados());

        model.addAttribute("filtroActivo", filtro);

        return "index";
    }
    // Ver y Editar Perfil
    @GetMapping("/perfil")
    public String perfil(Model model, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        model.addAttribute("user", user);
        List<String> listaTotal = obtenerInteresesCombinados();
        model.addAttribute("listaIntereses", listaTotal);
        boolean esCustom = user.getInteres() != null && !listaTotal.contains(user.getInteres());
        model.addAttribute("esInteresCustom", esCustom);
        return "perfil";
    }

    // Procesar actualización de perfil fotos y datos
    @PostMapping("/subir/fotos")
    public String subirFotos(@RequestParam("avatarFile") MultipartFile avatarFile,
                             @RequestParam("galeriaFiles") MultipartFile[] galeriaFiles,
                             @RequestParam("biografia") String biografia,
                             @RequestParam(value = "interesSeleccion", required = false) String interesSeleccion,
                             @RequestParam(value = "interesNuevo", required = false) String interesNuevo,
                             @RequestParam(value = "edad", required = false) Integer edad,
                             @RequestParam(value = "ubicacion", required = false) String ubicacion,
                             Authentication auth) {

        User user = userService.findByEmail(auth.getName());

        user.setBiografia(biografia);
        user.setEdad(edad);
        user.setUbicacion(ubicacion);

        // Lógica para elegir entre interés del desplegable o uno nuevo escrito
        String interesFinal = null;
        if ("OTRO".equals(interesSeleccion) && interesNuevo != null && !interesNuevo.trim().isEmpty()) {
            interesFinal = interesNuevo;
        } else if (esInteresPersonalizado(interesSeleccion, interesNuevo)) {
            interesFinal = interesNuevo;
        } else {
            interesFinal = interesSeleccion;
        }
        if ("OTRO".equals(interesFinal)) interesFinal = null;
        user.setInteres(interesFinal);

        // Subida de Avatar
        if (!avatarFile.isEmpty()) {
            String imgName = "avatar-" + user.getId() + "-" + System.currentTimeMillis() + ".png";
            fileProcessingService.uploadFile(avatarFile, imgName);
            user.setAvatar(imgName);
        }

        // Subida de Galería
        if (galeriaFiles != null && galeriaFiles.length > 0) {
            for (MultipartFile fichero : galeriaFiles) {
                if (!fichero.isEmpty()) {
                    String fileName = "galeria-" + user.getId() + "-" + System.currentTimeMillis() + ".png";
                    fileProcessingService.uploadFile(fichero, fileName);

                    Foto nuevaFoto = new Foto();
                    nuevaFoto.setNombreArchivo(fileName);
                    nuevaFoto.setUsuario(user);
                    nuevaFoto.setLikes(0);

                    user.getFotosGaleria().add(nuevaFoto);
                }
            }
        }

        userService.save(user);
        return "redirect:/perfil?success";
    }
    //Guardar el nuevo interes creado
    private boolean esInteresPersonalizado(String seleccion, String nuevo) {
        return (seleccion == null || seleccion.isEmpty()) && (nuevo != null && !nuevo.isEmpty());
    }

    @PostMapping("/perfil/eliminar-foto")
    public String eliminarFoto(@RequestParam("idFoto") Long idFoto, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        user.getFotosGaleria().removeIf(f -> f.getId().equals(idFoto));
        userService.save(user);
        return "redirect:/perfil";
    }

    @GetMapping("/usuario/{id}")
    public String verUsuarioPublico(@PathVariable("id") Long id, Model model, Authentication auth) {
        User logueado = userService.findByEmail(auth.getName());
        User usuario = userService.findById(id);

        if (logueado.getId().equals(usuario.getId())) {
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("miUsuario", logueado);

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

    @GetMapping("/usuario/{id}/compartir")
    public String compartirPerfil(@PathVariable Long id, Authentication auth) {
        // Lógica simple: Enviar un correo fijo a ti mismo o al usuario logueado
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@tinder.com");
        message.setTo(auth.getName()); // Se lo envía a quien está logueado
        message.setSubject("¡Mira este perfil de Tinder!");
        message.setText("He encontrado un perfil interesante: http://localhost:8080/usuario/" + id);
        emailSender.send(message);

        return "redirect:/usuario/" + id + "?compartido=true";
    }

}