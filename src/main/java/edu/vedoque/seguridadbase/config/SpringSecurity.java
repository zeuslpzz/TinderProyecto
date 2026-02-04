package edu.vedoque.seguridadbase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                        authorize
                                // Dejo entrar a todo el mundo a los recursos estáticos y a la página de registro
                                .requestMatchers("/register/**", "/css/**", "/js/**", "/img/**", "/file/download/**", "/").permitAll()

                                // Solo dejo entrar a los administradores a la zona de gestión
                                .requestMatchers("/admin/**", "/users").hasRole("ADMIN")

                                // Para el resto de zonas importantes el usuario tiene que haber iniciado sesión
                                .requestMatchers("/chat/**").authenticated()
                                .requestMatchers("/matches/**").authenticated()
                                .requestMatchers("/perfil/**").authenticated()
                                .requestMatchers("/subir/**").authenticated()
                                .requestMatchers("/usuario/**").authenticated()

                                // Cualquier otra url que no haya puesto arriba obligo a que esté autenticada por seguridad
                                .anyRequest().authenticated()
                ).formLogin(
                        form -> form
                                // Indico cual es mi página personalizada de login
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/", true)
                                .permitAll()
                ).logout(
                        logout -> logout
                                // Configuro la ruta para cerrar sesión y borrar los datos de la sesión
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                );
        return http.build();
    }
}