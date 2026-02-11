package edu.vedoque.seguridadbase.repository;

import edu.vedoque.seguridadbase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    // Obtener lista de intereses únicos (para el desplegable)
    @Query("SELECT DISTINCT u.interes FROM User u WHERE u.interes IS NOT NULL AND u.interes <> ''")
    List<String> findAllIntereses();

    // Actualizar nombre de un interés Admin
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.interes = :nuevoNombre WHERE u.interes = :viejoNombre")
    void actualizarInteres(String viejoNombre, String nuevoNombre);

    // Eliminar un interés ponerlo a null
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.interes = NULL WHERE u.interes = :nombre")
    void eliminarInteres(String nombre);
}