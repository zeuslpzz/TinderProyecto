package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.entity.Role;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Necesito inyectar casi todos los repositorios porque al borrar un usuario tengo que borrar manualmente sus rastros en otras tablas
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MatchActionRepository matchActionRepository;
    @Autowired
    private MensajeRepository mensajeRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<String> LISTA_INTERESES = Arrays.asList(
            "Deportista", "Cinéfilo", "Gamer", "Viajero", "Músico",
            "Lector", "Fiestero", "Aventurero", "Tranquilo"
    );

    // Carga la lista completa de usuarios de la base de datos para mostrarla en la tabla del panel de control
    @GetMapping
    public String panelGestion(Model model) {
        List<User> usuarios = userRepository.findAll();
        model.addAttribute("listaUsuarios", usuarios);
        return "admin";
    }

    // Este método borra un usuario y todos sus datos relacionados
    // Uso @Transactional para que todas las operaciones de borrado se traten como una sola transacción y evitar inconsistencias si algo falla
    @PostMapping("/delete/{id}")
    @Transactional
    public String eliminarUsuario(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Antes de borrar al usuario, tengo que limpiar sus likes y mensajes para no romper la integridad referencial de la base de datos
            matchActionRepository.deleteByEmisor(user);
            matchActionRepository.deleteByReceptor(user);
            mensajeRepository.deleteByRemitente(user);
            mensajeRepository.deleteByDestinatario(user);
            userRoleRepository.deleteByUser(user);

            // Una vez limpio todo lo demás, ya puedo borrar al usuario sin problemas
            userRepository.delete(user);
        }
        return "redirect:/admin?deleted=true";
    }

    // Lógica sencilla para dar o quitar permisos de administrador a un usuario existente
    @PostMapping("/toggle-role/{id}")
    public String cambiarRol(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
            // Si ya tiene el rol se lo quito, y si no lo tiene se lo añado
            if (user.getRoles().contains(roleAdmin)) {
                user.getRoles().remove(roleAdmin);
            } else {
                user.getRoles().add(roleAdmin);
            }
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    // Muestra el formulario con los datos actuales del usuario cargados para poder modificarlos
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "redirect:/admin";

        model.addAttribute("usuario", user);
        model.addAttribute("listaIntereses", LISTA_INTERESES);
        return "admin_editar";
    }

    // Recoge los datos del formulario de edición y actualiza la entidad en la base de datos
    @PostMapping("/editar/guardar")
    public String guardarEdicion(@RequestParam("id") Long id,
                                 @RequestParam("name") String name,
                                 @RequestParam("email") String email,
                                 @RequestParam("interes") String interes,
                                 @RequestParam("biografia") String biografia,
                                 @RequestParam(value = "edad", required = false) Integer edad,
                                 @RequestParam(value = "ubicacion", required = false) String ubicacion,
                                 @RequestParam(value = "password", required = false) String password) {

        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Actualizo los campos básicos
            user.setName(name);
            user.setEmail(email);
            user.setInteres(interes);
            user.setBiografia(biografia);
            user.setEdad(edad);
            user.setUbicacion(ubicacion);

            // Solo encripto y actualizo la contraseña si el campo no viene vacío
            // Esto evita sobreescribir la contraseña actual con una cadena vacía si el admin no quería cambiarla
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            userRepository.save(user);
        }
        return "redirect:/admin?edited=true";
    }
}