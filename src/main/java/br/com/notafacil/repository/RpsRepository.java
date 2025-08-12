package br.com.notafacil.repository;

import br.com.notafacil.entity.RpsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RpsRepository extends JpaRepository<RpsEntity, Long> {
    // você pode adicionar métodos de consulta customizados, se precisar
}
