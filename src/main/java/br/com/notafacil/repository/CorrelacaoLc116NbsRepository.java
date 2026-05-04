package br.com.notafacil.repository;

import br.com.notafacil.entity.CorrelacaoLc116NbsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CorrelacaoLc116NbsRepository extends JpaRepository<CorrelacaoLc116NbsEntity, Long> {

    List<CorrelacaoLc116NbsEntity> findByItemListaServico(String itemListaServico);

    Optional<CorrelacaoLc116NbsEntity> findFirstByItemListaServicoAndHabilitadoTrue(String itemListaServico);

    List<CorrelacaoLc116NbsEntity> findByNbsCodigo(String nbsCodigo);

    List<CorrelacaoLc116NbsEntity> findByHabilitadoTrue();
}
