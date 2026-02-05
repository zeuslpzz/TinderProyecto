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

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

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

    // Carga la lista completa de usuarios
    @GetMapping
    public String panelGestion(Model model) {
        List<User> usuarios = userRepository.findAll();
        model.addAttribute("listaUsuarios", usuarios);
        return "admin";
    }


    //Mostrar la página de gestión de categorías
    @GetMapping("/categorias")
    public String gestionarCategorias(Model model) {
        // Obtenemos las categorías reales que existen en la BD
        List<String> categorias = userRepository.findAllIntereses();
        model.addAttribute("categorias", categorias);
        return "admin_categorias";
    }

    //Procesar el cambio de nombre renombrar etiqueta
    @PostMapping("/categorias/editar")
    public String editarCategoria(@RequestParam("viejoNombre") String viejoNombre,
                                  @RequestParam("nuevoNombre") String nuevoNombre) {
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            userRepository.actualizarInteres(viejoNombre, nuevoNombre);
        }
        return "redirect:/admin/categorias?edited=true";
    }

    //Procesar la eliminación dejar usuarios sin etiqueta
    @PostMapping("/categorias/eliminar")
    public String eliminarCategoria(@RequestParam("nombre") String nombre) {
        userRepository.eliminarInteres(nombre);
        return "redirect:/admin/categorias?deleted=true";
    }


    @PostMapping("/delete/{id}")
    @Transactional
    public String eliminarUsuario(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            matchActionRepository.deleteByEmisor(user);
            matchActionRepository.deleteByReceptor(user);
            mensajeRepository.deleteByRemitente(user);
            mensajeRepository.deleteByDestinatario(user);
            userRoleRepository.deleteByUser(user);
            userRepository.delete(user);
        }
        return "redirect:/admin?deleted=true";
    }

    @PostMapping("/toggle-role/{id}")
    public String cambiarRol(@PathVariable("id") Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
            if (user.getRoles().contains(roleAdmin)) {
                user.getRoles().remove(roleAdmin);
            } else {
                user.getRoles().add(roleAdmin);
            }
            userRepository.save(user);
        }
        return "redirect:/admin";
    }

    // Muestra el formulario de edición de usuario
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "redirect:/admin";

        model.addAttribute("usuario", user);

        //Ahora cargamos la lista dinámica de la BD para que el admin vea las opciones reales
        List<String> interesesBD = userRepository.findAllIntereses();
        model.addAttribute("listaIntereses", interesesBD);

        return "admin_editar";
    }

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
            user.setName(name);
            user.setEmail(email);
            user.setInteres(interes);
            user.setBiografia(biografia);
            user.setEdad(edad);
            user.setUbicacion(ubicacion);

            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            userRepository.save(user);
        }
        return "redirect:/admin?edited=true";
    }
}