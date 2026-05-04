package br.com.notafacil.repository;

import br.com.notafacil.entity.ClassificacaoTributariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassificacaoTributariaRepository extends JpaRepository<ClassificacaoTributariaEntity, Long> {

    Optional<ClassificacaoTributariaEntity> findByCodigo(String codigo);

    List<ClassificacaoTributariaEntity> findByHabilitadoTrue();
}
