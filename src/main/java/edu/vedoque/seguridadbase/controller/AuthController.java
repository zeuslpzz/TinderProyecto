package edu.vedoque.seguridadbase.controller;

import edu.vedoque.seguridadbase.dto.UserDto;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AuthController {

    private UserService userService;

    // Inyecto el servicio de usuarios en el constructor para poder guardar y buscar gente en la base de datos
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Muestra la página de Login
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // Muestra el formulario de Registro
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    // Procesa el Registro cuando le das a registrarse
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto user,
                               BindingResult result,
                               Model model){
        // Primero compruebo si ya existe alguien con ese email para no tener duplicados
        User existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        // Si hay errores en el formulario como campos vacíos vuelvo a cargar la página de registro para que los corrijan
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }

        // Si todo está correcto guardo el usuario usando el servicio y redirijo con un mensaje de éxito
        userService.saveUser(user);
        return "redirect:/register?success";
    }

    // Este método saca la lista de todos los usuarios registrados para mostrarlos en la tabla de administración
    @GetMapping("/users")
    public String listRegisteredUsers(Model model){
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
}