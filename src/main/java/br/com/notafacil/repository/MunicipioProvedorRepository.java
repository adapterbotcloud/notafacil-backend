package br.com.notafacil.repository;

import br.com.notafacil.entity.MunicipioProvedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MunicipioProvedorRepository extends JpaRepository<MunicipioProvedorEntity, Long> {

    Optional<MunicipioProvedorEntity> findByCodigoMunicipio(String codigoMunicipio);

    List<MunicipioProvedorEntity> findByUf(String uf);

    List<MunicipioProvedorEntity> findByProviderId(String providerId);
}
