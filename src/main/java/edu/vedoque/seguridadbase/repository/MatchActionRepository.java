package edu.vedoque.seguridadbase.repository;

import edu.vedoque.seguridadbase.entity.MatchAction;
import edu.vedoque.seguridadbase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchActionRepository extends JpaRepository<MatchAction, Long> {

    // Comprobar si ya existe un like/dislike entre dos personas
    Optional<MatchAction> findByEmisorAndReceptor(User emisor, User receptor);

    // Contar likes para verificar Match
    @Query("SELECT COUNT(m) FROM MatchAction m WHERE m.emisor = :emisor AND m.receptor = :receptor AND m.leGusta = true")
    long countLikesRecibidos(@Param("receptor") User receptor, @Param("emisor") User emisor);

    // Métodos para borrar historial de chat cuando se borra usuario
    void deleteByEmisor(User emisor);
    void deleteByReceptor(User receptor);
}