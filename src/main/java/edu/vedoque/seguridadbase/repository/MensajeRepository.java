package edu.vedoque.seguridadbase.repository;

import edu.vedoque.seguridadbase.entity.Mensaje;
import edu.vedoque.seguridadbase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {


    @Query("SELECT m FROM Mensaje m WHERE " +
            "(m.remitente = :remitente AND m.destinatario = :destinatario) OR " +
            "(m.remitente = :destinatario AND m.destinatario = :remitente) " +
            "ORDER BY m.fechaHora ASC")
    List<Mensaje> findByRemitenteAndDestinatario(@Param("remitente") User remitente, @Param("destinatario") User destinatario);

    @Query(value = "SELECT remitente_id, COUNT(*) FROM Mensaje WHERE destinatario_id=:destinatario AND leido=false GROUP BY remitente_id", nativeQuery = true)
    List<Map<String, Object>> countByDestinatario(@Param("destinatario") long destinatario);


    void deleteByRemitente(User remitente);
    void deleteByDestinatario(User destinatario);
}