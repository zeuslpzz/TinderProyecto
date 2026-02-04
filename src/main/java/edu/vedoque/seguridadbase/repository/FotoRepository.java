package edu.vedoque.seguridadbase.repository;

import edu.vedoque.seguridadbase.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
}