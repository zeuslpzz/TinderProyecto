package edu.vedoque.seguridadbase.service.impl;

import edu.vedoque.seguridadbase.dto.UserDto;
import edu.vedoque.seguridadbase.entity.Role;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.entity.UserRole;
import edu.vedoque.seguridadbase.repository.RoleRepository;
import edu.vedoque.seguridadbase.repository.UserRepository;
import edu.vedoque.seguridadbase.repository.UserRoleRepository;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRoleRepository userRoleRepository;

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    // Uso el constructor para inyectar las dependencias porque es la forma recomendada por Spring para asegurarnos de que no sean nulas
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Estos métodos son simples puentes hacia el repositorio para poder guardar cambios en el perfil o buscar usuarios por id desde los controladores
    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        // Uso el orElse null para evitar que explote si no encuentra al usuario aunque lo ideal sería gestionar una excepción
        return userRepository.findById(id).orElse(null);
    }

    // Este método es el que uso en el registro. Recibo un DTO objeto de transferencia y lo convierto en una Entidad real de base de datos
    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        // Aquí es donde ocurre la magia de la encriptación antes de guardar
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);

        // Por defecto todos los que se registran son usuarios normales así que les busco el rol ROLE_USER y se lo asigno en la tabla intermedia
        Role role = roleRepository.findByName("ROLE_USER");
        userRoleRepository.save(new UserRole(user, role));
    }

    // Este método lo uso si cambio la contraseña desde el panel de admin para asegurarme de que se vuelve a encriptar
    @Override
    public void saveCifrandoPassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    // Método básico para buscar por email que es lo que usa Spring Security para el login
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Recupero todos los usuarios pero los convierto a DTO para no enviar información sensible como la contraseña encriptada a la vista
    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map((user) -> convertEntityToDto(user))
                .collect(Collectors.toList());
    }

    // Método auxiliar para pasar los datos de la entidad de base de datos al objeto ligero DTO que usamos en el formulario
    @Override
    public UserDto convertEntityToDto(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        userDto.setAvatar(user.getAvatar());
        return userDto;
    }

    // Busco manualmente los roles del usuario consultando la tabla intermedia user_roles
    @Override
    public List<Role> conseguirRolesByUser(User user){
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        List<Role> roles = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            roles.add(userRole.getRole());
        }
        return roles;
    }
}