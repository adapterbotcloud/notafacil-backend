package br.com.notafacil.repository;

import br.com.notafacil.entity.CstIbsCbsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CstIbsCbsRepository extends JpaRepository<CstIbsCbsEntity, Long> {

    Optional<CstIbsCbsEntity> findByCodigo(String codigo);

    List<CstIbsCbsEntity> findByHabilitadoTrue();
}
