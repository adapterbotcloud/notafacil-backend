package br.com.notafacil.repository;

import br.com.notafacil.entity.RpsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RpsRepository extends JpaRepository<RpsEntity, Long> {

    @Query("SELECT DISTINCT r.idCobranca FROM RpsEntity r WHERE r.empresa.id = :empresaId AND r.idCobranca IN :ids")
    List<Long> findIdCobrancasByEmpresaAndIds(@Param("empresaId") Long empresaId, @Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT r.idCobranca FROM RpsEntity r WHERE r.empresa.id = :empresaId AND r.idCobranca IS NOT NULL")
    List<Long> findAllIdCobrancasByEmpresa(@Param("empresaId") Long empresaId);
}
