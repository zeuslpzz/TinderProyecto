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

    // Método sencillo que solo carga la vista del formulario de inicio de sesión cuando alguien entra en la ruta login
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // Aquí preparo el formulario de registro enviando un objeto vacío para que el usuario pueda rellenar sus datos
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    // Este es el método importante donde proceso el registro cuando le dan al botón de enviar
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