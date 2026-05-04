package br.com.notafacil.service;

import br.com.notafacil.config.NfseProviderProperties;
import br.com.notafacil.entity.EmpresaEntity;
import br.com.notafacil.provider.NfseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Resolve o regime tributário (ISS ou IBS_CBS) para uma empresa/provider.
 *
 * Cascata de decisão:
 * 1. Feature global desligada → ISS
 * 2. Provider não suporta IBS/CBS → ISS
 * 3. Empresa não optou por IBS/CBS → ISS
 * 4. Tudo OK → IBS_CBS
 */
@Component
public class TaxRegimeResolver {

    private static final Logger log = LoggerFactory.getLogger(TaxRegimeResolver.class);

    public static final String ISS = "ISS";
    public static final String IBS_CBS = "IBS_CBS";

    private final NfseProviderProperties properties;

    public TaxRegimeResolver(NfseProviderProperties properties) {
        this.properties = properties;
    }

    public String resolve(EmpresaEntity empresa, NfseProvider provider) {
        // 1. Feature global desligada
        if (!properties.getFeatures().isIbsCbsEnabled()) {
            log.debug("IBS/CBS desabilitado globalmente → ISS");
            return ISS;
        }

        // 2. Provider não suporta
        if (!provider.supportsIbsCbs()) {
            log.debug("Provider {} não suporta IBS/CBS → ISS", provider.getProviderId());
            return ISS;
        }

        // 3. Empresa não optou
        if (!Boolean.TRUE.equals(empresa.getIbsCbsHabilitado())) {
            log.debug("Empresa {} não optou por IBS/CBS → ISS", empresa.getCnpj());
            return ISS;
        }

        // 4. Tudo OK
        log.info("Empresa {} usando regime IBS_CBS via provider {}", empresa.getCnpj(), provider.getProviderId());
        return IBS_CBS;
    }

    /**
     * Retorna a versão do schema adequada para o regime tributário.
     */
    public String resolveSchemaVersion(String regimeTributario) {
        return IBS_CBS.equals(regimeTributario) ? "4" : "3";
    }
}
