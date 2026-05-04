package br.com.notafacil.repository;

import br.com.notafacil.entity.NbsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NbsRepository extends JpaRepository<NbsEntity, Long> {

    Optional<NbsEntity> findByCodigo(String codigo);

    List<NbsEntity> findByHabilitadoTrue();

    List<NbsEntity> findByDescricaoContainingIgnoreCase(String termo);
}
