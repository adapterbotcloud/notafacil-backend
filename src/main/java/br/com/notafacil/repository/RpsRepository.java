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

    List<RpsEntity> findByEmpresaId(Long empresaId);

    @Query("SELECT r FROM RpsEntity r WHERE r.empresa.id = :empresaId AND YEAR(r.createdAt) = :ano AND MONTH(r.createdAt) = :mes ORDER BY r.createdAt DESC")
    List<RpsEntity> findByEmpresaIdAndAnoMes(@Param("empresaId") Long empresaId, @Param("ano") int ano, @Param("mes") int mes);

    @Query("SELECT r FROM RpsEntity r WHERE r.empresa.id = :empresaId AND YEAR(r.createdAt) = :ano ORDER BY r.createdAt DESC")
    List<RpsEntity> findByEmpresaIdAndAno(@Param("empresaId") Long empresaId, @Param("ano") int ano);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r) FROM RpsEntity r WHERE r.empresa.id = :empresaId GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) ORDER BY YEAR(r.createdAt) DESC, MONTH(r.createdAt) DESC")
    List<Object[]> findDistinctAnoMesByEmpresaId(@Param("empresaId") Long empresaId);
}
