package br.com.notafacil.provider;

import br.com.notafacil.config.NfseProviderProperties;
import br.com.notafacil.entity.MunicipioProvedorEntity;
import br.com.notafacil.repository.MunicipioProvedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConfigDrivenProviderResolver implements ProviderResolver {

    private static final Logger log = LoggerFactory.getLogger(ConfigDrivenProviderResolver.class);

    private final Map<String, NfseProvider> providerMap;
    private final NfseProviderProperties properties;
    private final MunicipioProvedorRepository municipioRepo;

    public ConfigDrivenProviderResolver(List<NfseProvider> providers,
                                         NfseProviderProperties properties,
                                         MunicipioProvedorRepository municipioRepo) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(NfseProvider::getProviderId, Function.identity()));
        this.properties = properties;
        this.municipioRepo = municipioRepo;
        log.info("ProviderResolver inicializado com {} providers: {}", providerMap.size(), providerMap.keySet());
    }

    @Override
    public NfseProvider resolve(String codigoMunicipio) {
        // 1) Mapeamento explícito em application.yml
        if (properties.getMunicipioMapping() != null) {
            String mappedId = properties.getMunicipioMapping().get(codigoMunicipio);
            if (mappedId != null) {
                NfseProvider p = providerMap.get(mappedId);
                if (p != null) {
                    log.debug("Município {} resolvido via config → {}", codigoMunicipio, mappedId);
                    return p;
                }
            }
        }

        // 2) Fallback: tabela municipio_provedor no banco
        Optional<MunicipioProvedorEntity> dbMapping = municipioRepo.findByCodigoMunicipio(codigoMunicipio);
        if (dbMapping.isPresent() && dbMapping.get().getHabilitado()) {
            NfseProvider p = providerMap.get(dbMapping.get().getProviderId());
            if (p != null) {
                log.debug("Município {} resolvido via banco → {}", codigoMunicipio, dbMapping.get().getProviderId());
                return p;
            }
        }

        // 3) Fallback: default por UF (2 primeiros dígitos do código IBGE)
        if (codigoMunicipio != null && codigoMunicipio.length() >= 2 && properties.getUfDefaults() != null) {
            String uf = codigoMunicipio.substring(0, 2);
            String ufProviderId = properties.getUfDefaults().get(uf);
            if (ufProviderId != null) {
                NfseProvider p = providerMap.get(ufProviderId);
                if (p != null) {
                    log.debug("Município {} resolvido via UF default ({}) → {}", codigoMunicipio, uf, ufProviderId);
                    return p;
                }
            }
        }

        // 4) Fallback final: PADRAO_NACIONAL ou primeiro disponível
        NfseProvider fallback = providerMap.get("PADRAO_NACIONAL");
        if (fallback != null) {
            log.debug("Município {} resolvido via fallback → PADRAO_NACIONAL", codigoMunicipio);
            return fallback;
        }

        // Se não há PADRAO_NACIONAL, usa GINFES como fallback absoluto
        fallback = providerMap.get("GINFES");
        if (fallback != null) {
            log.warn("Município {} sem provider específico, usando GINFES como fallback", codigoMunicipio);
            return fallback;
        }

        throw new IllegalStateException("Nenhum NfseProvider disponível para o município: " + codigoMunicipio);
    }

    @Override
    public NfseProvider resolveById(String providerId) {
        NfseProvider p = providerMap.get(providerId);
        if (p == null) {
            throw new IllegalArgumentException("Provider não encontrado: " + providerId);
        }
        return p;
    }
}
