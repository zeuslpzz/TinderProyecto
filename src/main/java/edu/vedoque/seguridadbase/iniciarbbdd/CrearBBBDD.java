package edu.vedoque.seguridadbase.iniciarbbdd;

import edu.vedoque.seguridadbase.entity.Role;
import edu.vedoque.seguridadbase.entity.User;
import edu.vedoque.seguridadbase.entity.UserRole;
import edu.vedoque.seguridadbase.repository.RoleRepository;
import edu.vedoque.seguridadbase.repository.UserRoleRepository;
import edu.vedoque.seguridadbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CrearBBBDD implements CommandLineRunner {

    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    UserService userService;
    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        if(roleRepository.findAll().isEmpty()){


            Role roleAdmin = new Role();
            roleAdmin.setName("ROLE_ADMIN");
            roleRepository.save(roleAdmin);

            Role roleUser = new Role();
            roleUser.setName("ROLE_USER");
            roleRepository.save(roleUser);

            User superAdmin = new User();
            superAdmin.setName("Administrador");
            superAdmin.setEmail("admin@admin.com");
            superAdmin.setPassword("admin");
            superAdmin.setInteres("Gestión");
            userService.saveCifrandoPassword(superAdmin);

            UserRole adminRoleRelation = new UserRole();
            adminRoleRelation.setRole(roleAdmin);
            adminRoleRelation.setUser(superAdmin);
            userRoleRepository.save(adminRoleRelation);


            User user = new User();
            user.setName("Antonio Salinas");
            user.setEmail("asalinasci@gmail.com");
            user.setPassword("1234");
            userService.saveCifrandoPassword(user);

            UserRole userRole = new UserRole();
            userRole.setRole(roleAdmin);
            userRole.setUser(user);
            userRoleRepository.save(userRole);

            User user2 = new User();
            user2.setName("Hugo");
            user2.setEmail("hugo@gmail.com");
            user2.setPassword("1234");
            user2.setBiografia("Me encanta el baloncesto y meter triples.");
            user2.setAvatar("hugo.png");
            userService.saveCifrandoPassword(user2);

            UserRole userRole2 = new UserRole();
            userRole2.setRole(roleUser);
            userRole2.setUser(user2);
            userRoleRepository.save(userRole2);


            User user3 = new User();
            user3.setName("Sergio");
            user3.setEmail("sergio@gmail.com");
            user3.setPassword("1234");
            user3.setAvatar("sergio.png");
            userService.saveCifrandoPassword(user3);

            UserRole userRole3 = new UserRole();
            userRole3.setRole(roleUser);
            userRole3.setUser(user3);
            userRoleRepository.save(userRole3);

        }
    }
}