package br.com.notafacil.repository;

import br.com.notafacil.entity.IndicadorOperacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IndicadorOperacaoRepository extends JpaRepository<IndicadorOperacaoEntity, Long> {

    Optional<IndicadorOperacaoEntity> findByCodigo(String codigo);

    List<IndicadorOperacaoEntity> findByHabilitadoTrue();
}
